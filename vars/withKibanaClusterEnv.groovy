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
  Wrap the Kibana cluster credentials and entrypoints as environment variables that are masked

  withKibanaClusterEnv(cluster: 'test-cluster-azure') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withKibanaClusterEnv')
  def cluster = args.containsKey('cluster') ? args.cluster : error('withKibanaClusterEnv: cluster parameter is required.')
  def secret = "${getTestClusterSecret()}/${cluster}/k8s-kibana"
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withKibanaClusterEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def dataJson = props?.data.value
  def kibana = toJSON(dataJson)
  def kibana_url = kibana.url
  def username = kibana.username
  def password = kibana.password
  validateField(kibana_url, "withKibanaClusterEnv: was not possible to get the authentication info for the url field.")
  validateField(username, "withKibanaClusterEnv: was not possible to get the authentication info for the username field.")
  validateField(password, "withKibanaClusterEnv: was not possible to get the authentication info for the password field.")
  withEnvMask(vars: [
    [var: 'KIBANA_URL', password: kibana_url],
    [var: 'KIBANA_USERNAME', password: username],
    [var: 'KIBANA_PASSWORD', password: password]
  ]){
    body()
  }
}

def validateField(String value, String errorMessage) {
  if(value == null || value?.trim() == ""){
    error errorMessage
  }
}
