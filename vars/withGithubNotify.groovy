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

  def blueoceanBuildURL = getBORedirect(env.RUN_DISPLAY_URL)

  switch (type) {
    case 'test':
      url = "${blueoceanBuildURL}tests"
      break
    case 'artifact':
      url = "${blueoceanBuildURL}artifacts"
      break
    case 'build':
      url = blueoceanBuildURL
      break
    default:
      error 'withGithubNotify: Unsupported type'
  }
  return url
}

/**
* Blueocean doesn't provide any env variable with the final URL to the BO view.
* Besides, folder separator is raw encoded, aka %2F, rather than '/'. Therefore,
* it's not easy to detect progammatically what items are either MBP or folders.
*
* Further details: https://groups.google.com/forum/#!topic/jenkinsci-users/-fuk4BK6Hvs
*/
def getBORedirect(String url) {
  def redirect
  if (isUnix()) {
    redirect = sh(script: "curl -w '%{url_effective}' -I -L -s -S ${url} -o /dev/null", returnStdout: true)
  } else {
    redirect = powershell(script: "[System.Net.HttpWebRequest]::Create('${url}').GetResponse().ResponseUri.AbsoluteUri",
                          returnStdout: true)
  }
  return redirect
}
