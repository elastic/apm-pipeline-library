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
  Configure the git release context to run the body closure.
  
  withGitRelease() {
    // block
  }

  NOTE: This particular implementation requires to checkout with the step gitCheckout
*/
def call(Map params = [:], Closure body) {
  if(!isUnix()){
    error('withGitRelease: windows is not supported yet.')
  }

  // Validate the git required context
  if(!env.GIT_BASE_COMMIT?.trim()){
    error('withGitRelease: GIT_BASE_COMMIT has not been set, either the `gitCheckout` or `githubEnv` steps have not been executed')
  }
  if(!env.GITHUB_USER?.trim() || !env.GITHUB_TOKEN?.trim()){
    error('withGitRelease: GITHUB_USER / GITHUB_TOKEN have not been set')
  }

  // Worth to dobule mask the sensitive credentials
  withEnvMask(vars: [[var: 'GITHUB_USER', password: "${env.GITHUB_USER}"],
                     [var: 'GITHUB_TOKEN', password: "${env.GITHUB_TOKEN}"]]){
    try {                       
      sh(label: 'Setup git release', script: libraryResource('scripts/setup-git-release.sh'))
      body()
    } catch (err) {
      error "withGitRelease: error ${err}"
      throw err
    } finally {
      // Rollback to the previous release context
      sh(label: 'Rollback git context', script: """
        git config remote.origin.url "https://github.com/${env.ORG_NAME}/${env.REPO_NAME}.git"
        git config remote.upstream.url "https://github.com/${env.ORG_NAME}/${env.REPO_NAME}.git"
      """)
    }
  }
}
