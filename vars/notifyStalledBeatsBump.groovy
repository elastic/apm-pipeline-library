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
  def branch = args.containsKey('branch') ? args.branch : error('notifyStalledBeatsBumps: branch parameter is required')
  def days = args.get('days', 7)
  def sendEmail = args.get('sendEmail', 'false')
  def to = args.get('to', '')

  // Proceed but notify with a warning
  if (sendEmail && !to?.trim()) {
    log(level: 'WARN', text: "notifyStalledBeatsBumps: email won't be sent since 'to' param is empty.")
  }

  // Run the script in charge to create the email.txt if there are things to be notified
  def scriptFile = 'generate-email-template-if-no-recent-changes.sh'
  def resourceContent = libraryResource("scripts/${scriptFile}")
  writeFile(file: scriptFile, text: resourceContent)

  // TODO: within the gh context
  sh(label: scriptFile, returnStatus: true, script: """#!/bin/bash -x
    chmod 755 ${scriptFile}
    ./${scriptFile} 'https://github.com/elastic/beats.git' ${branch} ${days}""")

  if (fileExists("email.txt")) {
    if (sendEmail && to?.trim()) {
      mail(to: to,
        subject: "[Bump][${branch}] Elastic Stack version has not been updated.",
        body: readFile("email.txt"),
        mimeType: 'text/html'
      )
    }
  } else {
    log(level: 'WARN', text: "notifyStalledBeatsBumps: there are no changes to be reported")
  }
}
