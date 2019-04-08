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
  def data = jsonValue.containsKey('data') ? jsonValue.data : error("dockerLogin: No valid data in secret.")
  def dockerUser = data.containsKey('user') ? data.user : error("dockerLogin: No valid user in secret.")
  def dockerPassword = data.containsKey('password') ? data.password : error("dockerLogin: No valid password in secret.")

  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'DOCKER_USER', password: dockerUser],
    [var: 'DOCKER_PASSWORD', password: dockerPassword],
    ]]) {
    sh(label: "Docker login", script: """
    set +x
    host ${registry} 2>&1 > /dev/null
    docker login -u '${dockerUser}' -p '${dockerPassword}' '${registry}' 2>/dev/null
    """)
  }
}
