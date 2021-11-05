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
  Given the list of patterns, the CHANGE_TARGET, GIT_SHA env variables and the kind of match then it
  evaluates the change list with the pattern list:

  - When exact match then all the files should match those patterns then it returns `true` otherwise
  `false`.
  - Otherwise if any files match any of those patterns then it returns `true` otherwise `false`.

 def match = isGitRegionMatch(patterns: ["^_beats", "^_beats/apm-server.yml"], shouldMatchAll: true)

  NOTE: This particular implementation requires to checkout with the step gitCheckout

*/
def call(Map args = [:]) {
  if(!isUnix()){
    error('isGitRegionMatch: windows is not supported yet.')
  }
  def patterns = args.containsKey('patterns') ? args.patterns.toList() : error('isGitRegionMatch: patterns parameter is required.')
  def shouldMatchAll = args.get('shouldMatchAll', false)
  def from = args.get('from', env.CHANGE_TARGET?.trim() ? "origin/${env.CHANGE_TARGET}" : "${env.GIT_PREVIOUS_COMMIT?.trim() ? env.GIT_PREVIOUS_COMMIT : env.GIT_BASE_COMMIT}")
  def to = args.get('to', env.GIT_BASE_COMMIT)

  if (patterns.isEmpty()) {
    error('isGitRegionMatch: Missing patterns with values.')
  }

  def gitDiffFile = 'git-diff.txt'
  def match = false
  if (from?.trim() && to?.trim()) {
    def changes = sh(script: "git diff --name-only ${from}...${to} > ${gitDiffFile}", returnStdout: true)
    if (shouldMatchAll) {
      match = isFullPatternMatch(gitDiffFile, patterns)
    } else {
      match = isPartialPatternMatch(gitDiffFile, patterns)
    }
    log(level: 'INFO', text: "isGitRegionMatch: ${match ? 'found' : 'not found'} with regex ${patterns}")
  } else {
    echo 'isGitRegionMatch: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes. Or the from/to arguments are required.'
  }
  return match
}

def isFullPatternMatch(gitDiffFile, patterns) {
  def fileContent = readFile(gitDiffFile)
  def match = true
  fileContent.split('\n').each { String line ->
    log(level: 'DEBUG', text: "changeset element: '${line}'")
    if (!patterns.every { pattern -> isPatternMatch(line, pattern) }) {
      match = false
    }
  }
  return match
}

def isPartialPatternMatch(gitDiffFile, patterns) {
  def fileContent = readFile(gitDiffFile)
  def match = patterns.any { pattern ->
    fileContent.split('\n').any { line -> isPatternMatch(line, pattern) }
  }
  return match
}

@NonCPS
def isPatternMatch(line, pattern) {
  def match = line ==~ pattern
  return match
}
