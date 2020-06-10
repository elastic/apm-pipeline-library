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
  Search in the current Pull Request context the latest comment from the given list of 
  users and pattern to match with.

  // Return the comment that matches the pattern '<!--foo-->' and the onwer of the comment is
  //  elasticmachine
  githubPrLatestComment(pattern: '<!--foo-->', users: [ 'elasticmachine' ])
*/
def call(Map args = [:]){
  def pattern = args.containsKey('pattern') ? args.pattern : error('githubPrLatestComment: pattern parameter is required')
  def users = args.containsKey('users') ? args.users : []
  if (isPR()) {
    def token = getGithubToken()
    def url = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/issues/${env.CHANGE_ID}/comments"
    def comments = githubApiCall(token: token, url: url)
    return comments.reverse().find { comment ->
      if (users) {
        users.find { it == comment.user.login } && comment.body =~ "${pattern}"
      } else {
        comment.body =~ "${pattern}"
      }
    }
  } else {
    log(level: 'WARN', text: 'githubPrLatestComment: is only available for PRs.')
  }
}
