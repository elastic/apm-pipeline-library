/**
  Create a git TAG named ${BUILD_TAG} and push it to the git repo.
  It requires to initialise the pipeline with github_enterprise_constructor() first.

  gitCreateTag()
*/

def call() {
  withCredentials([
    usernamePassword(
      credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
      passwordVariable: 'GIT_PASSWORD',
      usernameVariable: 'GIT_USERNAME')]) {
    sh(label: "Create tag ${BUILD_TAG}", script: "git tag -a '${BUILD_TAG}' -m 'Jenkins TAG ${RUN_DISPLAY_URL}'")
    sh(label: "Push tag ${BUILD_TAG}", script: 'git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${ORG_NAME}/${REPO_NAME}.git --tags')
  }
}
