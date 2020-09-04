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

// NOTE: Consumers should use the current tag
// @Library('apm@current') _
// NOTE: Master branch will contain the upcoming release changes
//       this will help us to detect any breaking changes in production.
@Library('apm@master') _

pipeline {

  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
    LANG = "C.UTF-8"
    LC_ALL = "C.UTF-8"
    PYTHONUTF8 = "1"
    APM_CLI_SERVICE_NAME = "${env.JOB_NAME}"
  }
  stages {
    stage('Checkout') {
      options { skipDefaultCheckout() }
      steps {
        // Just in case the workspace is reset.
        deleteDir()
        apmCLITransaction("Pipeline", true)
        apmCLITransaction("${STAGE_NAME} - BEGIN", false)
        apmCLITransaction("${STAGE_NAME} - END", false)
      }
    }
    stage('lint') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        apmCLITransaction("${STAGE_NAME} - BEGIN", false)
        apmCLITransaction("${STAGE_NAME} - END", false)
      }
    }
    stage('test') {
      options { skipDefaultCheckout() }
      steps {
        // Just in case the workspace is reset.
        deleteDir()
        apmCLITransaction("${STAGE_NAME} - BEGIN", false)
        apmCLITransaction("${STAGE_NAME} - END", false)
      }
    }
    stage('build') {
      options { skipDefaultCheckout() }
      steps {
        deleteDir()
        apmCLITransaction("${STAGE_NAME} - BEGIN", false)
        apmCLITransaction("${STAGE_NAME} - END", false)
      }
    }
  }
}

def apmCLITransaction(transactionName, saveTsID){
  if(!isUnix()){
    return
  }

  def setupPy = libraryResource("scripts/apm-cli/requirements.txt")
  def apmCliPy = libraryResource("scripts/apm-cli/apm-cli.py")
  dir('apm-cli'){
    writeFile file: "requirements.txt", text: setupPy
    writeFile file: "apm-cli.py", text: apmCliPy
    def apmJson = getVaultSecret(secret: "secret/observability-team/ci/test-clusters/dev-next-oblt/k8s-apm")?.data.value
    def apm = readJSON(text: apmJson)
    withEnvMask(vars: [
      [var: "APM_CLI_SERVER_URL", password:"${apm.url}"],
      [var: "APM_CLI_TOKEN", password: "${apm.token}"],
      [var: "APM_CLI_TRANSACTION_NAME", password:" ${transactionName}"],
      [var: "APM_CLI_PARENT_TRANSACTION_SAVE", password: "${ saveTsID ? 'tsID.txt' : ''}"],
    ]){
      sh(script: """#!/bin/bash +x
            if [ -z "\$(command -v python3)" ] \
              || [ -z "\$(command -v virtualenv)" ]; then
              exit 0
            fi
            virtualenv -q --python=python3 .venv
            source ".venv/bin/activate"
            pip -q install -r requirements.txt
            set -x
            python3 apm-cli.py --transaction-name "${transactionName}"
            sleep 10s
          """,
        label: 'apm-cli'
      )
      if(saveTsID){
        setEnvVar('APM_CLI_PARENT_TRANSACTION', readFile(file: 'tsID.txt'))
      }
    }
  }
}
