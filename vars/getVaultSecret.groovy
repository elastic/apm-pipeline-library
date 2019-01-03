/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret('secret-name')
*/
def call(secret) {
  def props = null
  def roleId = '35ad5918-eab7-c814-f8be-a305c811732e'
  def secretId = '95d18733-44b5-53c3-89c5-91e27b29be4f'
  def addr = 'https://secrets.elastic.co:8200'
  log(level: 'INFO', text: "getVaultSecret: Getting secrets")
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_ROLE_ID', password: roleId], 
    [var: 'VAULT_SECRET_ID', password: secretId], 
    [var: 'VAULT_ADDR', password: addr], 
    [var: 'VAULT_SECRET', password: "${secret}"], 
    ]]) {
      def tokenJson =  sh(returnStdout: true, script: """#!/bin/bash
      set +x -euo pipefail
      curl -s -X POST -H "Content-Type: application/json" -L -d '{"role_id":"${roleId}","secret_id":"${secretId}"}' ${addr}/v1/auth/approle/login
      """)
      def token = readJSON(text: tokenJson)?.auth?.client_token
      
      if(token == null){
        error("getVaultSecret: Unable to get the token.")
      }
      
      wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'VAULT_TOKEN', password: token],
      ]]) {
        def retJson = sh(returnStdout: true, script: """#!/bin/bash
        set +x -euo pipefail
        curl -s -L -H "X-Vault-Token:${token}" ${addr}/v1/secret/apm-team/ci/${secret}
        """)
        props = readJSON(text: retJson)
      }
   }
   return props
}