#!/usr/bin/env groovy

/**
  Get the current commit SHA from the .git folder.
  If the checkout was made by Jenkins, you would use the environment variable GIT_COMMIT.
  In other cases, you probably has to use this step.
  
  def sha = getGitCommitSha()
*/
def call() {
  def sha = sh script: "git rev-parse HEAD", returnStdout: true
  return "${sha}"
}