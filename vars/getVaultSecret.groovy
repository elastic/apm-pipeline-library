import net.sf.json.JSONObject

/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret(secret: 'secret/team/ci/secret-name')
*/
def call(Map params = [:]){
  def secret = params.containsKey('secret') ? params.secret : error("getVaultSecret: No valid secret to looking for.")
  return readSecret(secret)
}

/**
  Get a secret from the Vault.

  def jsonValue = getVaultSecret('secret-name')

  TODO remove it is only for APM projects
*/
def call(secret) {
  if(secret == null){
    error("getVaultSecret: No valid secret to looking for.")
  }
  secret = 'secret/apm-team/ci/' + secret
  return readSecret(secret)
}

def readSecret(secret){
  def props = null
  log(level: 'INFO', text: "getVaultSecret: Getting secrets")
  withCredentials([
    string(credentialsId: 'vault-addr', variable: 'VAULT_ADDR'),
    string(credentialsId: 'vault-role-id', variable: 'VAULT_ROLE_ID'),
    string(credentialsId: 'vault-secret-id', variable: 'VAULT_SECRET_ID')]) {
    def token = getVaultToken(env.VAULT_ADDR, env.VAULT_ROLE_ID, env.VAULT_SECRET_ID)
    props = getVaultSecretObject(env.VAULT_ADDR, secret, token)
    //we do not have permissions to revoke a token.
    //revokeToken(env.VAULT_ADDR, token)
  }
  return props
}

def getVaultToken(addr, roleId, secretId){
  def tokenJson = httpRequest(url: "${addr}/v1/auth/approle/login",
    method: "POST",
    headers: ["Content-Type": "application/json"],
    data: "{\"role_id\":\"${roleId}\",\"secret_id\":\"${secretId}\"}")
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
    def retJson = httpRequest(url: "${addr}/v1/${secret}",
      headers: ["X-Vault-Token": "${token}"])
    def obj = toJSON(retJson);
    if(!(obj instanceof JSONObject)){
      error("getVaultSecret: Unable to get the secret.")
    }
    return obj
  }
}

def revokeToken(addr, token){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'VAULT_TOKEN', password: token],
    [var: 'VAULT_ADDR', password: addr],
    ]]) {
    httpRequest(url: "${addr}/v1/auth/token/revoke",
      method: "POST",
      headers: [
        "Content-Type": "application/json",
        "X-Vault-Token": "${token}"
      ],
      data: "{\"token\":\"${token}\"}")
  }
}
