#!/usr/bin/env groovy

/**
  Grab a secret from the vault, define some environment variables with the
  secrets and mask the secrets

  withSecretVault(secret: 'secret', data: [APP_USER: 'my_variable', APP_PASSWORD: 'my_password']){
    //block
  }
*/
def call(Map params = [:], Closure body) {
  def secret = params.get('secret')
  def user_variable = params.get('data').get('APP_USER')
  def pass_variable = params.get('data').get('APP_PASSWORD')

  def jsonValue = getVaultSecret(secret: "${secret}")
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
        [var: "${user_variable}", password: jsonValue.data.user],
        [var: "${pass_variable}", password: jsonValue.data.password],
  ]]) {
    withEnv([
      "${user_variable}=${jsonValue.data.user}",
      "APP_PASSWORD=${jsonValue.data.password}"]) {
        body()
    }
  }
}
