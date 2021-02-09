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
  Write the given data in vault for the given secret.

  writeVaultSecret(secret: 'secret/apm-team/ci/temp/github-comment', data: ['secret': 'foo'] )
*/
import groovy.json.JsonOutput

def call(Map args = [:]) {
  def secret = args.containsKey('secret') ? args.secret : error ('writeVaultSecret: secret parameter is required.')
  def data = args.containsKey('data') ? args.data : error ('writeVaultSecret: data parameter is required.')

  // Ensure the data is transformed to Json and then toString.
  def transformedData = JsonOutput.toJson(data)

  getVaultSecret.readSecretWrapper {
    // When running in the CI with multiple parallel stages
    // the access could be considered as a DDOS attack. Let's sleep a bit if it fails.
    retryWithSleep(retries: 3, seconds: 5, backoff: true) {
      def token = getVaultSecret.getVaultToken(env.VAULT_ADDR, env.VAULT_ROLE_ID, env.VAULT_SECRET_ID)
      writeVaultSecretObject(env.VAULT_ADDR, secret, token, transformedData)
    }
  }
}

def writeVaultSecretObject(addr, secret, token, data){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_SECRET', password: secret],
    [var: 'VAULT_TOKEN', password: token],
    [var: 'VAULT_ADDR', password: addr] ]]) {
    httpRequest(url: "${addr}/v1/${secret}",
                method: 'POST',
                headers: ['X-Vault-Token': "${token}", 'Content-Type': 'application/json'],
                data: data)
  }
}
