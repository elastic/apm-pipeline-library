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
  return the branch name, if we are in a branch, or the git ref, if we are in a PR.
  required to execute githubEnv step before.
*/
def call(Map args = [:]){
  def branchName = env.BRANCH_NAME
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('githubBranchRef: Environment not initialized, try to call githubEnv step before')
  }
  if(isPR()) {
    def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
    def token = getGithubToken()
    def pr = githubPrInfo(token: token, repo: repoName, pr: env.CHANGE_ID)
    branchName = "${pr.head.repo.owner.login}/${pr.head.ref}"
  }
  return branchName
}
