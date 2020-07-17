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
Add a comment or edit an existing comment in the GitHub Pull Request
using the GitHub API.

  // create a new comment
  githubTraditionalPrComment(message: 'foo bar')

  // edit an existing comment
  githubTraditionalPrComment(message: 'foo bar', id: 12323)

  _NOTE_: To edit the existing comment is required these environment variables:
          - `CHANGE_ID`
          - `ORG_NAME`
          - `REPO_NAME`
*/
def call(Map args = [:]){
  def id = "${args.get('id', '')}"
  def message = args.containsKey('message') ? args.message : error('githubTraditionalPrComment: message parameter is required')

  if (isPR()) {
    def token = getGithubToken()
    def url = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/issues/${env.CHANGE_ID}/comments"
    def method = 'POST'
    if(id.trim()) {
       url = "${url}/${id}"
       method = 'PATCH'
    }
    def comment = githubApiCall(token: token, url: url, data: """{ "body": "${message}"} """, method: method)
    return comment.id
  } else {
    log(level: 'WARN', text: 'githubTraditionalPrComment: is only available for PRs.')
  }
}
