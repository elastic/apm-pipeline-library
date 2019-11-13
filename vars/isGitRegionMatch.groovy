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
  Given the list of patterns, the CHANGE_TARGET, GIT_SHA env variables and the kind of match then it
  evaluates the change list with the regexp list:

  - When exact match then all the files should match those patterns then it returns `true` otherwise
  `false`.
  - Otherwise if any files match any of those patterns then it returns `true` otherwise `false`.

 def match = isGitRegionMatch(regexps: ["^_beats","^apm-server.yml", "^apm-server.docker.yml"], isExactMatch: true)

*/
def call(Map params = [:]) {
  if(!isUnix()){
    error('isGitRegionMatch: windows is not supported yet.')
  }
  def patterns = params.containsKey('regexps') ? params.regexps : error('isGitRegionMatch: Missing regexps argument.')
  def isExactMatch = params.get('isExactMatch', false)
  def comparator = params.get('comparator', 'glob')

  if (patterns.isEmpty()) {
    error('isGitRegionMatch: Missing regexps with values.')
  }

  def gitDiffFile = 'git-diff.txt'
  def match = false
  if (env.CHANGE_TARGET && env.GIT_SHA) {
    def changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...${env.GIT_SHA} > ${gitDiffFile}", returnStdout: true)
    if (isExactMatch) {
      match = isFullPatternMatch(gitDiffFile, patterns, isGlob(comparator))
    } else {
      match = isPartialPatternMatch(gitDiffFile, patterns, isGlob(comparator))
    }
    log(level: 'INFO', text: "isGitRegionMatch: ${match ? 'found' : 'not found'}")
  } else {
    echo 'isGitRegionMatch: CHANGE_TARGET and GIT_SHA env variables are required to evaluate the changes.'
  }
  return match
}

def isFullPatternMatch(gitDiffFile, patterns, isGlob) {
  def fileContent = readFile(gitDiffFile)
  def match = true
  fileContent.eachLine { String line ->
    log(level: 'DEBUG', text: "changeset element: '${line}'")
    if (isGlob) {
      if (!patterns.every { pattern -> isGrepPatternFound(fileContent, pattern) }) {
        match = false
      }
    } else {
      if (!patterns.every { line ==~ it }) {
        match = false
      }
    }
  }
  return match
}

def isPartialPatternMatch(gitDiffFile, patterns, isGlob) {
  def match = false
  if (isGlob) {
    match = patterns.any { pattern -> isGrepPatternFoundInFile(gitDiffFile, pattern) }
  } else {
    match = patterns.any { line ==~ it }
  }
  return match
}

def isGrepPatternFoundInFile(file, pattern) {
  return sh(script: "grep '${pattern}' ${file}", returnStatus: true) == 0
}

def isGrepPatternFound(compareWith, pattern) {
  log(level: 'DEBUG', text: "isGrepPatternFound: '${compareWith}' with pattern: '${pattern}'")
  return sh(script: "echo '${compareWith}' | grep '${pattern}'", returnStatus: true) == 0
}

def isGlob(comparator) {
  return comparator.equals('glob')
}
