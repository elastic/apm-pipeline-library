/**
  Delete a git TAG named ${BUILD_TAG} and push it to the git repo.
  It requires to initialise the pipeline with github_enterprise_constructor() first.

  gitDeleteTag()
*/

def call() {
  withCredentials([
    usernamePassword(
      credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', 
      passwordVariable: 'GIT_PASSWORD', 
      usernameVariable: 'GIT_USERNAME')]) {
    sh("git fetch --tags")
    sh("git tag -d '${BUILD_TAG}'")
    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${ORG_NAME}/${REPO_NAME}.git --tags')
  }
}