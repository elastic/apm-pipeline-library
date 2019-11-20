// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**
  Opbeans Pipeline

  opbeansPipeline()

  opbeansPipeline(downstreamJobs: ['job1', 'folder/job1', 'mbp/PR-1'])
*/

def call(Map pipelineParams) {
  pipeline {
    agent { label 'linux && immutable' }
    environment {
      REPO = 'opbeans-go'
      BASE_DIR = "src/github.com/elastic/${env.REPO?.trim() ?: 'foo'}"
      NOTIFY_TO = credentials('notify-to')
      JOB_GCS_BUCKET = credentials('gcs-bucket')
      JOB_GCS_CREDENTIALS = 'apm-ci-gcs-plugin'
      DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
      PIPELINE_LOG_LEVEL = 'INFO'
      PATH = "${env.PATH}:${env.WORKSPACE}/bin"
      HOME = "${env.WORKSPACE}"
      DOCKER_REGISTRY_SECRET = 'secret/apm-team/ci/docker-registry/prod'
      REGISTRY = 'docker.elastic.co'
      STAGING_IMAGE = "${env.REGISTRY}/observability-ci/${env.REPO}"
      GITHUB_CHECK_ITS_NAME = 'Integration Tests'
      ITS_PIPELINE = 'apm-integration-tests-selector-mbp/master'
    }
    options {
      timeout(time: 1, unit: 'HOURS')
      buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
      timestamps()
      ansiColor('xterm')
      disableResume()
      durabilityHint('PERFORMANCE_OPTIMIZED')
      rateLimitBuilds(throttle: [count: 60, durationName: 'hour', userBoost: true])
      quietPeriod(10)
    }
    triggers {
      issueCommentTrigger('(?i).*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
    }
    stages {
      /**
       Checkout the code and stash it, to use it on other stages.
      */
      stage('Checkout') {
        steps {
          deleteDir()
          gitCheckout(basedir: BASE_DIR)
          stash allowEmpty: true, name: 'source', useDefaultExcludes: false
        }
      }
      /**
      Build the project from code..
      */
      stage('Build') {
        steps {
          withGithubNotify(context: 'Build') {
            deleteDir()
            unstash 'source'
            dir(BASE_DIR){
              sh 'make build'
            }
          }
        }
      }
      /**
      Execute unit tests.
      */
      stage('Test') {
        steps {
          withGithubNotify(context: 'Test', tab: 'tests') {
            deleteDir()
            unstash 'source'
            dir(BASE_DIR){
              sh 'make test'
            }
          }
        }
        post {
          always {
            junit(allowEmptyResults: true,
              keepLongStdio: true,
              testResults: "${BASE_DIR}/**/junit-*.xml")
          }
        }
      }
      stage('Staging') {
        steps {
          withGithubNotify(context: 'Staging') {
            deleteDir()
            unstash 'source'
            dir(BASE_DIR){
              dockerLogin(secret: "${DOCKER_REGISTRY_SECRET}", registry: "${REGISTRY}")
              sh script: "VERSION=${env.GIT_BASE_COMMIT} IMAGE=${env.STAGING_IMAGE} make publish", label: "push docker image to ${env.STAGING_IMAGE}"
            }
          }
        }
      }
      stage('Integration Tests') {
        steps {
          runBuildITs("${env.REPO}", "${env.STAGING_IMAGE}")
        }
      }
      stage('Downstream') {
        when {
          allOf {
            branch 'master'
            expression { return pipelineParams?.downstreamJobs }
          }
          beforeAgent true
        }
        steps {
          script {
            pipelineParams?.downstreamJobs.each { job ->
              build job: "${job}", propagate: false, wait: false
            }
          }
        }
      }
      stage('Release') {
        agent { label 'linux && immutable' }
        options { skipDefaultCheckout() }
        when {
          anyOf {
            branch 'master'
            tag pattern: 'v\\d+\\.\\d+.*', comparator: 'REGEXP'
          }
          beforeAgent true
        }
        stages {
          stage('Publish') {
            steps {
              withGithubNotify(context: 'Publish') {
                deleteDir()
                unstash 'source'
                dir(BASE_DIR){
                  dockerLogin(secret: "${DOCKERHUB_SECRET}", registry: 'docker.io')
                  sh "VERSION=${env.BRANCH_NAME.equals('master') ? 'latest' : env.BRANCH_NAME} make publish"
                }
              }
            }
          }
          stage('Release Notes') {
            when {
              expression { return false }
            }
            steps {
              echo 'TBD'
            }
          }
        }
      }
    }
    post {
      always {
        notifyBuildResult()
      }
    }
  }
}

def runBuildITs(String repo, String stagingDockerImage) {
  build(job: env.ITS_PIPELINE, propagate: waitIfNotPR(),
        wait: env.CHANGE_ID?.trim() ? false : true,
        parameters: [string(name: 'AGENT_INTEGRATION_TEST', value: 'Opbeans'),
                     string(name: 'BUILD_OPTS', value: "${generateBuildOpts(repo, stagingDockerImage)}"),
                     string(name: 'GITHUB_CHECK_NAME', value: env.GITHUB_CHECK_ITS_NAME),
                     string(name: 'GITHUB_CHECK_REPO', value: env.REPO),
                     string(name: 'GITHUB_CHECK_SHA1', value: env.GIT_BASE_COMMIT)])
  githubNotify(context: "${env.GITHUB_CHECK_ITS_NAME}", description: "${env.GITHUB_CHECK_ITS_NAME} ...", status: 'PENDING', targetUrl: "${env.JENKINS_URL}search/?q=${env.ITS_PIPELINE.replaceAll('/','+')}")
}

def generateBuildOpts(String repo, String stagingDockerImage) {
  switch(repo) {
    case 'opbeans-go':
      opts = "--with-opbeans-go --opbeans-go-branch ${env.GIT_BASE_COMMIT} --opbeans-go-repo ${getForkedRepoOrElasticRepo(repo)}"
      break;
    case 'opbeans-java':
      opts = "--with-opbeans-java --opbeans-java-image ${stagingDockerImage} --opbeans-java-version ${env.GIT_BASE_COMMIT}"
      break;
   default:
      opts = ''
    break;
  }
  return opts.toString()
}

def waitIfNotPR() {
  return env.CHANGE_ID?.trim() ? false : true
}

def getForkedRepoOrElasticRepo(String repo) {
  // See https://issues.jenkins-ci.org/browse/JENKINS-58450
  if (env.CHANGE_FORK?.contains('/')) {
    return env.CHANGE_FORK
  } else {
    return "${env.CHANGE_FORK?.trim() ?: 'elastic' }/${repo}".toString()
  }
}
