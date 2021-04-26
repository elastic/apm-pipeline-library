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

@Library('apm@master') _

pipeline {
  agent { label 'linux && immutable' }
  environment {
    REPO = 'observability-dev'
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL = 'INFO'
    DRY_RUN_MODE = "${params.DRY_RUN_MODE}"
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  parameters {
    booleanParam(name: 'DRY_RUN_MODE', defaultValue: true, description: 'If true, allows to execute this pipeline in dry run mode.')
  }
  stages {
    stage('Sync GitHub labels') {
      steps {
        deleteDir()
        git(url: "https://github.com/elastic/${REPO}.git", credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
        withCredentials([string(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7', variable: 'GITHUB_TOKEN')]) {
          sh '''#!/bin/bash -e
            cd .github/labels
            github-labels-sync.sh "${GITHUB_TOKEN}" "${DRY_RUN_MODE}"
          '''
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
