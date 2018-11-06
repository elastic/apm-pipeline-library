/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/
import groovy.transform.Field
import org.kohsuke.github.GitHub

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
    // Need to get name from url, supports these variants:
    //  git@github.com:docker/docker.git -> docker/docker
    //  git://github.com/docker/docker.git -> docker/docker
    //  https://github.com/docker/docker.git -> docker/docker
    //  ssh://git@github.com/docker/docker.git -> docker/docker
    // 1. split on colon, take the last part.
    // 2. split that on slash, take the last 2 parts and rejoin them with /.
    // 3. remove .git at the end
    // 4. ta-da
    def repoName = repoUrl.split(":")[-1].split("/")[-2..-1].join("/").replaceAll(/\.git$/, '')
    def gh = GitHub.connectUsingOAuth(getGithubToken())
    def pr = gh.getRepository(repoName).getPullRequest(env.CHANGE_ID.toInteger())
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
