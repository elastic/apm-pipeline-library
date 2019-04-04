/**
  Login to hub.docker.com with an authentication credentials from a Vault secret.
  The vault secret contains `user` and `password` fields with the authentication details.

  dockerLogin(secret: 'secret/team/ci/secret-name')
  dockerLogin(secret: 'secret/team/ci/secret-name', registry: "docker.io")
*/
def call(Map params = [:]){
  def secret = params.containsKey('secret') ? params.secret : error("dockerLogin: No valid secret to looking for.")
  def registry = params.containsKey('registry') ? params.registry : "docker.io"
  def jsonValue = getVaultSecret(secret: secret)
  log(level: "DEBUG", text: "secret: ${jsonValue.data.toString()}")
  def dockerUser = jsonValue.data.user =! null ? jsonValue.data.user : error("dockerLogin: No valid user in secret.")
  def dockerPassword = jsonValue.data.password =! null ? jsonValue.data.password : error("dockerLogin: No valid password in secret.")
  log(level: "DEBUG", text: "auth: ${jsonValue.data.user} ${jsonValue.data.password}")
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'DOCKER_USER', password: dockerUser],
    [var: 'DOCKER_PASSWORD', password: dockerPassword],
    ]]) {
    log(level: "DEBUG", text: "auth: ${dockerUser} ${dockerPassword}")
    sh(label: "Docker login", script: "docker login -u ${dockerUser} -p ${dockerPassword} ${registry}")
  }
}
