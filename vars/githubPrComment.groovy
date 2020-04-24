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

  _NOTE_: To edit the existing comment is required these environment variables: `ORG_NAME`, `REPO_NAME` and `CHANGE_ID`

*/
def call(Map params = [:]){
  def details = params.containsKey('details') ? "* Further details: [here](${params.details})" : ''
  def message = params.containsKey('message') ? params.message : ''

  if (env?.CHANGE_ID) {
    addOrEditComment(commentTemplate(details: "${details}", message: message))
  } else {
    log(level: 'WARN', text: 'githubPrComment: is only available for PRs.')
  }
}

def createBuildInfo() {
  return [
    commit: env.GIT_BASE_COMMIT,
    number: env.BUILD_ID,
    status: currentBuild.currentResult,
    url: env.BUILD_URL
  ]
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
    body = """
      ${header}
      * [pipeline](${url})
      * Commit: ${env.GIT_BASE_COMMIT}
      ${details}
    """
  }

  // Ensure the PIPELINE comment does not have any indentation
  return """${body}
<!--PIPELINE
${toJSON(createBuildInfo()).toString()}
PIPELINE-->""".stripIndent()
}

def addOrEditComment(String details) {

  // Get the latest comment that was added with this step, if any.
  def lastComment = getLatestBuildComment()

  if (lastComment) {
    log(level: 'DEBUG', text: "githubPrComment: Edit comment with id '${lastComment.id}'.")
    pullRequest.editComment(lastComment.id, details)
  } else {
    log(level: 'DEBUG', text: 'githubPrComment: Add a new comment.')
    pullRequest.comment(details)
  }
}

def getComments() {
  def token = getGithubToken()
  def comments = githubApiCall(token: token, url: "https://api.github.com/repos/${env.ORG_NAME}/${env.REPO_NAME}/issues/${env.CHANGE_ID}/comments")

  return comments
}

def getLatestBuildComment() {
  // Get all the comments for the given PR.
  def comments = getComments()
  return comments
    .reverse()
    .find { (it.user.login == 'elasticmachine' || it.user.login == 'apmmachine') && it.body =~ /<!--PIPELINE/ }
}
