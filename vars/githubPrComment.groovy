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
def call(Map args = [:]){
  def details = args.containsKey('details') ? "* Further details: [here](${args.details})" : ''
  def message = args.containsKey('message') ? args.message : ''

  if (isPR()) {
    addOrEditComment(commentTemplate(details: "${details}", message: message))
  } else {
    log(level: 'WARN', text: 'githubPrComment: is only available for PRs.')
  }
}

def commentTemplate(Map args = [:]) {
  def details = args.containsKey('details') ? args.details : ''
  def header = currentBuild.currentResult == 'SUCCESS' ?
              '## :green_heart: Build Succeeded' :
              '## :broken_heart: Build Failed'
  def url = env.RUN_DISPLAY_URL?.trim() ? env.RUN_DISPLAY_URL : env.BUILD_URL

  def body
  if (args.message?.trim()) {
    body = args.message
  } else {
    body = """\
      ${header}
      * [pipeline](${url})
      * Commit: ${env.GIT_BASE_COMMIT}
      ${details}
    """.stripIndent()  // stripIdent() requires """/
  }

  return "${body}\n${metadata()}"
}

def addOrEditComment(String details) {
  def id = getCommentIfAny()
  if (id != errorId()) {
    try {
      log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${id}'. If comment still exists.")
      pullRequest.editComment(id, details)
    } catch (err) {
      log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${id}' failed with error '${err}'. Let's fallback to add a comment.")
      id = addComment(details)
    }
  } else {
    id = addComment(details)
  }
  writeFile(file: "${commentIdFileName()}", text: "${id}")
  archiveArtifacts(artifacts: commentIdFileName())
}

def addComment(String details) {
  log(level: 'DEBUG', text: 'githubPrComment: Add a new comment.')
  def comment = pullRequest.comment(details)
  return comment?.id
}

def getCommentFromFile() {
  copyArtifacts(filter: commentIdFileName(), flatten: true, optional: true, projectName: env.JOB_NAME, selector: lastWithArtifacts())
  if (fileExists(commentIdFileName())) {
    return readFile(commentIdFileName())?.trim()
  } else {
    return ''
  }
}

def commentIdFileName() {
  return 'comment.id'
}

/**
  Support search for the comment id.
**/
def getCommentIfAny() {
  def commentId = getCommentFromFile()
  def id = errorId()
  if (commentId?.trim() && commentId.isInteger()) {
    id = commentId as Integer
  } else {
    try {
      commentId = githubPrLatestComment(pattern: metadata(), users: ['elasticmachine', 'apmmachine'])
      if (commentId && commentId.id) {
        id = commentId.id as Integer
      }
    } catch(e) {
      log(level: 'WARN', text: "githubPrLatestComment: failed. Therefore a new GitHub comment will be created. For further details see ${e}")
    }
  }
  return id
}

def errorId() {
  return -1000
}

def metadata(){
  return '<!--METADATA-->'
}
