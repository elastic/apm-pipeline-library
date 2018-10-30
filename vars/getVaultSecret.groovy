/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret('secret-name')
*/
def call(secret) {
  def props = null
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_ROLE_ID', password: '35ad5918-eab7-c814-f8be-a305c811732e'], 
    [var: 'VAULT_SECRET_ID', password: '95d18733-44b5-53c3-89c5-91e27b29be4f'], 
    [var: 'VAULT_ADDR', password: 'https://secrets.elastic.co:8200'], 
    [var: 'VAULT_SECRET', password: "${secret}"], 
    ]]) {
     def retJson = sh(returnStdout:true, script: '''#!/bin/bash
     set +x
     VAULT_TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -L -d "{\"role_id\":\"${VAULT_ROLE_ID}\",\"secret_id\":\"${VAULT_SECRET_ID}\"}" ${VAULT_ADDR}/v1/auth/approle/login | jq -r '.auth.client_token' )
     curl -s -L -H "X-Vault-Token:${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/apm-team/ci/${VAULT_SECRET}
     ''')
     props = readJSON(text: retJson)
   }
   return props
}