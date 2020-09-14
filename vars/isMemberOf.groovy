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
  Check if the given GitHub user is member of the given GitHub team.

  whenTrue(isMemberOf(user: 'my-user', team: 'my-team'))

  NOTE: https://developer.github.com/v3/teams/members/#get-team-membership-for-a-user
*/

def call(Map args = [:]) {
  def user = args.containsKey('user') ? args.user : error('isMemberOf: user param is required')
  def team = args.containsKey('team') ? args.team : error('isMemberOf: team param is required')
  def org = args.containsKey('org') ? args.org : 'elastic'

  try {
    def token = getGithubToken()
    def url = "https://api.github.com/orgs/${org}/teams/${team}/memberships/${user}"
    def membershipResponse = githubApiCall(token: token, allowEmptyResponse: true, url: url)
    if (membershipResponse?.state) {
      return membershipResponse?.state?.equals('active')
    } else {
      return false
    }
  } catch(err) {
    return false
  }
  return false
}
