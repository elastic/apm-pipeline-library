import groovy.transform.Field

/**
https://github.com/docker/jenkins-pipeline-scripts/blob/master/vars/codecov.groovy
*/

@Field def tokens = [:]

def call(Map params = [:]){
  def repo = params?.repo
  def basedir = params.containsKey('basedir') ? params.basedir : "."
  def flags = params.containsKey("flags") ? params.flags : ""
  def secret = params?.secret

  if(!repo){
    log(level: 'WARN', text: "Codecov: No repository specified.")
    return
  }

  def token = null
  if(tokens["${repo}"] == null){
    log(level: 'DEBUG', text: "Codecov: get the token from Vault.")
    if(secret != null){
      token = getVaultSecret(secret: secret)?.data?.value
    } else {
      /** TODO remove it is only for APM projects */
      token = getVaultSecret("${repo}-codecov")?.data?.value
    }
    tokens["${repo}"] = token
  } else {
    log(level: 'DEBUG', text: "Codecov: get the token from cache.")
    token = tokens["${repo}"]
  }
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
        sh label: 'Send report to Codecov', script: """#!/bin/bash
        set -x
        curl -s -o codecov.sh https://codecov.io/bash
        bash codecov.sh ${flags} || echo "codecov exited with \$?"
        """
      }
    }
  }
}
