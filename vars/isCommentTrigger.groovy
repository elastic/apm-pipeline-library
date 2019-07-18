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
  Check it the build was triggered by a comment in GitHub.

  def commentTrigger = isCommentTrigger()
*/
def call(){
  def triggerCause = currentBuild.rawBuild.getCauses().find { cause ->
    log(level: 'DEBUG', text: "isCommentTrigger: ${cause.getClass().getSimpleName()}")
    return cause.getClass().getSimpleName().equals('IssueCommentCause')
  }
  def ret = triggerCause != null
  log(level: 'DEBUG', text: "isCommentTrigger: ${ret}")
  if(ret){
    env.GITHUB_COMMENT = triggerCause.getComment()
    env.BUILD_CAUSE_USER = triggerCause.getUserLogin()
    //Only Elastic users are allowed
    def token = getGithubToken()
    def user = githubApiCall(token: token, url: "https://api.github.com/users/${env.BUILD_CAUSE_USER}")
    ret = '@elastic'.equals(user?.company)
  }
  return ret
}
