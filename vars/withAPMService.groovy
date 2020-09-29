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
  It defines and masks the environment variables APM_CLI_SERVER_URL and APM_CLI_TOKEN,
  you can pass the URL and the token, or pass a Vault secret like {"url": "https://apm.example.com:8200", "token": "SKmaKSwsnaA1"}

  withAPMService(){
    apmCli()
    echo "Hello world"
    apmCli()
  }
*/
def call(Map args = [:], Closure body) {
  def apmCliConfig = args.containsKey('apmCliConfig') ? args.apmCliConfig : "secret/observability-team/ci/jenkins-stats-cloud-apm"
  def url = args.containsKey('url') ? args.url : ''
  def token = args.containsKey('token') ? args.token : ''

  if(!url && !token){
    def apmJson = getVaultSecret(secret: "${apmCliConfig}")?.data
    def apm = readJSON(text: apmJson)
    url = apm.url
    token = apm.token
  }

  withEnvMask(vars: [
    [var: "APM_CLI_SERVER_URL", password: "${url}"],
    [var: "APM_CLI_TOKEN", password: "${token}"],
  ]){
    body()
  }
}
