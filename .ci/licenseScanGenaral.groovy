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
  agent { label 'ubuntu && immutable' }
  environment {
    BASE_DIR = "license/scan"
    HOME = "${env.WORKSPACE}"
  }
  options {
    timeout(time: 2, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    disableConcurrentBuilds()
  }
  parameters {
    string(name: 'branch_specifier', defaultValue: 'master', description: 'the Git branch specifier to scan.')
    string(name: 'repo', defaultValue: 'git@github.com:elastic/apm-pipeline-library.git', description: 'the Git repository to scan.')
  }
  stages {
    stage('License Scan') {
      steps {
        script {
          currentBuild.description = "Third-party license scan of ${params.repo}/${params.branch}"
        }
        dir("${env.BASE_DIR}"){
          git(credentialsId: 'f6c7695a-671e-4f4f-a331-acdce44ff9ba',
            url: "${params.repo}",
            branch: "${params.branch_specifier}"
          )
          licenseScan()
        }
      }
    }
  }
}
