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
  Get the totp code from the vault, define the environment variables which have been
  passed as parameters and mask the secrets

  withTotpVault(secret: 'secret', code_var_name: 'VAULT_TOTP'){
    //block
  }
*/
def call(Map params = [:], Closure body) {
  def secret = params.containsKey('secret') ? params.secret : error('withTotpVault: Missing secret parameter')
  def code_var_name = params.containsKey('code_var_name') ? params.code_var_name : error('withTotpVault: Missing code_var_name parameter')

  def props = getVaultSecret(secret: secret)
  if (props?.errors) {
    error "withTotpVault: Unable to get credentials from the vault: ${props.errors.toString()}"
  }


  def code = props?.data?.code
  if(code == null){
    error 'withTotpVault: was not possible to get authentication info.'
  }

  wrap([$class: 'MaskPasswordsBuildWrapper',
        varPasswordPairs: [ [var: "${code_var_name}", password: code] ]]) {
    withEnv(["${code_var_name}=${code}"]) {
      body()
    }
  }
}
