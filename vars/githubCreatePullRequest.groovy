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
  Create a Pull Request in GitHub as long as the command runs in the git repo and
  there are commited changes

  githubCreatePullRequest(title: 'Foo')
*/

def call(Map params = [:]) {
  if(!isUnix()) {
    error 'githubCreatePullRequest: windows is not supported yet.'
  }
  def title = params.containsKey('title') ? """--message '${params.title}'""" : error('githubCreatePullRequest: title argument is required.')
  def description = params.containsKey('description') ? """--message '${params.description}'""" : ''
  def assign = params.containsKey('assign') ? "--assign ${params.assign}" : ''
  def reviewer = params.containsKey('reviewer') ? "--reviewer ${params.reviewer}" : ''
  def milestone = params.containsKey('milestone') ? "--milestone ${params.milestone}" : ''
  def labels = params.containsKey('labels') ? "--labels ${params.labels}" : ''
  def draft = params.containsKey('draft') ? params.draft : false
  def base = params.containsKey('base') ? "--base ${params.base}" : ''
  def credentialsId = params.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')

  def draftFlag = draft ? '--draft' : ''
  withCredentials([
    usernamePassword(credentialsId: "${credentialsId}", passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')
  ]) {
    sh(label: 'Config remote', script: "git config remote.origin.url https://${GITHUB_USER}:${GITHUB_TOKEN}@github.com/${env.ORG_NAME}/${env.REPO_NAME}.git")
    try {
      sh(label: 'Create GitHub issue', script: "hub pull-request --push ${title} ${description} ${draftFlag} ${assign} ${reviewer} ${labels} ${milestone} ${base}")
    } catch(e) {
      error "githubCreatePullRequest: error ${e}"
    } finally {
      sh(label: 'Config remote', script: "git config remote.origin.url https://github.com/${env.ORG_NAME}/${env.REPO_NAME}.git")
    }
  }
}
