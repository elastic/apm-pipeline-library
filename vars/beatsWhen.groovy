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
* Given the YAML definition and the changeset global macros
* then it verifies if the project or stage should be enabled.
*/
Boolean call(Map args = [:]){
  def project = args.containsKey('project') ? args.project : error('beatsWhen: project param is required')
  def content = args.containsKey('content') ? args.content : error('beatsWhen: content param is required')
  def changeset = args.changeset
  def ret = false

  if (whenBranches(args)) { ret = true }
  if (whenChangeset(args)) { ret = true }
  if (whenComments(args)) { ret = true }
  if (whenLabels(args)) { ret = true }
  if (whenParameters(args)) { ret = true }
  if (whenTags(args)) { ret = true }

  return ret
}

private Boolean whenBranches(Map args = [:]) {
  if (env.BRANCH_NAME?.trim() && args.content?.get('branches')) {
    markdownReason(project: args.project, reason: 'Branch is enabled and matches with the pattern.')
    return true
  }
  markdownReason(project: args.project, reason: 'Branch is disabled')
  return false
}

private Boolean whenChangeset(Map args = [:]) {
  if (args.content?.get('changeset')) {
    // Gather macro changeset entries
    def macro = [:]
    args?.changeset?.each { k,v ->
      macro[k] = v
    }

    // Create list of changeset patterns to be searched.
    def patterns = []
    args.content.changeset.each {
      if (it.startsWith('@')){
        def search = it.replaceAll('@', '')
        macro[search].each { macroEntry ->
          patterns << macroEntry
        }
      } else {
        patterns << it
      }
    }

    // TODO: to be refactored  with isGitRegionMatch.isPartialPatternMatch()

    // Gather the diff between the target branch and the current commit.
    def gitDiffFile = 'git-diff.txt'
    def from = env.CHANGE_TARGET?.trim() ? "origin/${env.CHANGE_TARGET}" : env.GIT_PREVIOUS_COMMIT
    sh(script: "git diff --name-only ${from}...${env.GIT_BASE_COMMIT} > ${gitDiffFile}", returnStdout: true)

    // Search for any pattern that matches that particular
    def fileContent = readFile(gitDiffFile)
    ret = patterns?.any { pattern ->
      fileContent?.split('\n').any { line -> line ==~ pattern }
    }
    if (ret) {
      markdownReason(project: args.project, reason: 'Changeset is enabled and matches with the pattern.')
      return true
    } else {
      markdownReason(project: args.project, reason: 'Changeset is enabled and does not match with the pattern.')
    }
  } else {
    markdownReason(project: args.project, reason: 'Changeset is disabled')
  }
  return false
}

private Boolean whenComments(Map args = [:]) {
  if (args.content?.get('comments') && env.GITHUB_COMMENT?.trim()) {
    def match = args.content.get('comments').find { env.GITHUB_COMMENT?.toLowerCase()?.contains(it?.toLowerCase()) }
    if (match) {
      markdownReason(project: args.project, reason: "Comment is enabled and matches with the pattern '${match}.")
      return true
    }
    markdownReason(project: args.project, reason: "Comment is enabled and does not match with the pattern '${args.content.get('comments').toString()}.")
  } else {
    markdownReason(project: args.project, reason: 'Comment is disabled')
  }
  return false
}

private Boolean whenLabels(Map args = [:]) {
  if (args.content?.get('labels')) {
    def match = args.content.get('labels').find { matchesPrLabel(label: it) }
    if (match) {
      markdownReason(project: args.project, reason: "Label is enabled and matches with the pattern '${match}.")
      return true
    }
    markdownReason(project: args.project, reason: "Label is enabled and does not match with the pattern '${args.content.get('labels').toString()}.")
  } else {
    markdownReason(project: args.project, reason: 'Label is disabled')
  }
  return false
}

private Boolean whenParameters(Map args = [:]) {
  if (args.content?.get('parameters')) {
    def match = args.content.get('parameters').find { params[it] }
    if (match) {
      markdownReason(project: args.project, reason: "Parameter is enabled and matches with the pattern '${match}.")
      return true
    } else {
      markdownReason(project: args.project, reason: "Parameter is enabled and does not match with the pattern '${args.content.get('parameters').toString()}.")
    }
  } else {
    markdownReason(project: args.project, reason: 'Parameter is disabled')
  }
  return false
}

private Boolean whenTags(Map args = [:]) {
  if (env.TAG_NAME?.trim() && args.content?.get('tags')) {
    markdownReason(project: args.project, reason: 'Tag is enabled and matches with the pattern.')
    return true
  }
  markdownReason(project: args.project, reason: 'Tag is disabled')
  return false
}

private void markdownReason(Map args = [:]) {
  def fileName = "build-${args.project?.trim() ?: ''}-reasons.md"
  def data = ''
  if(fileExists(fileName)) {
    data = readFile(file: "${fileName}")
  }
  writeFile(file: fileName, text: "${data}\r\n${args.reason}")
}
