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
import groovy.transform.Field

@Field def transactions = [:]

def call(Map args = [:]) {
  def apmCliConfig = args.containsKey('apmCliConfig') ? args.apmCliConfig : "secret/observability-team/ci/test-clusters/dev-next-oblt/k8s-apm"
  def url = args.containsKey('url') ? args.url : ''
  def token = args.containsKey('token') ? args.token : ''
  def serviceName = args.containsKey('serviceName') ? args.serviceName : "${env.APM_CLI_SERVICE_NAME ? env.APM_CLI_SERVICE_NAME : ''}"
  def parentTransaction = args.containsKey('parentTransaction') ? args.parentTransaction : "${env.APM_CLI_PARENT_TRANSACTION ? env.APM_CLI_PARENT_TRANSACTION : ''}"
  def saveTsID = args.containsKey('saveTsID') ? args.saveTsID : false
  def transactionName = args.containsKey('transactionName') ? args.transactionName : "${env.STAGE_NAME}"
  def spanName = args.containsKey('spanName') ? args.spanName : ''
  def spanCommand = args.containsKey('spanCommand') ? args.spanCommand : ''
  def spanLabel = args.containsKey('spanLabel') ? args.spanLabel : ''
  def result = args.containsKey('result') ? args.result : ''

  if(!isUnix() || !serviceName){
    log(level: 'DEBUG', text: "apmCli: not executed.")
    return
  }

  log(level: 'DEBUG', text: "apmCli: ${args}")

  //A Span needs a name.
  if(spanCommand && !spanName){
    log(level: 'DEBUG', text: "apmCli: Set span name to : ${spanCommand}")
    spanName = spanCommand
  }

  def setupPy = libraryResource("scripts/apm-cli/requirements.txt")
  def apmCliPy = libraryResource("scripts/apm-cli/apm-cli.py")
  dir('apm-cli'){
    log(level: 'DEBUG', text: "apmCli: Installing APM CLI")
    writeFile file: "requirements.txt", text: setupPy
    writeFile file: "apm-cli.py", text: apmCliPy

    if(!url && !token){
      def apmJson = getVaultSecret(secret: "${apmCliConfig}")?.data.value
      def apm = readJSON(text: apmJson)
      url = apm.url
      token = apm.token
    }

    // transactions with and span do not need BEGIN/END
    if(!spanName) {
      if(transactions["${transactionName}"]) {
        log(level: 'DEBUG', text: "apmCli: Transaction ${transactionName} end")
        transactionName += "-END"
      } else {
        log(level: 'DEBUG', text: "apmCli: Transaction ${transactionName} begin")
        transactionName += "-BEGIN"
        transactions["${transactionName}"] = true
      }
    }

    withEnvMask(vars: [
      [var: "APM_CLI_SERVER_URL", password: "${url}"],
      [var: "APM_CLI_TOKEN", password: "${token}"],
      [var: "APM_CLI_SERVICE_NAME", password: "${serviceName}"],
      [var: "APM_CLI_TRANSACTION_NAME", password: "${transactionName}"],
      [var: "APM_CLI_SPAN_NAME", password: "${spanName}"],
      [var: "APM_CLI_SPAN_COMMAND", password: "${spanCommand}"],
      [var: "APM_CLI_SPAN_LABELS", password: "${spanLabel}"],
      [var: "APM_CLI_PARENT_TRANSACTION_SAVE", password: "${ saveTsID ? 'tsID.txt' : ''}"],
      [var: "APM_CLI_TRANSACTION_RESULT", password: "${result}"],
      [var: "APM_CLI_PARENT_TRANSACTION", password: "${parentTransaction}"],
    ]){
      log(level: 'DEBUG', text: "apmCli: Runninf APM CLI")
      sh(script: """#!/bin/bash +x
            if [ -z "\$(command -v python3)" ] \
              || [ -z "\$(command -v virtualenv)" ]; then
              exit 0
            fi
            virtualenv -q --python=python3 .venv
            source ".venv/bin/activate"
            pip -q install -r requirements.txt
            set -x
            export |grep APM_CLI
            python3 apm-cli.py
          """,
        label: 'apm-cli'
      )
      if(saveTsID){
        log(level: 'DEBUG', text: "apmCli: Persistent transaction ID on APM_CLI_PARENT_TRANSACTION")
        setEnvVar('APM_CLI_PARENT_TRANSACTION', readFile(file: 'tsID.txt'))
      }
    }
  }
}
