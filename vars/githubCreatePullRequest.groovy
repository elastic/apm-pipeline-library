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

  def pullRequestUrl = githubCreatePullRequest(title: 'Foo', description: 'something')
*/

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'githubCreatePullRequest: windows is not supported yet.'
  }
  def titleValue = args.containsKey('title') ? args.title : error('githubCreatePullRequest: title argument is required.')
  def descriptionValue = args.get('description', '')
  def assign = args.containsKey('assign') ? "--assign ${args.assign}" : ''
  def reviewer = args.containsKey('reviewer') ? "--reviewer ${args.reviewer}" : ''
  def milestone = args.containsKey('milestone') ? "--milestone ${args.milestone}" : ''
  def labels = args.containsKey('labels') ? "--labels ${args.labels}" : ''
  def draft = args.containsKey('draft') ? args.draft : false
  def base = args.containsKey('base') ? "--base ${args.base}" : ''
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
  def force = args.containsKey('force') ? args.force : false
  def title = titleValue?.trim() ? """--message '${titleValue}'""" : ''
  def description = descriptionValue?.trim() ? """--message '${descriptionValue}'""" : ''

  def draftFlag = draft ? '--draft' : ''
  def forceFlag = force ? '--force' : ''
  def output = ''
  // Some corner cases with single quotes in the description or title
  if (descriptionValue.contains("'") || titleValue.contains("'")) {
    error('githubCreatePullRequest: single quotes are not allowed.')
  }

  withCredentials([
    usernamePassword(credentialsId: "${credentialsId}", passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')
  ]) {
    sh(label: 'Config remote with credentials', script: """
      ## To enable push with https+github-credentials
      remoteUrl=\$(git config remote.origin.url | sed "s#https://#https://${GITHUB_USER}:${GITHUB_TOKEN}@#g")
      git config remote.origin.url \${remoteUrl}
    """)
    try {
      output = sh(label: 'Create GitHub issue', returnStdout: true,
                  script: "hub pull-request --push ${title} ${description} ${draftFlag} ${assign} ${reviewer} ${labels} ${milestone} ${base} ${forceFlag}").trim()
      return output
    } catch(e) {
      error "githubCreatePullRequest: error ${e}"
    } finally {
      sh(label: 'Revert remote url', script: """
        ## To configure remote url as used to be
        remoteUrl=\$(git config remote.origin.url | sed "s#.*@#https://#g")
        git config remote.origin.url \${remoteUrl}
      """)
    }
  }
}
