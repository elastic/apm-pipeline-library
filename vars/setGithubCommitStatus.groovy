// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

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
def call(Map args = [:]) {
  def repoUrl = args.containsKey('repoUrl') ? args.repoUrl : getGitRepoURL()
  def commitSha = args.containsKey('commitSha') ? args.commitSha : getGitCommitSha()
  def message = args.containsKey('message') ? args.message : 'Build result.'
  def state = args.containsKey('state') ? args.state : 'SUCCESS'

  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
      errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
      //errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}
