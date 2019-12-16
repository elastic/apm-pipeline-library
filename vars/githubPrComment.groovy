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
  Add a comment in the GitHub.

  githubPrComment()
*/
def call(Map params = [:]){
  def details = params.containsKey('details') ? "* Further details: [here](${params.details})" : ''

  if (env?.CHANGE_ID) {
    pullRequest.comment(commentTemplate(details: "${details}"))
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
  return """
    ${header}
    * [pipeline](${url})
    * Commit: ${env.GIT_BASE_COMMIT}
    ${details}

    <!--PIPELINE
    ${toJSON(createBuildInfo()).toString()}
    PIPELINE-->
  """.stripIndent()
}
