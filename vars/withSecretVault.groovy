#!/usr/bin/env groovy

/**
  Grab a secret from the vault, define the environment variables which have been
  passed as parammeters and mask the secrets

  withSecretVault(secret: 'secret', user_var_name: 'my_user_env', pass_var_name: 'my_password_env'){
    //block
  }
*/
def call(Map params = [:], Closure body) {
  def secret = params?.secret
  def user_variable = params?.user_var_name
  def pass_variable = params?.pass_var_name

  if (!secret || !user_variable || !pass_variable) {
    error "withSecretVault: Missing variables"
  }

  def props = getVaultSecret(secret)
  if(props?.errors){
    error "withSecretVault: Unable to get credentials from the vault: " + props.errors.toString()
  }

  def user = props?.data?.user
  def password = props?.data?.password

  if(user == null || password == null){
    error "withSecretVault: was not possible to get authentication info"
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
