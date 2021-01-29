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
  def context = args.containsKey('context') ? args.context : error('withGithubStatus: missing context argument')
  def description = args.get('description', context)
  def redirect = args.get('tab', 'pipeline')
  def isBo = args.get('isBlueOcean', false)

  // To be refactored
  if (!redirect.startsWith('http')) {
    if (isBo) {
      redirect = getBlueoceanTabURL(redirect)
    } else {
      redirect = getTraditionalPageURL(redirect)
    }
  }
  //
  
  try {
    notify(context, "${description} ...", 'PENDING', redirect)
    withAPM(){
      body()
    }
    notify(context, "${description} passed", 'SUCCESS', redirect)
  } catch (err) {
    notify(context, "${description} failed", 'FAILURE', redirect)
    throw err
  }
}

def notify(String context, String description, String status, String redirect) {
  retryWithSleep(retries: 2, seconds: 5, backoff: true) {
    githubNotify(context: "${context}", description: "${description}", status: "${status}", targetUrl: "${redirect}")
  }
}
