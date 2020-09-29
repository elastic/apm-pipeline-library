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
  def serviceName = args.containsKey('serviceName') ? args.serviceName : "${env.APM_CLI_SERVICE_NAME ? env.APM_CLI_SERVICE_NAME : ''}"
  def parentTransaction = args.containsKey('parentTransaction') ? args.parentTransaction : "${env.APM_CLI_PARENT_TRANSACTION ? env.APM_CLI_PARENT_TRANSACTION : ''}"
  def saveTsID = args.containsKey('saveTsID') ? args.saveTsID : false
  def transactionName = args.containsKey('transactionName') ? args.transactionName : "${env.STAGE_NAME}"
  def spanName = args.containsKey('spanName') ? args.spanName : ''
  def spanCommand = args.containsKey('spanCommand') ? args.spanCommand : ''
  def spanLabel = args.containsKey('spanLabel') ? args.spanLabel : ''
  def result = args.containsKey('result') ? args.result : ''

  if(!serviceName){
    log(level: 'DEBUG', text: "apmCli: not executed.")
    return
  }

  log(level: 'DEBUG', text: "apmCli: ${args}")

  //A Span needs a name.
  if(spanCommand && !spanName){
    log(level: 'DEBUG', text: "apmCli: Set span name to : ${spanCommand}")
    spanName = spanCommand
  }

  dir('apm-cli'){
    def apmCliRelease = "https://github.com/elastic/apm-pipeline-library/releases/download/apm-cli-0.0.1/apm-cli-0.0.1.tar.gz"
    python(label: 'Install apmCLI', cmd: "-m pip install ${apmCliRelease}")
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

    withAPMService(args){
      withEnv([
        "APM_CLI_SERVICE_NAME=${serviceName}",
        "APM_CLI_TRANSACTION_NAME=${transactionName}",
        "APM_CLI_SPAN_NAME=${spanName}",
        "APM_CLI_SPAN_COMMAND=${spanCommand}",
        "APM_CLI_SPAN_LABELS=${spanLabel}",
        "APM_CLI_PARENT_TRANSACTION_SAVE=${ saveTsID ? 'tsID.txt' : ''}",
        "APM_CLI_TRANSACTION_RESULT=${result}",
        "APM_CLI_PARENT_TRANSACTION=${parentTransaction}",
      ]){
        log(level: 'DEBUG', text: "apmCli: Runninf APM CLI")
        python(label: 'apmCLI', file: 'apm-cli.py')
        if(saveTsID){
          log(level: 'DEBUG', text: "apmCli: Persistent transaction ID on APM_CLI_PARENT_TRANSACTION")
          setEnvVar('APM_CLI_PARENT_TRANSACTION', readFile(file: 'tsID.txt'))
        }
      }
    }
  }
}
