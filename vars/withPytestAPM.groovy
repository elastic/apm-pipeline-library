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
  It configures environment variables needed to use pytest-apm plugin.
  You can pass and APM URL+Token or a Vault secret like {"url": "https://apm.example.com:8200", "token": "SKmaKSwsnaA1"}

  withPytestAPM(serviceName: 'MyService'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', url: 'https://apm.example.com:8200', token: 'SKmaKSwsnaA1'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', secret: 'secret/team/my_secret'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', secret: 'secret/team/my_secret', labels: '{"name1": "value1", "name2": "value2"}'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', secret: 'secret/team/my_secret', customContext: '{"name1": "value1", "name2": "value2"}'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', secret: 'secret/team/my_secret', sessionName: 'MyTestSession'){
    sh(label: 'Test', 'pytest')
  }

  withPytestAPM(serviceName: 'MyService', secret: 'secret/team/my_secret', transactionMode: true){
    sh(label: 'Test', 'pytest')
  }
*/
def call(Map args = [:], Closure body) {
  def customContext = args.containsKey('customContext') ? "--apm-custom-context '${args.customContext}'" : ''
  def labels = args.containsKey('labels') ? "--apm-labels '${args.labels}'" : ''
  def serviceName = args.containsKey('serviceName') ? "--apm-service-name '${args.serviceName}'" : ''
  def sessionName = args.containsKey('sessionName') ? "--apm-session-name '${args.sessionName}'" : ''
  def transactionMode = args.containsKey('transactionMode') && args.transactionMode ? "--apm-transaction-mode" : ''
  def venv = args.containsKey('venv') ? "--apm-labels '${args.venv}'" : "${env.PYTHON_ENV}"

  withAPMService(args){
    def pytestApmRelease = "https://github.com/elastic/apm-pipeline-library/releases/download/pytest-apm-0.0.1/pytest-apm-0.0.1.tar.gz"
    python(label: 'Install pytest-apm', cmd: "-m pip install ${pytestApmRelease}", venv: "${venv}")
    withEnv([
      "PYTEST_ADDOPTS='${env.PYTEST_ADDOPTS} --apm-server-url ${env.APM_CLI_SERVER_URL} --apm-token ${env.APM_CLI_TOKEN} --apm-service-name ${serviceName} ${sessionName} ${labels} ${customContext} ${transactionMode}'",
    ]){
      body()
    }
  }
}
