/**
  Create a git TAG named ${BUILD_TAG} and push it to the git repo.
  gitCreateTag()
*/

def call() {
  /** TODO enable create tag
  https://jenkins.io/doc/pipeline/examples/#push-git-repo
  */
  sh("git tag -a '${BUILD_TAG}' -m 'Jenkins TAG ${BUILD_TAG} on ${RUN_DISPLAY_URL}'")
  sh("git push git@github.com:${ORG_NAME}/${REPO_NAME}.git --tags")
  /*
  withCredentials([usernamePassword(credentialsId: 'dca1b5a0-edbc-4d0e-bc0c-c38857c83a80', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh("git tag -a '${BUILD_TAG}' -m 'Jenkins TAG ${RUN_DISPLAY_URL}'")
    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${ORG_NAME}/${REPO_NAME}.git --tags')
  }*/
}