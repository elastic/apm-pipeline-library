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

import com.cloudbees.groovy.cps.NonCPS

/**
  Given the regex pattern, the CHANGE_TARGET, GIT_SHA env variables then it
  evaluates the change list:

  - When exact match then all the files should match those patterns then it
    returns the region otherwise and empty string.

  // def module = getRegionFromPattern(pattern: "beat/module/([^\/]+)/")
  whenTrue(module.trim()) {
    // ...
  }

  NOTE: This particular implementation requires to checkout with the step gitCheckout

*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('getRegionFromPattern: windows is not supported yet.')
  }
  def pattern = params.containsKey('pattern') ? params.pattern : error('getRegionFromPattern: Missing pattern argument.')
  def from = params.get('from', env.CHANGE_TARGET?.trim() ? "origin/${env.CHANGE_TARGET}" : env.GIT_PREVIOUS_COMMIT)
  def to = params.get('to', env.GIT_BASE_COMMIT)

  def gitDiffFile = 'git-diff.txt'
  def match = false
  if (from?.trim() && to?.trim()) {
    def changes = sh(script: "git diff --name-only ${from}...${to} > ${gitDiffFile}", returnStdout: true)
    match = getRegion(gitDiffFile, pattern)
    log(level: 'INFO', text: "getRegionFromPattern: ${match.trim() ?: 'not found'} with regex ${pattern}")
  } else {
    echo 'getRegionFromPattern: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes. Or the from/to arguments are required.'
  }
  return match
}

def getRegion(gitDiffFile, patterns) {
  def fileContent = readFile(gitDiffFile)
  def match = true
  fileContent.split('\n').each { String line ->
    log(level: 'DEBUG', text: "changeset element: '${line}'")
    if (!patterns.every { line ==~ it }) {
      match = false
    }
  }
  return match
}
