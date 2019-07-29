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
  Get the current git repository url from the .git folder.
  If the checkout was made by Jenkins, you would use the environment variable GIT_URL.
  In other cases, you probably has to use this step.

  def repoUrl = getGitRepoURL()
*/
def call() {
  if(!isUnix()){
    error('getGitRepoURL: windows is not supported yet.')
  }
  def repoUrl = sh(label: 'Get repo URL', script: "git config --get remote.origin.url", returnStdout: true)?.trim()
  return "${repoUrl}"
}
