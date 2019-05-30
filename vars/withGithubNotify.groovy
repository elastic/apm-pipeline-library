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
  Wrap the GitHub notify check step

  withGithubNotify(context: 'checkName', description: 'Execute something') {
    // block
  }
*/
def call(Map params = [:], Closure body) {
  def context = params.context
  def description = params.containsKey('description') ? params.description : context
  def type = params.type ?: 'build'

  if (!context) {
    error 'withGithubNotify: Missing arguments'
  }

  def redirect = getUrlGivenType(type)
  try {
    notify(context, "${description} ...", 'PENDING', redirect)
    body()
    notify(context, "${description} passed", 'SUCCESS', redirect)
  } catch (err) {
    notify(context, "${description} failed", 'FAILURE', redirect)
    throw err
  }
}

def notify(String context, String description, String status, String redirect) {
  githubNotify(context: "${context}", description: "${description}", status: "${status}", targetUrl: "${redirect}")
}

def getUrlGivenType(String type) {
  def url

  // Workaround https://groups.google.com/forum/#!topic/jenkinsci-users/-fuk4BK6Hvs
  def buildURL = "${env.JENKINS_URL}/blue/organizations/jenkins/${env.JOB_NAME}"
  buildURL = buildURL.replace("${env.BRANCH_NAME}", "detail/${env.BRANCH_NAME}/${env.BUILD_ID}")

  switch (type) {
    case 'test':
      url = "${buildURL}/tests"
      break
    case 'artifact':
      url = "${buildURL}/artifacts"
      break
    case 'build':
      url = env.RUN_DISPLAY_URL
      break
    default:
      error 'withGithubNotify: Unsupported type'
  }
  return url
}
