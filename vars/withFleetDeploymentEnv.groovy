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
  Wrap the Fleet credentials and entrypoints as environment variables that are masked
  for the Elastic Cloud deployment

  withFleetDeploymentEnv(cluster: 'test-cluster-azure') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withFleetDeploymentEnv')
  def cluster = args.containsKey('cluster') ? args.cluster : error('withFleetDeploymentEnv: cluster parameter is required.')
  def secret = "${getTestClusterSecret()}/${cluster}/k8s-apm"
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withFleetDeploymentEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def dataJson = props?.data.value
  def fleet = toJSON(dataJson)
  def fleet_url = fleet.fleet_url
  def token = fleet.token
  errorIfEmpty(fleet_url, "withFleetDeploymentEnv: was not possible to get the authentication info for the url field.")
  errorIfEmpty(token, "withFleetDeploymentEnv: was not possible to get the authentication info for the username field.")
  withEnvMask(vars: [
    [var: 'FLEET_URL', password: fleet_url],
    [var: 'FLEET_TOKEN', password: token]
  ]){
    body()
  }
}
