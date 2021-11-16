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

This script is designed to be used in conjunction with githubReleaseCreate().
The normal workflow when using the two scripts together is to first create a
release with githubReleaseCreate() and setting the `draft` flag to `true`. Then,
once a human reviews the proposed release, this script can then be used to
make the release public.

If githubReleaseCreate() was called with the `draft` flag set to `false`,
this has no effect.

The `id` field required by this script is accessible in the return from
githubReleaseCreate(). See documentation for that script for more details about
the data structure of the return.

  Example invocation:

    githubReleasePublish(
      id: '1',                // Release ID
      name: 'Release v1.0.0', // Release name
    )

To learn more about what individual flags mean, please visit the GitHub
documentation page on releases:

  https://developer.github.com/v3/repos/releases/#create-a-release
**/

def call(Map args = [:]){
  // Gather variables and verify them
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('githubReleasePublish: Environment not initialized. Try to call githubEnv step before')
  }

  log(level: 'INFO', text: 'githubReleasePublish: just been called.')

  def token = getGithubToken()

  def id = args.containsKey('id') ? args.id : error('githubReleasePublish: id parameter is required.')
  def name = args.containsKey('name') ? args.id : error('githubReleasePublish: name is required.')

  // Construct GitHub API URL
  def apiURL = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/releases/${id}"

  def release_params = [
    "draft": false
  ]

  // Call the GitHub API
  ret = githubApiCall(token: token, url: apiURL, data: release_params)
  return ret
}
