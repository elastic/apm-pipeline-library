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
  Wrap the .credentials.json token
  withAzureCredentials(path: '/foo', credentialsFile: '.credentials.json') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  def path = args.containsKey('path') ? args.path : env.HOME
  def credentialsFile = args.containsKey('credentialsFile') ? args.credentialsFile : '.credentials.json'
  def secret = args.containsKey('secret') ? args.secret : 'secret/apm-team/ci/apm-agent-dotnet-azure'

  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withAzureCredentials: Unable to get credentials from the vault: ${props.errors.toString()}"
  }
  def client_id = props?.data?.client_id
  def client_secret = props?.data?.client_secret
  def subscription_id = props?.data?.subscription_id
  def tenant_id = props?.data?.tenant_id
  if (client_id == null || client_secret == null || subscription_id == null || tenant_id == null) {
    error 'withAzureCredentials: was not possible to get authentication info'
  }
  dir(path) {
    writeFile file: credentialsFile, text: """
{
  "clientId": "${client_id}",
  "clientSecret": "${client_secret}",
  "subscriptionId": "${subscription_id}",
  "tenantId": "${tenant_id}"
}
"""
  }
  try {
    body()
  } catch(err) {
    error("withAzureCredentials: error ${err.toString()}")
  } finally {
    // ensure any sensitive details are deleted
    dir(path) {
      if (fileExists("${credentialsFile}")) {
        if(isUnix()){
          sh "rm ${credentialsFile}"
        } else {
          bat "del ${credentialsFile}"
        }
      }
    }
  }
}
