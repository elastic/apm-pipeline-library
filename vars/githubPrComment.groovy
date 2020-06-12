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
  Add a comment or edit an existing comment in the GitHub.

  githubPrComment()

  githubPrComment(details: "${env.BUILD_URL}artifact/docs.txt")

  githubPrComment(message: 'foo bar')

  _NOTE_: To edit the existing comment is required these environment variables: `CHANGE_ID`

*/
def call(Map params = [:]){
  def commentFile = params.get('commentFile', 'comment.id')
  def details = params.containsKey('details') ? "* Further details: [here](${params.details})" : ''
  def message = params.containsKey('message') ? params.message : ''

  if (isPR()) {
    addOrEditComment(commentFile: commentFile, details: commentTemplate(details: "${details}", message: message))
  } else {
    log(level: 'WARN', text: 'githubPrComment: is only available for PRs.')
  }
}

def commentTemplate(Map params = [:]) {
  def details = params.containsKey('details') ? params.details : ''
  def header = currentBuild.currentResult == 'SUCCESS' ?
              '## :green_heart: Build Succeeded' :
              '## :broken_heart: Build Failed'
  def url = env.RUN_DISPLAY_URL?.trim() ? env.RUN_DISPLAY_URL : env.BUILD_URL

  def body
  if (params.message?.trim()) {
    body = params.message
  } else {
    body = """\
      ${header}
      * [pipeline](${url})
      * Commit: ${env.GIT_BASE_COMMIT}
      ${details}
    """.stripIndent()  // stripIdent() requires """/
  }

  return body
}

def addOrEditComment(Map args = [:]) {
  def commentFile = args.commentFile
  def details = args.details
  def commentId = getCommentFromFile(commentFile: commentFile)
  def id
  if (commentId?.trim() && commentId.isInteger()) {
    id = commentId as Integer
    try {
      log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${commentId}'. If comment still exists.")
      pullRequest.editComment(id, details)
    } catch (err) {
      log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${commentId}' failed with error '${err}'. Let's fallback to add a comment.")
      id = addComment(details)
    }
  } else {
    id = addComment(details)
  }
  writeFile(file: "${commentFile}", text: "${id}")
  archiveArtifacts(artifacts: commentFile)
}

def addComment(String details) {
  log(level: 'DEBUG', text: 'githubPrComment: Add a new comment.')
  def comment = pullRequest.comment(details)
  return comment?.id
}

def getCommentFromFile(Map args = [:]) {
  def commentFile = args.commentFile
  copyArtifacts(filter: commentFile, flatten: true, optional: true, projectName: env.JOB_NAME, selector: lastWithArtifacts())
  if (fileExists(commentFile)) {
    return readFile(commentFile)?.trim()
  } else {
    return ''
  }
}
