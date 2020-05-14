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

@Library('apm@current') _

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    rateLimitBuilds(throttle: [count: 30, durationName: 'hour', userBoost: true])
    quietPeriod(10)
  }
  triggers {
    cron 'H H(3-4) * * 1-5'
  }
  parameters {
    string(name: 'PARAM_WITH_DEFAULT_VALUE', defaultValue: 'defaultValue', description: 'It would not be defined on the first build, see JENKINS-41929.')
  }
  stages {
    stage('Checkout') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        pipelineManager([ cancelPreviousRunningBuilds: [ when: 'PR' ] ])
        gitCheckout(basedir: "${BASE_DIR}", branch: 'master',
          repo: "git@github.com:elastic/${env.REPO}.git",
          credentialsId: "${JOB_GIT_CREDENTIALS}",
          githubNotifyFirstTimeContributor: false,
          reference: "/var/lib/jenkins/${env.REPO}.git")
        stash allowEmpty: true, name: 'source', useDefaultExcludes: false
      }
    }
    stage('Workers Checks'){
      matrix {
        agent { label "${PLATFORM}" }
        axes {
          axis {
            name 'PLATFORM'
            values 'ubuntu && immutable', 'worker-c07l34n6dwym', 'worker-c07y20b6jyvy', 'worker-c07ll940dwyl', 'worker-c07y20b9jyvy', 'worker-c07y20b4jyvy', 'worker-c07y20bcjyvy', 'worker-395930', 'worker-0a434dec4bdcd060f', 'windows-immutable && windows-2019', 'windows-immutable && windows-2016', 'windows-immutable && windows-2012', 'windows-immutable && windows-10', 'windows-immutable && windows-8', 'windows-immutable && windows-2008-r2', 'windows-immutable && windows-7', 'windows-immutable-32-bit && windows-7-32-bit'
          }
        }
        stages {
          stage('Report') {
            options { skipDefaultCheckout() }
            steps {
              echo "NODE_LABELS=${env.NODE_LABELS}"
              echo "NODE_NAME=${env.NODE_NAME}"
              echo "PLATFORM=${PLATFORM}"
            }
          }
          stage('Build') {
            options { skipDefaultCheckout() }
            environment {
              PARAM_WITH_DEFAULT_VALUE = "${params?.PARAM_WITH_DEFAULT_VALUE}" //see JENKINS-41929
            }
            steps {
              runBuild()
            }
          }
          stage('Test') {
            options { skipDefaultCheckout() }
            steps {
              runTest()
            }
            post {
              always {
                junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${BASE_DIR}/**/junit-*.xml")
              }
            }
          }
        }
      }
    }
  }
  post {
    cleanup {
      notifyBuildResult()
    }
  }
}

def runBuild(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    if (isUnix()) {
      sh(returnStatus: true, script: './resources/scripts/jenkins/build.sh')
    } else {
      bat(returnStatus: true, script: '.\\resources\\scripts\\jenkins\\build.bat')
    }
  }
}

def runTest(){
  deleteDir()
  unstash 'source'
  dir("${BASE_DIR}"){
    if (isUnix()) {
      def uname = sh script: 'uname', returnStdout: true
      if (uname.startsWith("Darwin")) {
         sh(returnStatus: true, script: './resources/scripts/jenkins/beats-ci/test-mac.sh')
      } else {
        // worker-0a434dec4bdcd060f does not have any reference repos
        def runPackerCacheTests = PLATFORM.equals('worker-0a434dec4bdcd060f') ? 'false' : 'true'
        // Ephemeral workers don't have a HOME env variable.
        withEnv(["HOME=${env.WORKSPACE}"]){
          sh(returnStatus: true, script: "./resources/scripts/jenkins/beats-ci/test.sh ${runPackerCacheTests}")
        }
        docker.image('node:12').inside(){
          withEnv(["HOME=${env.WORKSPACE}/${BASE_DIR}"]){
            sh(script: './resources/scripts/jenkins/build.sh')
          }
        }
      }
    } else {
      powershell(returnStatus: true, script: ".\\resources\\scripts\\jenkins\\beats-ci\\test.ps1")
    }
  }
}
