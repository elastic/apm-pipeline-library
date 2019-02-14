/**
  return the Github token.
*/
def call(Map params = [:]){
  def credentialsId = params.containsKey('credentialsId') ? params.credentialsId : "2a9602aa-ab9f-4e52-baf3-b71ca88469c7"
  def githubToken
  withCredentials([[
    variable: "GITHUB_TOKEN",
    credentialsId: credentialsId,
    $class: "StringBinding",
  ]]) {
    githubToken = env.GITHUB_TOKEN
  }
  return githubToken
}
