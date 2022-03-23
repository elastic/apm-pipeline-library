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
  Whether the given branch is an active branch in the unified release

  whenTrue(isBranchUnifiedReleaseAvailable('main')) {
    //
  }
*/
def call(String branch){
  def branchName = branch
  if (branch.equals('main')) {
    branchName = 'master'
  }
  def fileName = "${branchName}.gradle"
  def token = getGithubToken()
  def ret = githubApiCall(token: token,
                          method: 'GET',
                          failNever: true,
                          allowEmptyResponse: true,
                          url:"https://api.github.com/repos/elastic/infra/contents/cd/release/release-manager/${fileName}")
  return (ret?.name?.trim() == fileName) ? true : false
}
