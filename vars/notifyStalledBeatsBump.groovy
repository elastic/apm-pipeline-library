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
  Evaluate if the latest bump update was merged a few days ago and if so
  send an email if configured for such an action.

  // Run the notifyStalledBeatsBumps and send an email
  notifyStalledBeatsBumps(branch: '8.0', sendEmail: true, to: 'foo@acme.com')
*/

def call(Map args = [:]) {
  def branch = args.branch
  def days = args.get('days', 7)
  def sendEmail = args.get('sendEmail', 'false')
  def to = args.get('to', '')

  // Proceed but notify with a warning
  if (sendEmail && !to?.trim()) {
    log(level: 'WARN', text: "stalledBeatsBump: email won't be sent since 'to' param is empty.")
  }

  // Look whether the file has changed recently
  def didFileChangeRecently = true
  dir(branch) {
    // TODO: review is a full checkout or at least n number of days
    git(branch: branch, url: 'https://github.com/elastic/beats.git')
    fileDidNotChangeRecently = sh(script: "git log --name-only --since='${days} days ago' | grep 'testing/environments/snapshot-oss.yml'", returnStatus: true) > 0
  }

  if (fileDidNotChangeRecently) {
    if (sendEmail && to?.trim()) {
      mail(to: to,
        subject: getSubject(branch),
        body: getBody(branch, openPullRequest(branch), days),
        mimeType: 'text/html'
      )
    }
  } else {
    log(level: 'WARN', text: "stalledBeatsBump: there are no changes to be reported")
  }
}

private openPullRequest(branch) {
  def filter = "is:open is:pr author:apmmachine base:${branch}"
  def template = '{{range .}}{{tablerow .url (.createdAt | timeago)}}{{end}}'
  return gh(command: 'pr list', flags: [ search: filter, json: "url,createdAt", template: template ])
}

private getSubject(branch){
  return "[Bump][${branch}] Elastic Stack version has not been updated."
}

private getBody(branch, list, days){
  return """
  Just wanted to share with you that the Elastic Stack version for the ${branch} branch has not been updated for a while ( > ${days} days).

  Those bumps are automatically merged if it passes the CI checks. Otherwise, it might be related to some problems with the
  Integrations Tests.

  ${list}

  If any of the existing PRs are obsolete please close them.

  Thanks
  """
}
