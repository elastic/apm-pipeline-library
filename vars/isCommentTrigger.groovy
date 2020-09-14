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
  if (env.GITHUB_COMMENT_AUTHOR && env.GITHUB_COMMENT) {
    log(level: 'DEBUG', text: 'isCommentTrigger: only users under the elastic organisation are allowed.')
    def token = getGithubToken()

    try {
      def membershipResponse = githubApiCall(token: token, allowEmptyResponse: true,
                                            url: "https://api.github.com/orgs/elastic/members/${env.GITHUB_COMMENT_AUTHOR}")
      // githubApiCall returns either a raw ouput or an error message if so it means the user is not a member.
      found = membershipResponse.message?.trim() ? false : true
    } catch(err) {
      log(level: 'WARN', text: "isCommentTrigger: only users under the Elastic organisation are allowed. Message: See ${err.toString()}")
      // Then it means 404 errorcode.
      // See https://developer.github.com/v3/orgs/members/#response-if-requester-is-an-organization-member-and-user-is-not-a-member
      found = false
    }
  }
  return found
}
