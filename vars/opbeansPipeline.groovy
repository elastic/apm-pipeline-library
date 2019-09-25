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

import groovy.transform.Field

/**
This is the list of builds to be triggered.
*/
@Field def builds

def call(Map pipelineParams) {
  builds = pipelineParams?.get('downstreamJobs', [])
  pipeline {
    agent { label 'linux && immutable' }
    environment {
      BASE_DIR = 'src/github.com/elastic'
      NOTIFY_TO = credentials('notify-to')
      JOB_GCS_BUCKET = credentials('gcs-bucket')
      JOB_GCS_CREDENTIALS = 'apm-ci-gcs-plugin'
      DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
      PIPELINE_LOG_LEVEL = 'INFO'
      PATH = "${env.PATH}:${env.WORKSPACE}/bin"
      HOME = "${env.WORKSPACE}"
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
              sh "make test"
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
      stage('Release') {
        agent { label 'linux && immutable' }
        when {
          branch 'master'
          beforeAgent true
        }
        steps {
          withGithubNotify(context: 'Release') {
            deleteDir()
            unstash 'source'
            dir(BASE_DIR){
              dockerLogin(secret: "${DOCKERHUB_SECRET}", registry: 'docker.io')
              sh "VERSION=latest make publish"
            }
          }
        }
      }
      stage('Downstream') {
        when {
          allOf {
            branch 'master'
            expression { return !builds?.isEmpty() }
          }
          beforeAgent true
        }
        steps {
          script {
            builds.each { job ->
              build job: "${job}", propagate: false, wait: false
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
