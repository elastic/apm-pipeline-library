#!/usr/bin/env groovy

/**
  Get the current git repository url from the .git folder.
  If the checkout was made by Jenkins, you would use the environment variable GIT_URL.
  In other cases, you probably has to use this step.
  
  def repoUrl = getGitRepoURL()
*/
def call() {
  sh "git config --get remote.origin.url > .git/remote-url"
  return readFile(".git/remote-url").trim()
}