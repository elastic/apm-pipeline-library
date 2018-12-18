/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

/**
  return the branch name, if we are in a branch, or the git ref, if we are in a PR.
*/
def getBranchRef(){
  def branchName = env.BRANCH_NAME
  if (env.CHANGE_ID) {
    def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
    def token = getGithubToken()
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'GITHUB_TOKEN', password: "${token}"], 
      ]]) {
      def prJson = sh(
        script: """#!/bin/bash
        set +x
        curl -s -H 'Authorization: token ${token}' 'https://api.github.com/repos/${repoName}/pulls/${env.CHANGE_ID}'
        """,
        returnStdout: true
      )
      def pr = readJSON(text: prJson)
      branchName = "${pr.head.repo.owner.login}/${pr.head.ref}"
    }
  }
  return branchName
}

def call(Map params = [:]){
  def repo = params?.repo
  def basedir = params.containsKey('basedir') ? params.basedir : "."
  
  if(!repo){
    echo "Codecov: No repository specified."
    return
  }
  
  def token = getVaultSecret("${repo}-codecov")?.data?.value
  if(!token){
    echo "Codecov: Repository not found: ${repo}"
    return
  }
  
  dir(basedir){
    echo "Codecov: Getting branch ref..."
    def branchName = getBranchRef()
    if(branchName == null){
      error "Codecov: was not possible to get the branch ref"
    }
    echo "Codecov: Sending data..."
    // Set some env variables so codecov detection script works correctly
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
      [var: 'CODECOV_TOKEN', password: "${token}"], 
      ]]) {
      withEnv([
        "ghprbPullId=${env.CHANGE_ID}",
        "GIT_BRANCH=${branchName}",
        "CODECOV_TOKEN=${token}"]) {
        sh '''#!/bin/bash
        set -x
        curl -s -o codecov.sh https://codecov.io/bash
        bash codecov.sh || echo "codecov exited with $?"
        '''
      }
    }
  }
}
