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
  withVaultToken(path: '/foo', tokenFile: '.myfile') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withVaultToken')
  def path = args.containsKey('path') ? args.path : env.WORKSPACE
  def tokenFile = args.containsKey('tokenFile') ? args.tokenFile : '.vault-token'
  getVaultSecret.readSecretWrapper {
    // When running in the CI with multiple parallel stages
    // the access could be considered as a DDOS attack. Let's sleep a bit if it fails.
    retryWithSleep(retries: 3, seconds: 5, backoff: true) {
      def token = getVaultSecret.getVaultToken(env.VAULT_ADDR, env.VAULT_ROLE_ID, env.VAULT_SECRET_ID)
      dir(path) {
        writeFile file: tokenFile, text: token
      }
    }
    try {
      body()
    } catch (err) {
      throw err
    } finally {
      // ensure any sensitive details are deleted
      dir(path) {
        if (fileExists("${tokenFile}")) {
          if(isUnix()){
            sh "rm ${tokenFile}"
          } else {
            bat "del ${tokenFile}"
          }
        }
      }
    }
  }
}
