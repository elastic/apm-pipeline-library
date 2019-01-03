import net.sf.json.JSON
import net.sf.json.JSONSerializer

/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret('secret-name')
*/
def call(secret) {
  if(secret == null){
    error("getVaultSecret: No valid secret to looking for.")
  }
  def props = null
  log(level: 'INFO', text: "getVaultSecret: Getting secrets")
  withCredentials([
    string(credentialsId: 'vault-addr', variable: 'VAULT_ADDR'),
    string(credentialsId: 'vault-role-id', variable: 'VAULT_ROLE_ID'),
    string(credentialsId: 'vault-secret-id', variable: 'VAULT_SECRET_ID')]) {
      def token = getVaultToken(env.VAULT_ADDR, env.VAULT_ROLE_ID, env.VAULT_SECRET_ID)
      if(token == null){
        error("getVaultSecret: Unable to get the token.")
      }

      props = getVaultSecretObject(env.VAULT_ADDR, secret, token)
      if(props == null){
        error("getVaultSecret: Unable to get the secret.")
      }
  }
  return props
}

def getVaultToken(addr, roleId, secretId){
  def tokenJson =  sh(returnStdout: true, script: """#!/bin/bash
  set +x -euo pipefail
  curl -s -X POST -H "Content-Type: application/json" -L -d '{"role_id":"${roleId}","secret_id":"${secretId}"}' ${addr}/v1/auth/approle/login
  """)
  def obj = JSONSerializer.toJSON(tokenJson?.trim());
  return obj?.auth?.client_token
}

def getVaultSecretObject(addr, secret, token){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_SECRET', password: secret], 
    [var: 'VAULT_TOKEN', password: token],
    [var: 'VAULT_ADDR', password: addr],
    ]]) {
      def retJson = sh(returnStdout: true, script: """#!/bin/bash
      set +x -euo pipefail
      curl -s -L -H "X-Vault-Token:${token}" ${addr}/v1/secret/apm-team/ci/${secret}
      """)
      return readJSON(text: retJson)
    }
}