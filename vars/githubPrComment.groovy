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
  def commentFile = args.get('commentFile', 'comment.id')
  def details = args.containsKey('details') ? "* Further details: [here](${args.details})" : ''
  def message = args.containsKey('message') ? args.message : ''

  if (isPR()) {
    def comment = commentTemplate(details: "${details}", message: message)
    // Add some metadata to support the githubPrLatestComment step
    def commentWithMetadata =  comment + "\n${metadata(args)}"
    addOrEditComment(commentFile: commentFile, details: commentWithMetadata)
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
  return body
}

def addOrEditComment(Map args = [:]) {
  def commentFile = args.commentFile
  def details = args.details
  def id = getCommentIfAny(args)
  if (id != errorId()) {
    try {
      editComment(id, details)
    } catch (err) {
      log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${id}' failed with error '${err}'. Let's fallback to add a comment.")
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
  def id
  try {
    def comment = pullRequest.comment(details)
    id = comment?.id
  } catch (err) {
    log(level: 'DEBUG', text: "githubPrComment: pullRequest.comment failed with message: ${err.toString()}")
    id = githubTraditionalPrComment(message: details)
  }
  return id
}

def editComment(id, details) {
  log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${id}'. If comment still exists.")
  try {
    pullRequest.editComment(id, details)
  } catch (err) {
    log(level: 'DEBUG', text: "githubPrComment: pullRequest.editComment failed with message: ${err.toString()}")
    githubTraditionalPrComment(message: details, id: id)
  }
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

/**
  Support search for the comment id.
**/
def getCommentIfAny(Map args = [:]) {
  def commentId = getCommentFromFile(args)
  def id = errorId()
  if (commentId?.trim() && commentId.isInteger()) {
    id = commentId as Integer
  } else {
    try {
      commentId = githubPrLatestComment(pattern: metadata(args), users: ['elasticmachine', 'apmmachine'])
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

def metadata(Map args = [:]){
  // .toString() to avoid org.codehaus.groovy.runtime.GStringImpl issues when comparing Strings.
  return "<!--COMMENT_GENERATED_WITH_ID_${args.commentFile}-->".toString()
}
