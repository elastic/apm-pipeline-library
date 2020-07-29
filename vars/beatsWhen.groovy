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
  def macros = args.get('changeset', [:])
  def ret = false

  def changeset = content.get('changeset', [])
  def comments = content.get('comments', [])
  def isBranch = content.get('branches', false)
  def isTag = content.get('tags', false)
  def labels = content.get('labels', [])
  def parameters = content.get('parameters', [])

  if (whenBranches(isBranch: isBranch, project: project)) { ret = true }
  if (whenChangeset(changeset: changeset, macros: macros, project: project)) { ret = true }
  if (whenComments(comments: comments, project: project)) { ret = true }
  if (whenLabels(labels: labels, project: project)) { ret = true }
  if (whenParameters(parameters: parameters, project: project)) { ret = true }
  if (whenTags(isTag: isTag, project: project)) { ret = true }
  return ret
}

private Boolean whenBranches(Map args = [:]) {
  def isBranch = args.get('isBranch', false)
  def project = args.project
  if (env.BRANCH_NAME?.trim() && isBranch) {
    markdownReason(project: project, reason: 'Branch is enabled and matches with the pattern.')
    return true
  }
  return false
}

private Boolean whenChangeset(Map args = [:]) {
  def macros = args.get('macros', [:])
  def changeset = args.get('changeset', [])
  def project = args.project

  if (changeset) {
    // Gather macro changeset entries
    def macro = [:]
    macros.each { k,v ->
      macro[k] = v
    }

    // Create list of changeset patterns to be searched.
    def patterns = []
    changeset.each {
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
      markdownReason(project: project, reason: 'Changeset is enabled and matches with the pattern.')
      return true
    } else {
      markdownReason(project: project, reason: 'Changeset is enabled and does not match with the pattern.')
    }
  }

  return false
}

private Boolean whenComments(Map args = [:]) {
  def comments = args.get('comments', [])
  def project = args.project
  if (comments && env.GITHUB_COMMENT?.trim()) {
    if (comments.any { env.GITHUB_COMMENT?.toLowerCase()?.contains(it?.toLowerCase()) }) {
      markdownReason(project: project, reason: 'Comment is enabled and matches with the pattern.')
      return true
    }
    markdownReason(project: project, reason: 'Comment is enabled and does not match with the pattern.')
  }
  return false
}

private Boolean whenLabels(Map args = [:]) {
  def labels = args.get('labels', [])
  def project = args.project
  if (labels) {
    if (labels.any { matchesPrLabel(label: it) }) {
      markdownReason(project: project, reason: 'Label is enabled and matches with the pattern.')
      return true
    } else {
      markdownReason(project: project, reason: 'Label is enabled and does not match with the pattern.')
    }
  }
  return false
}

private Boolean whenParameters(Map args = [:]) {
  def parameters = args.get('parameters', [])
  def project = args.project
  if (parameters) {
    if (parameters.any { params[it] }) {
      markdownReason(project: project, reason: 'Parameter is enabled and matches with the pattern.')
      return true
    } else {
      markdownReason(project: project, reason: 'Parameter is enabled and does not match with the pattern.')
    }
  }
  return false
}

private Boolean whenTags(Map args = [:]) {
  def isTag = args.get('isTag', false)
  def project = args.project
  if (env.TAG_NAME?.trim() && isTag) {
    markdownReason(project: project, reason: 'Tag is enabled and matches with the pattern.')
    return true
  }
  return false
}

private void markdownReason(Map args = [:]) {
  def fileName = "build-${args.project}.md"
  def data = ''
  if(fileExists(fileName)) {
    data = readFile(file: "${fileName}")
  }
  writeFile file: 'build.sbt', text: "${data}\r\n${args.reason}"
}
