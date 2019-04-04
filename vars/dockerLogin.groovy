/**
  Login to hub.docker.com with an authentication credentials from a Vault secret.
  The vault secret contains `user` and `password` fields with the authentication details.

  dockerLogin(secret: 'secret/team/ci/secret-name')
  dockerLogin(secret: 'secret/team/ci/secret-name', registry: "docker.io")
*/
def call(Map params = [:]){
  echo "Is me not you"
  def secret = params.containsKey('secret') ? params.secret : error("dockerLogin: No valid secret to looking for.")
  def registry = params.containsKey('registry') ? params.registry : "docker.io"
  def jsonValue = getVaultSecret(secret: secret)
  log(level: "DEBUG", text: "secret: ${jsonValue.toString()}")
  def user = jsonValue.data.user =! null ? jsonValue.data.user : error("dockerLogin: No valid user in secret.")
  def password = jsonValue.data.password =! null ? jsonValue.data.password : error("dockerLogin: No valid password in secret.")
  log(level: "DEBUG", text: "auth: ${user} ${password}")
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'DOCKER_USER', password: user],
    [var: 'DOCKER_PASSWORD', password: password],
    ]]) {
    log(level: "DEBUG", text: "auth: ${user} ${password}")
    sh(label: "Docker login", script: "docker login -u ${user} -p ${password} ${registry}")
  }
}
