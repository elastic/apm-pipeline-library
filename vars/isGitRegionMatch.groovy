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
 Given the list of regexps, the CHANGE_TARGET and GIT_SHA env variables then it
 evaluates the change list with the regexp and if any matches then it returns `true` otherwise
 `false`.

 def match = isGitRegionMatch(regexps: ["^_beats","^apm-server.yml", "^apm-server.docker.yml"])

*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('isGitRegionMatch: windows is not supported yet.')
  }
  def regexps =  params.containsKey('regexps') ? params.regexps : error('isGitRegionMatch: Missing regexps argument.')

  if (regexps.isEmpty()) {
    error('isGitRegionMatch: Missing regexps with values.')
  }

  if (env.CHANGE_TARGET && env.GIT_SHA) {
    def changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...${env.GIT_SHA} > git-diff.txt", returnStdout: true)
    def match = regexps.find { regexp -> sh(script: "grep '${regexp}' git-diff.txt",returnStatus: true) == 0 }
    log(level: 'INFO', text: "isGitRegionMatch: '${match ?: 'not' }' matched")
    return (match != null)
  } else {
    echo 'isGitRegionMatch: CHANGE_TARGET and GIT_SHA env variables are required to evaluate the changes.'
    return false
  }
}
