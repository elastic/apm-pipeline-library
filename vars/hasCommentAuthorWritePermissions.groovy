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
  Check if the author of a GitHub comment has admin or write permissions in the repository.
*/
def call(Map args = [:]){
  def repoName = args.containsKey('repoName') ? args.repoName : error('hasCommentAuthorWritePermissions: repoName params is required')
  def commentId = args.containsKey('commentId') ? args.commentId : error('hasCommentAuthorWritePermissions: commentId params is required')
  if (repoName.contains('/')) {
    def token = getGithubToken()
    def url = "https://api.github.com/repos/${repoName}/issues/comments/${commentId}"
    def comment = githubApiCall(token: token, url: url, noCache: true)
    def json = githubRepoGetUserPermission(token: token, repo: repoName, user: comment?.user?.login)
    return json?.permission?.trim() == 'admin' || json?.permission?.trim() == 'write'
  }
  error('hasCommentAuthorWritePermissions: invalid repository format, please use the format <org>/<repo> (elastic/beats).')
}
