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
  Send notifications with the release status by email and slack
  
  releaseNotification(slackColor: 'good',
                      subject: "[${env.REPO}] Release tag *${env.TAG_NAME}* has been created", 
                      body: "Build: (<${env.RUN_DISPLAY_URL}|here>) for further details.")

*/
def call(Map args = [:]) {
  def slackChannel = args.get('slackChannel', env.SLACK_CHANNEL)
  def slackColor = args.get('slackColor')
  def credentialsId = args.get('slackCredentialsId', 'jenkins-slack-integration-token')
  def to = args.get('to', env.NOTIFY_TO)
  def subject = args.get('subject', '')
  def body = args.get('body', '')

  if (slackChannel?.trim()) {
    slackSend(channel: slackChannel,
              color: slackColor,
              message: "${subject}. ${body}",
              tokenCredentialId: credentialsId)
  } else {
    log(level: 'INFO', text: 'releaseNotification: missing slackChannel therefore skipped the slack notification.')
  }
  
  emailext(subject: subject,
           to: to,
           body: transformSlackURLFormatToEmailFormat(body))
}

def transformSlackURLFormatToEmailFormat(String message) {
  // transform slack URL format '(<URL|description>)' to 'URL'.
  return message?.replaceAll('\\(<', '')?.replaceAll('\\|.*>\\)', '')
}
