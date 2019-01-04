//readJSON show the JSON in the BO console output so it can not be used.
//https://issues.jenkins-ci.org/browse/JENKINS-54248
import net.sf.json.JSON
import net.sf.json.JSONObject
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
    props = getVaultSecretObject(env.VAULT_ADDR, secret, token)
  }
  return props
}

def getVaultToken(addr, roleId, secretId){
  def tokenJson = httpRequest(url: "${addr}/v1/auth/approle/login",
    method: "POST",
    headers: ["Content-Type": "application/json"],
    data: "{'role_id':'${roleId}','secret_id':'${secretId}'}")
  def obj = toJSON(tokenJson);
  if(!(obj instanceof JSONObject) || !(obj.auth instanceof JSONObject) || obj.auth.client_token == null){
    error("getVaultSecret: Unable to get the token.")
  }
  return obj.auth.client_token
}

def getVaultSecretObject(addr, secret, token){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_SECRET', password: secret], 
    [var: 'VAULT_TOKEN', password: token],
    [var: 'VAULT_ADDR', password: addr],
    ]]) {
    def retJson = httpRequest(url: "${addr}/v1/secret/apm-team/ci/${secret}",
      headers: ["X-Vault-Token": "${token}"])
    def obj = toJSON(retJson);
    if(!(obj instanceof JSONObject)){
      error("getVaultSecret: Unable to get the secret.")
    }
    return obj
  }
}

def toJSON(text){
  def obj = null
  if(text != null){
    try {
      obj = JSONSerializer.toJSON(text?.trim());
    } catch(e){
      //NOOP
      log(level: 'DEBUG', text: "getVaultSecret: Unable to Parsing JSON: ${e?.message}" )
    }
  }
  return obj
}
