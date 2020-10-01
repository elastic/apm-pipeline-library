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

Send an email message with a summary of the build result,
and send some data to Elasticsearch.

notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')

**/

import co.elastic.BuildException
import co.elastic.NotificationManager
import hudson.tasks.test.AbstractTestResultAction
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper

def call(Map args = [:]) {
  def notifyPRComment = args.containsKey('prComment') ? args.prComment : true
  def notifySlackComment = args.containsKey('slackComment') ? args.slackComment : false
  def analyzeFlakey = args.containsKey('analyzeFlakey') ? args.analyzeFlakey : false
  def newPRComment = args.containsKey('newPRComment') ? args.newPRComment : [:]
  def flakyReportIdx = args.containsKey('flakyReportIdx') ? args.flakyReportIdx : ""
  def flakyThreshold = args.containsKey('flakyThreshold') ? args.flakyThreshold : 0.0

  node('master || metal || immutable'){
    stage('Reporting build status'){
      def secret = args.containsKey('secret') ? args.secret : 'secret/observability-team/ci/jenkins-stats-cloud'
      def es = args.containsKey('es') ? args.es : getVaultSecret(secret: secret)?.data.url
      def to = args.containsKey('to') ? args.to : customisedEmail(env.NOTIFY_TO)
      def statsURL = args.containsKey('statsURL') ? args.statsURL : "https://ela.st/observabtl-ci-stats"
      def shouldNotify = args.containsKey('shouldNotify') ? args.shouldNotify : !isPR() && currentBuild.currentResult != "SUCCESS"
      def slackChannel = args.containsKey('slackChannel') ? args.slackChannel : env.SLACK_CHANNEL
      def slackAlways = args.containsKey('slackAlways') ? args.slackAlways : (currentBuild.currentResult != "SUCCESS")
      catchError(message: 'There were some failures with the notifications', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        def data = getBuildInfoJsonFiles(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER, returnData: true)
        data['docsUrl'] = "http://${env?.REPO_NAME}_${env?.CHANGE_ID}.docs-preview.app.elstc.co/diff"
        data['emailRecipients'] = to
        data['statsUrl'] = statsURL
        def notificationManager = new NotificationManager()
        if(shouldNotify && !to?.empty){
          log(level: 'DEBUG', text: 'notifyBuildResult: Notifying results by email.')
          notificationManager.notifyEmail(data)
        }

        newPRComment.findAll { k, v ->
          catchError(message: "There were some failures when generating the customise comment for $k", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
            unstash v
            notificationManager.customPRComment(commentFile: k, file: v)
          }
        }

        // Should analyze flakey
        if(analyzeFlakey) {
          data['es'] = es
          data['es_secret'] = secret
          data['flakyReportIdx'] = flakyReportIdx
          data['flakyThreshold'] = flakyThreshold
          log(level: 'DEBUG', text: "notifyBuildResult: Generating flakey test analysis.")
          catchError(message: "There were some failures when generating flakey test results", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
            notificationManager.analyzeFlakey(data)
          }
        }
        // Should notify if it is a PR and it's enabled
        if(notifyPRComment && isPR()) {
          log(level: 'DEBUG', text: "notifyBuildResult: Notifying results in the PR.")
          notificationManager.notifyPR(data)
        }

        // Should notify in slack if it's enabled
        if(notifySlackComment) {
          data['channel'] = "#beats-ci-builds" // TODO: slackChannel
          data['credentialId'] = 'jenkins-slack-integration-token'
          data['enabled'] = slackAlways
          log(level: 'DEBUG', text: "notifyBuildResult: Notifying results in slack.")
          notificationManager.notifySlack(data)
        }
        log(level: 'DEBUG', text: 'notifyBuildResult: Generate build report.')
        catchError(message: "There were some failures when generating the build report", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
          notificationManager.generateBuildReport(data)
        }
      }

      catchError(message: 'There were some failures when sending data to elasticsearch', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        timeout(5) {
          def datafile = readFile(file: 'build-report.json')
          sendDataToElasticsearch(es: es, secret: secret, data: datafile)
        }
      }

      catchError(message: 'There were some failures when cleaning up the workspace ', buildResult: 'SUCCESS') {
        deleteDir()
      }
    }
  }
}

def customisedEmail(String email) {
  if (email) {
    // default name should be the REPO env variable.
    def suffix = env.REPO

    // If JOB_NAME then let's get its parent folder name
    if (env.JOB_NAME) {
      def folders = env.JOB_NAME.split("/")
      if (folders?.length > 0) {
        suffix = folders[0]
      }
    }
    if (suffix?.trim()) {
      return [email.replace('@', "+${suffix}@")]
    } else {
      return [email]
    }
  }
  return []
}
