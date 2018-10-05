#!/usr/bin/env groovy

/**
  https://plugins.jenkins.io/github
  Update the commit status on GitHub with the current status of the build.
  
  updateGithubCommitStatus(
    repoUrl: "${GIT_URL}",
    commitSha: "${GIT_COMMIT}",
    message: 'Build result.'
  )
  
  updateGithubCommitStatus()
  
  updateGithubCommitStatus(message: 'Build result.')
*/
def call(Map params = [:]) {
  def repoUrl = params.containsKey('repoUrl') ? params.repoUrl : getGitRepoURL()
  def commitSha = params.containsKey('commitSha') ? params.commitSha : getGitCommitSha()
  def message = params.containsKey('message') ? params.message : 'Build result.'
 
  step([
    $class: 'GitHubCommitStatusSetter',
    reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
    commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
    //errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
    errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
    statusResultSource: [
      $class: 'ConditionalStatusResultSource',
      results: [
        [$class: 'BetterThanOrEqualBuildResult', result: 'SUCCESS', state: 'SUCCESS', message: message],
        [$class: 'BetterThanOrEqualBuildResult', result: 'FAILURE', state: 'FAILURE', message: message],
        [$class: 'AnyBuildResult', state: 'FAILURE', message: 'Loophole']
      ]
    ]
  ])
}