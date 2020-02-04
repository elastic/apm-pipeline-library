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

def call(Map params = [:]){
  // Gather variables and verify them
  if(env.ORG_NAME == null || env.REPO_NAME == null){
    error('gitHubReleaseCreate: Environment not initialized. Try to call githubEnv step before')
  }
  def branchName = env.BRANCH_NAME

  def token = getGithubToken()

  def tagName =  params.get('tagName', "${BUILD_TAG}")
  def releaseName = params.get('releaseName', '')
  def body = params.get('body', '')
  def draft = params.get('draft', false)
  def preRelease = params.get('preRelease', false)

  // Construct GitHub API URL
  def apiURL = "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/releases"

  // Call the GitHub API
  
