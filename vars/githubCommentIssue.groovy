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
  Comment an existing GitHub issue

  NOTE: It uses hub. No supported yet by gh see https://github.com/cli/cli/issues/517

  // Add a new comment to the issue 123 using the REPO_NAME and ORG_NAME env variables
  githubCommentIssue(id: 123, body: 'My new comment')

  // Add a new comment to the issue 123 from foo/repo
  githubCommentIssue(org: 'foo', repo: 'repo', id: 123, body: 'My new comment')
*/

def call(Map args = [:]) {
  if(!isUnix()) {
    error 'githubCommentIssue: windows is not supported yet.'
  }
  def comment = args.containsKey('comment') ? normalise(args.comment) : error('githubCommentIssue: comment argument is required.')
  def credentialsId = args.get('credentialsId', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7')
  def id = args.containsKey('id') ? args.id : error('githubCommentIssue: id argument is required.')
  def org = args.containsKey('org') ? args.org : env.ORG_NAME
  def repo = args.containsKey('repo') ? args.repo : env.REPO_NAME

  if (!org?.trim() || !repo?.trim()) {
    error("githubCommentIssue: org/repo are empty ${org}/${repo}. Please use the org and repo arguments or set the ORG_NAME and REPO_NAME variables (if you use the gitCheckout step then it should be there)")
  }
  withCredentials([string(credentialsId: "${credentialsId}", variable: 'GITHUB_TOKEN')]) {
    return sh(label: 'Comment GitHub issue', script: "hub api repos/${org}/${repo}/issues/${id}/comments -f body='${comment}'", returnStdout: true).trim()
  }
}

/**
* Ensure ' is replaced to avoid any issues when using the markdown templating.
*/
def normalise(v) {
  if (v instanceof String) {
    return v?.toString()?.replaceAll("'",'"')
  }
  return v
}
