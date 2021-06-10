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
  Wrap the vault token
  withAzureEnv(secret: 'secret/acme') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withAzureEnv')
  def secret = args.containsKey('secret') ? args.secret : 'secret/observability-team/ci/service-account/azure-vm-extension'
  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withAzureEnv: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def value = props?.data
  def tenant = value?.tenant
  def username = value?.username
  def password = value?.password
  def subscription = value?.subscription
  validateField(tenant, "withAzureEnv: was not possible to get the authentication info for the tenant field.")
  validateField(username, "withAzureEnv: was not possible to get the authentication info for the username field.")
  validateField(password, "withAzureEnv: was not possible to get the authentication info for the password field.")
  validateField(subscription, "withAzureEnv: was not possible to get the authentication info for the subscription field.")
  withEnvMask(vars: [
    [var: 'AZ_USERNAME', password: username],
    [var: 'AZ_PASSWORD', password: password],
    [var: 'AZ_TENANT', password: tenant],
    [var: 'AZ_SUBSCRIPTION', password: subscription]
  ]){
    body()
  }
}

def validateField(String value, String errorMessage) {
  if(value == null || value?.trim() == ""){
    error errorMessage
  }
}
