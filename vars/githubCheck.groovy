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

import java.util.Date

/**
  Send GitHub check step

  githubCheck(name: 'checkName', description: 'Execute something')

*/
def call(Map args = [:]) {
  def name = args.containsKey('name') ? args.name : error('githubCheck: name parameter is required')
  def description = args.get('description', name)
  def body = args.get('body', '')
  def secret = args.get('secret', 'secret/observability-team/ci/github-app')
  def org = args.get('org', env.ORG_NAME)
  def repository = args.get('repository', env.REPO_NAME)
  def commitId = args.get('commitId', env.GIT_BASE_COMMIT)
  def status = args.get('status', 'neutral')
  def detailsUrl = args.get('detailsUrl', '')

  def token = githubAppToken(secret: secret)
  def parameters = [
                    checkName: name,
                    commitId: commitId,
                    org: org,
                    repository: repository,
                    status: status,
                    token: token
                  ]
  def checkRunId = getPreviousCheckNameRunIdIfExists(parameters)
  parameters << [
    output: [
      title: name,
      summary: description,
      text: body
    ],
    checkRunId: checkRunId,
    detailsUrl: detailsUrl
  ]
  if (checkRunId) {
    updateCheck(parameters)
  } else {
    createCheck(parameters)
  }
}

def getPreviousCheckNameRunIdIfExists(Map args = [:]) {
  try {
    def checkRuns = githubApiCall(token: args.token,
                                  url: "https://api.github.com/repos/${args.org}/${args.repository}/commits/${args.commitId}/check-runs",
                                  headers: ['Accept': 'application/vnd.github.v3+json'])
    return checkRuns?.check_runs?.find { it.name == args.checkName }?.id
  } catch(Exception e){
    return false
  }
}

def createCheck(Map args = [:]) {
  args.method = 'POST'
  setCheckName(args)
}

def updateCheck(Map args = [:]) {
  args.method = 'PATCH'
  setCheckName(args)
}

def setCheckName(Map args = [:]) {
  def token = args.token
  def org = args.org
  def repository = args.repository
  def checkName = args.checkName
  def status = args.get('status', 'neutral')
  def method = args.get('method', 'POST')
  def commitId = args.get('commitId', null)
  def checkRunId = args.get('checkRunId', null)
  def output = args.get('output', [:])
  def detailsUrl = args.get('detailsUrl', '')

  try {
    def when = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    // See https://docs.github.com/en/free-pro-team@latest/rest/reference/checks#create-a-check-run--parameters
    def data = [ 'name': "${checkName}",
                 'status': "in_progress",
                 'conclusion': "${status}",
                 'completed_at': "${when}",
                 'output': output
               ]
    if (detailsUrl?.trim()) {
      data['details_url'] = detailsUrl
    }
    def url = "https://api.github.com/repos/${org}/${repository}/check-runs"

    if (method == 'POST') {
      data['head_sha'] = "${commitId}"
    } else {
      url += "/${checkRunId}"
    }

    return githubApiCall(token: token,
                         url: url,
                         headers: ['Accept': 'application/vnd.github.v3+json'],
                         method: method,
                         data: data,
                         noCache: true)
  } catch(Exception e){
    log(level: 'ERROR', text: "Exception: ${e}")
    error 'setCheckName: Failed to create a check run'
  }
}
