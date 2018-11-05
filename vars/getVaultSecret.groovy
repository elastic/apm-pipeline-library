/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret('secret-name')
*/
def call(secret) {
  def props = null
  def roleId = '35ad5918-eab7-c814-f8be-a305c811732e'
  def secretId = '95d18733-44b5-53c3-89c5-91e27b29be4f'
  def addr = 'https://secrets.elastic.co:8200'
  echo "Getting secrets"
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_ROLE_ID', password: roleId], 
    [var: 'VAULT_SECRET_ID', password: secretId], 
    [var: 'VAULT_ADDR', password: addr], 
    [var: 'VAULT_SECRET', password: "${secret}"], 
    ]]) {
      def retJson = sh(returnStdout: true, script: """#!/bin/bash
      set +x -euo pipefail
      VAULT_TOKEN=\$(curl -s -X POST -H "Content-Type: application/json" -L -d '{"role_id":"${roleId}","secret_id":"${secretId}"}' ${addr}/v1/auth/approle/login | jq -r '.auth.client_token' )
      curl -s -L -H "X-Vault-Token:\${VAULT_TOKEN}" ${addr}/v1/secret/apm-team/ci/${secret}
      """)
      props = readJSON(text: retJson)
   }
   return props
}