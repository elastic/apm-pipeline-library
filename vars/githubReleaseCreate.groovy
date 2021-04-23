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
Create a GitHub release for a project

  Example invocation:

    gitHubReleaseCreate(
      'v1.0.0',         // Tag to base the release on
      'v1.0.0',         // Name of the release
      'This release introdces vegan tacos!', // Release description
      false,            // Do not publish as a draft. Make the release public.
      false,            // Do not publish as a prerelease.
    )

To learn more about what individual flags mean, please visit the GitHub
documentation page on releases:

  https://developer.github.com/v3/repos/releases/#create-a-release
*/

def call(Map args = [:]){
  // Gather variables and verify them
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('gitHubReleaseCreate: Environment not initialized. Try to call githubEnv step before')
  }
  def branchName = env.BRANCH_NAME

  def token = getGithubToken()

  def tagName =  args.get('tagName', "${BUILD_TAG}")
  def releaseName = args.get('releaseName', '')
  def body = args.get('body', '')
  def draft = args.get('draft', false)
  def preRelease = args.get('preRelease', false)

  // Construct GitHub API URL
  def apiURL = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/releases"

  /* Construct the paramaters for the GitHub API call

  Examnple from documentation:
  https://developer.github.com/v3/repos/releases/#create-a-release

        {
        "tag_name": "v1.0.0",
        "target_commitish": "master",
        "name": "v1.0.0",
        "body": "Description of the release",
        "draft": false,
        "prerelease": false
      }
  */

  def release_params = [
  "tag_name": tagName,
  "target_commitish": "master", // Hardcoded currently
  "name": releaseName,
  "body": body,
  "draft": draft,
  "prerelease": preRelease
  ]

  // Call the GitHub API
  ret = githubApiCall(token: token, url: apiURL, data: release_params)
  return ret
}
