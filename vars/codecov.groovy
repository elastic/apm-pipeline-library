/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

def call(Map params = [:]){
  def repo = params?.repo
  def basedir = params.containsKey('basedir') ? params.basedir : "."
  
  if(!repo){
    log(level: 'WARN', text: "Codecov: No repository specified.")
    return
  }
  
  def token = getVaultSecret("${repo}-codecov")?.data?.value
  if(!token){
    log(level: 'WARN', text: "Codecov: Repository not found: ${repo}")
    return
  }
  
  dir(basedir){
    log(level: 'INFO', text: "Codecov: Getting branch ref...")
    def branchName = githubBranchRef()
    if(branchName == null){
      error "Codecov: was not possible to get the branch ref"
    }
    log(level: 'INFO', text: "Codecov: Sending data...")
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
