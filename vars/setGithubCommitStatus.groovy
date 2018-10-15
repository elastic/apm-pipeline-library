#!/usr/bin/env groovy

/**
  https://plugins.jenkins.io/github
  https://embeddedartistry.com/blog/2017/12/28/jenkins-generating-blue-ocean-urls
  Set the commit status on GitHub with an status passed as parameter or SUCCESS by default.

  setGithubCommitStatus(
    repoUrl: "${GIT_URL}",
    commitSha: "${GIT_COMMIT}",
    message: 'Build result.',
    state: "SUCCESS"
  )

  setGithubCommitStatus()

  setGithubCommitStatus(message: 'Build result.', state: "FAILURE")
  
  setGithubCommitStatus(message: 'Build result.', state: "UNSTABLE")
*/
def call(Map params = [:]) {
  def repoUrl = params.containsKey('repoUrl') ? params.repoUrl : getGitRepoURL()
  def commitSha = params.containsKey('commitSha') ? params.commitSha : getGitCommitSha()
  def message = params.containsKey('message') ? params.message : 'Build result.'
  def state = params.containsKey('state') ? params.state : 'SUCCESS'
  
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
      errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
      //errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}