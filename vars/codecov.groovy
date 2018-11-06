/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

/**
return the Github token.
*/
def getGithubToken(){
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

/**
  return the branch name, if we are in a branch, or the git ref, if we are in a PR.
*/
def getBranchRef(){
  def branchName = env.BRANCH_NAME
  if (env.CHANGE_ID) {
    def repoUrl = getGitRepoURL()
    def repoName = "${ORG_NAME}/${REPO_NAME}"
    def token = getGithubToken()
    def prJson = sh(
      script: "curl -H 'Authorization: token ${token}' https://api.github.com/repos/${repoName}/pulls/${CHANGE_ID}",
      returnStdout: true
    )
    def pr = readJSON(text: prJson)
    branchName = "${pr.head.repo.owner.login}/${pr.head.ref}"
  }
  return branchName
}

def call(repo=null) {
  if(!repo){
    echo "Codecov: No repository specified."
    return
  }
  
  def token = getVaultSecret("${repo}-codecov")?.data?.value
  if(!token){
    echo "Codecov: Repository not found: ${repo}"
    return
  }
  
  echo "Codecov: Sending data..."
  def branchName = getBranchRef()
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
