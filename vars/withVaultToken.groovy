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

def call(Map params = [:], Closure body) {
  log(level: 'INFO', text: 'withVaultToken')
  def path = params.containsKey('path') ? params.path : env.WORKSPACE
  def tokenFile = params.containsKey('tokenFile') ? params.tokenFile : '.vault-token'
  getVaultSecret.readSecretWrapper {
    retry(2) {
      sleep randomNumber(min: 5, max: 10)
      def token = getVaultSecret.getVaultToken(env.VAULT_ADDR, env.VAULT_ROLE_ID, env.VAULT_SECRET_ID)
      dir(path) {
        writeFile file: tokenFile, text: token
      }
      try {
        body()
      } catch (err) {
        error "withVaultToken: error ${err}"
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
}
