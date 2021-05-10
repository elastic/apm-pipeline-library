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
  Wrap the GitHub notify commit status or check step

  withGithubStatus(context: 'checkName', description: 'Execute something') {
    // block
  }

  withGithubStatus(context: 'checkName', description: 'Execute something', isBlueOcean: false) {
    // block
  }
*/
def call(Map args = [:], Closure body) {
  def context = args.containsKey('context') ? args.context : error('withGithubStatus: context parameter is required.')
  def description = args.get('description', context)
  def tab = args.get('tab', 'pipeline')
  def isBlueOcean = args.get('isBlueOcean', false)
  def ignoreGitHubFailures = args.get('ignoreGitHubFailures', true)

  def redirect = detailsURL(tab: tab, isBlueOcean: isBlueOcean)

  try {
    notifyMap(context: context, description: "${description} ...", status: 'PENDING', redirect: redirect, ignoreGitHubFailures: ignoreGitHubFailures)
    withAPM(){
      body()
    }
    notifyMap(context: context, description: "${description} passed", status: 'SUCCESS', redirect: redirect, ignoreGitHubFailures: ignoreGitHubFailures)
  } catch (err) {
    notifyMap(context: context, description: "${description} failed", status: 'FAILURE', redirect: redirect, ignoreGitHubFailures: ignoreGitHubFailures)
    throw err
  }
}

// This is consumed by elastic/kibana.git@master:.ci/end2end.groovy
def notify(String context, String description, String status, String redirect) {
  notifyMap(context: context, description: description, status: status, redirect: redirect)
}

def notifyMap(Map args = [:]) {
  retryWithSleep(retries: 2, seconds: 5, backoff: true) {
    try {
      githubNotify(context: "${args.context}", description: "${args.description}", status: "${args.status}", targetUrl: "${args.redirect}")
    } catch (err) {
      if (args.get('ignoreGitHubFailures', false)) {
        log(level: 'WARN', text: "withGithubStatus: failed with error '${err.toString()}'. But 'ignoreGitHubFailures' has been enabled.")
      } else {
        throw err
      }
    }
  }
}
