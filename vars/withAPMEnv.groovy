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
Prepare the context with the ELASTIC_APM_SERVER_URL and ELASTIC_APM_SECRET_TOKEN
variables that are consumed by the body in order to send the data to the APM Server.

withAPMEnv(secret: 'secrets/my-secret-apm') {
  // the command that consumes those env variables.
}
*/

def call(Map args = [:], Closure body) {
  def secret = args.get('secret', 'secret/observability-team/ci/jenkins-stats')
  def tokenFieldName = args.get('tokenFieldName', 'apmServerToken')
  def urlFieldName = args.get('urlFieldName', 'apmServerUrl')

  def props = getVaultSecret(secret: secret)
  if(props?.errors){
    log(level: 'WARN', text: 'withAPMEnv: is disabled. Unable to get credentials from the vault: ' + props.errors.toString())
  } else if(props?.data?.get(urlFieldName, false) && props?.data?.get(tokenFieldName, false)) {
    log(level: 'INFO', text: 'withAPMEnv: is enabled.')
    withEnvMask(vars: [
      [var: 'ELASTIC_APM_SERVER_URL', password: props.data.get(urlFieldName)],
      [var: 'ELASTIC_APM_SECRET_TOKEN', password: props.data.get(tokenFieldName)]
    ]){
      body()
    }
  } else {
    log(level: 'WARN', text: 'withAPMEnv: is disabled. Missing fields in the vault secret')
    body()
  }
}
