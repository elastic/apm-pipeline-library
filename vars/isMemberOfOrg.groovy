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
  Check if the given GitHub user is member of the given GitHub org.

  whenTrue(isMemberOfOrg(user: 'my-user')) {
      //...
  }

  whenTrue(isMemberOfOrg(user: 'my-user')) {
      //...
  }

  // using another organisation
  whenTrue(isMemberOfOrg(user: 'my-user', org: 'acme')) {
      //...
  }
*/

def call(Map args = [:]) {
  def user = args.containsKey('user') ? args.user : error('isMemberOfOrg: user parameter is required')
  def org = args.containsKey('org') ? args.org : 'elastic'
  def token = getGithubToken()
  def found = false
  try {
    def membershipResponse = githubApiCall(token: token,
                                           allowEmptyResponse: true,
                                           url: "https://api.github.com/orgs/${org}/members/${user}")
    // githubApiCall returns either a raw output or an error message if so it means the user is not a member.
    found = membershipResponse.message?.trim() ? false : true
  } catch(err) {
    // Then it means 404 errorcode.
    // See https://developer.github.com/v3/orgs/members/#response-if-requester-is-an-organization-member-and-user-is-not-a-member
    found = false
  }
  return found
}
