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
Takes a GitHub release that is written as a draft and makes it public.

If githubReleaseCreate() was called with the `draft` flag set to `false`,
this has no effect.

  Example invocation:

    githubReleasePublish(
      id: '1',         // Release ID
    )

To learn more about what individual flags mean, please visit the GitHub
documentation page on releases:

  https://developer.github.com/v3/repos/releases/#create-a-release
**/

def call(Map params = [:]){
  // Gather variables and verify them
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('gitHubReleaseCreate: Environment not initialized. Try to call githubEnv step before')
  }
  def branchName = env.BRANCH_NAME

  def token = getGithubToken()

  def id = params?.id

  // Construct GitHub API URL
  def apiURL = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/releases/${id}"

  def release_params = [
  "draft": true
  ]

  // Call the GitHub API
  ret = githubApiCall(token: token, url: apiURL, data: release_params)
  return ret
}
