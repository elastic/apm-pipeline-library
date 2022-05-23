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
  Grab a secret from the vault, define the environment variables which have been
  passed as parameters and mask the secrets

  withSecretVault(secret: 'secret', user_var_name: 'my_user_env', pass_var_name: 'my_password_env'){
    //block
  }
*/
def call(Map args = [:], Closure body) {
  def secret = args?.secret
  def data = args?.get('data', [:])

  // For backward compatibility
  if (data.isEmpty()) {
    backward(args, body)
  } else {
    def vars = [:]
    def props = readSecretFromVault(args)
    data.each{ k, v ->
      vars << readSecret(props, k, v)
    }
    withEnvMask(vars: vars){
      body()
    }
  }
}

def readSecret(props, key_id, environment_variable) {
  if (!props || !key_id || !environment_variable) {
    error "withSecretVault: Missing variables"
  }
  def value = props?.data?.get(key_id)
  if(value == null){
    error('withSecretVault: was not possible to get authentication info')
  }
  return [var: "${environment_variable}", password: value]
}

def backward(Map args = [:], Closure body) {
  def secret = args?.secret
  def user_variable = args?.user_var_name
  def user_key = args.containsKey('user_key') ? args.user_key : 'user'
  def pass_variable = args?.pass_var_name
  def pass_key = args.containsKey('pass_key') ? args.pass_key : 'password'

  if (!secret || !user_variable || !pass_variable) {
    error('withSecretVault: Missing variables')
  }

  def props = readSecretFromVault(args)
  def user = props?.data?.get(user_key)
  def password = props?.data?.get(pass_key)

  if(user == null || password == null){
    error("withSecretVault: was not possible to get authentication info")
  }

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
        [var: "${user_variable}", password: user],
        [var: "${pass_variable}", password: password],
  ]]) {
    withEnv(["${user_variable}=${user}", "${pass_variable}=${password}"]) {
      body()
    }
  }
}

def readSecretFromVault(args) {
  def role_id = args.containsKey('role_id') ? args.role_id : 'vault-role-id'
  def secret_id = args.containsKey('secret_id') ? args.secret_id : 'vault-secret-id'
  def props = getVaultSecret(secret: args.secret, role_id: role_id, secret_id: secret_id)
  if(props?.errors){
    error("withSecretVault: Unable to get credentials from the vault: " + props.errors.toString())
  }
  return props
}
