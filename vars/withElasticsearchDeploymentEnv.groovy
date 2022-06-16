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
  Wrap the Elasticsearch credentials and entrypoints as environment variables that are masked
  for the Elastic Cloud deployment

  withElasticsearchDeploymentEnv(cluster: 'test-cluster-azure') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withElasticsearchDeploymentEnv')
  def cluster = args.containsKey('cluster') ? args.cluster : error('withElasticsearchDeploymentEnv: cluster parameter is required.')
  def secret = "${getTestClusterSecret()}/${cluster}/k8s-elasticsearch"
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withElasticsearchDeploymentEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def esJson = props?.data.value
  def es = toJSON(esJson)
  def es_url = es.url
  def username = es.username
  def password = es.password
  validateValue(es_url, "withElasticsearchDeploymentEnv: was not possible to get the authentication info for the url field.")
  validateValue(username, "withElasticsearchDeploymentEnv: was not possible to get the authentication info for the username field.")
  validateValue(password, "withElasticsearchDeploymentEnv: was not possible to get the authentication info for the password field.")
  withEnvMask(vars: [
    [var: 'ES_URL', password: es_url],
    [var: 'ES_USERNAME', password: username],
    [var: 'ES_PASSWORD', password: password]
  ]){
    body()
  }
}
