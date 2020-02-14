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
  Opbeans Selector Pipeline

  opbeansSelectorPipeline()
*/

def call() {
  pipeline {
    agent { label 'linux && immutable' }
    environment {
      BASE_DIR = 'src/github.com/elastic'
      NOTIFY_TO = credentials('notify-to')
      JOB_GCS_BUCKET = credentials('gcs-bucket')
      JOB_GIT_CREDENTIALS = '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'
      PIPELINE_LOG_LEVEL = 'INFO'
      BUILD_OPTS = "${params?.BUILD_OPTS}"
      DETAILS_ARTIFACT_URL = "${env.BUILD_URL}artifact/${env.DETAILS_ARTIFACT}"
    }
    options {
      timeout(time: 1, unit: 'HOURS')
      timestamps()
      ansiColor('xterm')
      disableResume()
      durabilityHint('PERFORMANCE_OPTIMIZED')
      rateLimitBuilds(throttle: [count: 60, durationName: 'hour', userBoost: true])
    }
    parameters {
      string(name: 'BUILD_OPTS', defaultValue: '', description: 'Build options to build and test the opbeans project')
      string(name: 'GITHUB_CHECK_NAME', defaultValue: '', description: 'Name of the GitHub check to be updated. Only if this build is triggered from another parent stream.')
      string(name: 'GITHUB_CHECK_REPO', defaultValue: '', description: 'Name of the GitHub repo to be updated. Only if this build is triggered from another parent stream.')
      string(name: 'GITHUB_CHECK_SHA1', defaultValue: '', description: 'Name of the GitHub repo to be updated. Only if this build is triggered from another parent stream.')
    }
    stages {
      stage('Checkout') {
        options {
          skipDefaultCheckout()
        }
        steps {
          githubCheckNotify('PENDING')
          deleteDir()
          gitCheckout(basedir: "${BASE_DIR}")
          stash allowEmpty: true, name: 'source', useDefaultExcludes: false
        }
      }
      /**
      Build the project from code..
      */
      stage('Build') {
        options {
          skipDefaultCheckout()
        }
        steps {
          deleteDir()
          unstash 'source'
          dir("${env.BASE_DIR}"){
            sh "${env.BUILD_OPTS} make build"
          }
        }
      }
      /**
      Execute unit tests.
      */
      stage('Test') {
        options {
          skipDefaultCheckout()
        }
        steps {
          deleteDir()
          unstash 'source'
          dir("${env.BASE_DIR}"){
            sh "${env.BUILD_OPTS} make test"
          }
        }
        post {
          always {
            junit(allowEmptyResults: true, keepLongStdio: true, testResults: "${env.BASE_DIR}/**/junit-*.xml")
          }
        }
      }
    }
    post {
      cleanup {
        githubCheckNotify(currentBuild.currentResult == 'SUCCESS' ? 'SUCCESS' : 'FAILURE')
        notifyBuildResult()
      }
    }
  }
}

/**
 Notify the GitHub check of the parent stream
**/
def githubCheckNotify(String status) {
  if (params.GITHUB_CHECK_NAME?.trim() && params.GITHUB_CHECK_REPO?.trim() && params.GITHUB_CHECK_SHA1?.trim()) {
    githubNotify context: "${params.GITHUB_CHECK_NAME}",
                 description: "${params.GITHUB_CHECK_NAME} ${status.toLowerCase()}",
                 status: "${status}",
                 targetUrl: "${env.RUN_DISPLAY_URL}",
                 sha: params.GITHUB_CHECK_SHA1, account: 'elastic', repo: params.GITHUB_CHECK_REPO, credentialsId: env.JOB_GIT_CREDENTIALS
  }
}
