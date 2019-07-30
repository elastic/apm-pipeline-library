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
  Creates some environment variables to identified the repo and the change type (change, commit, PR, ...)

  githubEnv()
*/
def call(){
  if(!isUnix()){
    error('githubEnv: windows is not supported yet.')
  }
  if(!env?.GIT_URL){
    env.GIT_URL = getGitRepoURL()
  }

  def tmpUrl = env.GIT_URL

  if (env.GIT_URL.startsWith("git")){
    tmpUrl = tmpUrl - "git@github.com:"
  } else {
    tmpUrl = tmpUrl - "https://github.com/" - "http://github.com/"
  }

  def parts = tmpUrl.split("/")
  env.ORG_NAME = parts[0]
  env.REPO_NAME = parts[1] - ".git"

  if(!env?.GIT_SHA){
    env.GIT_SHA = getGitCommitSha()
  }

  getBaseCommit()

  if (env.CHANGE_TARGET){
    env.GIT_BUILD_CAUSE = "pr"
  } else {
    env.GIT_BUILD_CAUSE = sh (
      label: 'Get latest commits SHA',
      script: 'git rev-list HEAD --parents -1', // will have 2 shas if commit, 3 or more if merge
      returnStdout: true
    )?.split(" ").length > 2 ? "merge" : "commit"
  }

  log(level: 'INFO', text: "githubEnv: Found Git Build Cause: ${env.GIT_BUILD_CAUSE}")
}

def getBaseCommit(){
  def baseCommit = ''
  def latestCommit = getGitCommitSha()
  def previousCommit = sh(label: 'Get previous commit', script: "git rev-parse HEAD^", returnStdout: true)?.trim()

  if(env?.GIT_COMMIT == null){
    def isLatestCommitInRepo = sh(label: 'Check latest commit is in the repo', returnStatus: true, script: "git branch -r --contains ${latestCommit}")
    if(isLatestCommitInRepo){
      env.GIT_COMMIT = latestCommit
    }
  }

  if(env?.CHANGE_ID == null){
    baseCommit = env.GIT_COMMIT
  } else if("${env.GIT_COMMIT}".equals("${latestCommit}")){
    baseCommit = env.GIT_COMMIT
  } else {
    baseCommit = previousCommit
  }
  env.GIT_BASE_COMMIT = baseCommit
  log(level: 'DEBUG', text: "GIT_BASE_COMMIT = ${env.GIT_BASE_COMMIT}")
  return baseCommit
}
