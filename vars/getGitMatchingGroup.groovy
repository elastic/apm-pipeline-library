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
  Given the regex pattern, the CHANGE_TARGET, GIT_SHA env variables then it
  evaluates the change list and returns the module name.

  - When exact match then all the files should match those patterns then it
    returns the region otherwise and empty string.

  def module = getGitMatchingGroup(pattern: '([^\\/]+)\\/.*')
  whenTrue(module.trim()) {
    // ...
  }

  // Exclude the asciidoc files from the search.
  def module = getGitMatchingGroup(pattern: '([^\\/]+)\\/.*', exclude: '.*\\.asciidoc')


  NOTE: This particular implementation requires to checkout with the step gitCheckout

*/
def call(Map params = [:]) {
  def pattern = params.containsKey('pattern') ? params.pattern : error('getGitMatchingGroup: pattern parameter is required.')
  def exclude = params.get('exclude', '')
  def from = params.get('from', env.CHANGE_TARGET?.trim() ? "origin/${env.CHANGE_TARGET}" : "${env.GIT_PREVIOUS_COMMIT?.trim() ? env.GIT_PREVIOUS_COMMIT : env.GIT_BASE_COMMIT}")
  def to = params.get('to', env.GIT_BASE_COMMIT)

  def gitDiffFile = 'git-diff.txt'
  def group = ''
  if (from?.trim() && to?.trim()) {
    def changes
    def command = "git diff --name-only ${from}...${to} > ${gitDiffFile}"
    if (isUnix()) {
      changes = sh(script: command, returnStdout: true)
    } else {
      changes = bat(script: command, returnStdout: true)
    }
    group = getGroup(gitDiffFile, pattern, exclude)
    log(level: 'INFO', text: "getGitMatchingGroup: ${group.trim() ?: 'not found'} with regex ${pattern}")
  } else {
    log(level: 'INFO', text: 'getGitMatchingGroup: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes. Or the from/to arguments are required.')
  }
  return group
}

def getGroup(gitDiffFile, pattern, exclude) {
  def fileContent = readFile(gitDiffFile)
  def modules = [:]
  fileContent.split('\n').each { String line ->
    if (isExcluded(line, exclude)) {
      log(level: 'DEBUG', text: "[excluded] changeset element: '${line}'")
    } else {
      log(level: 'DEBUG', text: "changeset element: '${line}'")
      def matches = line =~ pattern
      def auxModule = matches.collect { it[1] }[0] ?: ''
      modules[auxModule] = auxModule
    }
  }
  if (modules.size() == 1) {
    return modules.values().toArray()[0]
  }
  return ''
}

def isExcluded(line, exclude) {
  if (exclude.trim()) {
    return (line ==~ exclude)
  }
  return false
}
