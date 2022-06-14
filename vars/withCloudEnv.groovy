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
  Wrap the ESS cluster credentials and entrypoints as environment variables that are masked

  withCloudEnv(cluster: 'test-cluster-azure') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withCloudEnv')
  def cluster = args.containsKey('cluster') ? args.cluster : error('withCloudEnv: cluster parameter is required.')
  def secret = "${getTestClusterSecret()}/${cluster}/ec-deployment"
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withCloudEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def value = props?.data
  def cloud_id = value?.cloud_id
  def username = value?.username
  def password = value?.password
  validateField(cloud_id, "withCloudEnv: was not possible to get the authentication info for the cloud_id field.")
  validateField(username, "withCloudEnv: was not possible to get the authentication info for the username field.")
  validateField(password, "withCloudEnv: was not possible to get the authentication info for the password field.")
  withEnvMask(vars: [
    [var: 'CLOUD_USERNAME', password: username],
    [var: 'CLOUD_PASSWORD', password: password],
    [var: 'CLOUD_ID', password: cloud_id]
  ]){
    body()
  }
}

def validateField(String value, String errorMessage) {
  if(value == null || value?.trim() == ""){
    error errorMessage
  }
}
