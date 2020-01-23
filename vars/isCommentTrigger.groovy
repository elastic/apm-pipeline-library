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

import com.cloudbees.groovy.cps.NonCPS
/**
  Check it the build was triggered by a comment in GitHub.

  def commentTrigger = isCommentTrigger()
*/

def call(){
  def found = false
  def data = getCommentData()
  log(level: 'DEBUG', text: "isCommentTrigger: ${data.toMapString()}")
  if (data) {
    log(level: 'DEBUG', text: 'isCommentTrigger: set some environment variables with the comments and so on')
    env.GITHUB_COMMENT = data.comment
    env.BUILD_CAUSE_USER = data.user

    def token = getGithubToken()
    def user = githubApiCall(token: token, url: "https://api.github.com/users/${env.BUILD_CAUSE_USER}")
    log(level: 'DEBUG', text: 'isCommentTrigger: only users under the elastic organisation are allowed.')

    // Let's support two different validations, the initial one consist on searching for the user's company,
    // otherwise let's search for all the organisations that the user is belong to.
    if (user && user.company?.trim()) {
      found = '@elastic'.equals(user?.company?.trim())
    } else {
      log(level: 'DEBUG', text: "Let's fallback with the orgs entrypoint")
      def orgs = githubApiCall(token: token, url: "https://api.github.com/users/${env.BUILD_CAUSE_USER}/orgs")
      found = (orgs.find { it?.login?.equals('elastic') } != null)
    }
  }
  return found
}

@NonCPS
def getCommentData() {
  def data = [:]
  def triggerCause = currentBuild.rawBuild.getCauses().find { it.getClass().getSimpleName().equals('IssueCommentCause') }
  if (triggerCause) {
    data = [ comment: triggerCause.getComment(), user: triggerCause.getUserLogin() ]
  }
  return data
}
