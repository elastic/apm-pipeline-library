/**
return the Github token.
*/
def call(Map params = [:]){
  def githubToken
  withCredentials([[
    variable: "GITHUB_TOKEN",
    credentialsId: "2a9602aa-ab9f-4e52-baf3-b71ca88469c7",
    $class: "StringBinding",
  ]]) {
    githubToken = env.GITHUB_TOKEN
  }
  return githubToken
}