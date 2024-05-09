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

Notify build status in vary ways, such as an email, comment in GitHub, slack message.
In addition, it interacts with Elasticsearch to upload all the build data and execute
the flakey test analyser.

  // Default
  notifyBuildResult()

  // Notify to a different elasticsearch instance.
  notifyBuildResult(es: 'http://elastisearch.example.com:9200', secret: 'secret/team/ci/elasticsearch')

  // Notify a new comment with the content of the bundle-details.md file
  notifyBuildResult(newPRComment: [ bundle-details: 'bundle-details.md' ])

  // Notify build status for a PR as a GitHub comment, and send slack message if build failed
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel')

  // Notify build status for a PR as a GitHub comment, and send slack message to multiple channels if build failed
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel, #other-channel')

  // Notify build status for a PR as a GitHub comment, and send slack message with custom header
  notifyBuildResult(prComment: true, slackComment: true, slackChannel: '#my-channel', slackHeader: '*Header*: this is a header')

**/

import co.elastic.NotificationManager
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper

def call(Map args = [:]) {
  def sendTelemetry = args.containsKey('sendTelemetry') ? args.sendTelemetry : false
  def notifyPRComment = args.containsKey('prComment') ? args.prComment : true
  def notifyCoverageComment = args.containsKey('coverageComment') ? args.coverageComment : true
  def notifyGoBenchmarkComment = args.containsKey('goBenchmarkComment') ? args.goBenchmarkComment : false
  def notifySlackComment = args.containsKey('slackComment') ? args.slackComment : false
  def analyzeFlakey = args.containsKey('analyzeFlakey') ? args.analyzeFlakey : false
  def newPRComment = args.containsKey('newPRComment') ? args.newPRComment : [:]
  def notifyGHIssue = args.containsKey('githubIssue') ? args.githubIssue : false
  def githubAssignees = args.get('githubAssignees', '')
  def githubLabels = args.get('githubLabels', '')

  node('master || metal || linux'){
    stage('Reporting build status'){
      def secret = args.containsKey('secret') ? args.secret : 'secret/observability-team/ci/jenkins-stats-cloud'
      def es = args.containsKey('es') ? args.es : getVaultSecret(secret: secret)?.data.url
      def to = args.containsKey('to') ? args.to : customisedEmail(env.NOTIFY_TO)
      def statsURL = args.containsKey('statsURL') ? args.statsURL : "https://ela.st/observabtl-ci-stats"
      def shouldNotify = args.containsKey('shouldNotify') ? args.shouldNotify : !isPR() && currentBuild.currentResult != "SUCCESS"
      def slackHeader = args.containsKey('slackHeader') ? args.slackHeader : ''
      def slackChannel = args.containsKey('slackChannel') ? args.slackChannel : env.SLACK_CHANNEL
      def slackNotify = args.containsKey('slackNotify') ? args.slackNotify : !isPR() && currentBuild.currentResult != "SUCCESS"
      def slackCredentials = args.containsKey('slackCredentials') ? args.slackCredentials : 'jenkins-slack-integration-token'
      def aggregateComments = args.get('aggregateComments', true)
      def flakyDisableGHIssueCreation = args.get('flakyDisableGHIssueCreation', false)
      def jobName = args.get('jobName') ? args.jobName : ''
      catchError(message: 'There were some failures with the notifications', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        def data = getBuildInfoJsonFiles(jobURL: env.JOB_URL, buildNumber: env.BUILD_NUMBER, returnData: true)
        data['docsUrl'] = "http://${env?.REPO_NAME}_bk_${env?.CHANGE_ID}.docs-preview.app.elstc.co/diff"
        data['emailRecipients'] = to
        data['statsUrl'] = statsURL
        data['es'] = es
        data['es_secret'] = secret
        data['header'] = slackHeader
        data['channel'] = slackChannel
        data['credentialId'] = slackCredentials
        data['enabled'] = slackNotify
        data['jobName'] = jobName
        data['githubAssignees'] = githubAssignees
        data['githubLabels'] = githubLabels
        if (args.containsKey('githubTitle')) {
          data['githubTitle'] = githubTitle
        }
        data['disableGHIssueCreation'] = flakyDisableGHIssueCreation
        // Allow to aggregate the comments, for such it disables the default notifications.
        data['disableGHComment'] = aggregateComments
        // Generate digested data to be consumed later on by the createGitHubComment.
        data['comment'] = generateBuildReport(data: data)
        def notifications = []

        notifyEmail(data: data, when: (shouldNotify && !to?.empty))

        addGitHubCustomComment(newPRComment: newPRComment)

        createGitHubComment(data: data, notifications: notifications, when: notifyPRComment)

        // Should analyze flakey but exclude it when aborted
        analyzeFlaky(data: data, notifications: notifications, when: (analyzeFlakey && currentBuild.currentResult != 'ABORTED'))

        notifyGitHubCommandsInPR(data: data, notifications: notifications, when: notifyPRComment)

        notifySlack(data: data, when: notifySlackComment)

        // Notify only if there are notifications and they should be aggregated
        aggregateGitHubComments(when: (aggregateComments && notifications?.size() > 0), notifications: notifications)

        // Notify only if there are notifications and they should be aggregated and env.GITHUB_CHECK feature flag is enabled.
        aggregateGitHubCheck(when: (aggregateComments && notifications?.size() > 0 && env.GITHUB_CHECK?.equals('true')), notifications: notifications)

        createGitHubIssue(data: data, when: notifyGHIssue)
      }

      if (sendTelemetry) {
        catchError(message: 'There were some failures when sending data to elasticsearch', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
          timeout(5) {
            // New indexes
            def bulkFile = 'ci-test-report-bulk.json'
            if (fileExists(bulkFile)) {
              datafile = readFile(file: bulkFile)
              sendDataToElasticsearch(es: es, secret: secret, data: datafile, restCall: '/ci-tests/_bulk/')
            }
            datafile = 'ci-build-report.json'
            if (fileExists(datafile)) {
              sendDataToElasticsearch(es: es, secret: secret, restCall: '/ci-builds/_doc/', data: readFile(file: datafile))
            }

            // NOTE: Support temporarily the email notifications with the test summary
            //       See https://github.com/elastic/apm-pipeline-library/pull/1514 as a potential replacement
            if (env.REPO_NAME == 'integrations') {
              datafile = readFile(file: 'custom-build-report.json')
              sendDataToElasticsearch(es: es, secret: secret, data: datafile, restCall: '/ci-integrations-builds/_doc/')
            }
          }
        }
      }

      if (notifyCoverageComment) {
        notifyCommentWithCoverageReport()
      }

      if (notifyGoBenchmarkComment) {
        notifyCommentWithGoBenchmarkReport()
      }

      // Ensure we don't leave any leftovers if running in the jenkins controller.
      catchError(message: 'Delete dir if possible', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        deleteDir()
      }
    }
  }
}

def notifyCommentWithCoverageReport() {
  catchError(message: 'There were some failures when notifying the coverage report', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    def coverageFile = "tests-coverage.json"
    def coverageMarkdown = 'build/coverage.md'
    if (fileExists(coverageFile)) {
      // If no data to be analysed then
      def testCoverageContent = readJSON(file: coverageFile)
      if (testCoverageContent?.isEmpty()) {
        log(level: 'INFO', text: "notifyBuildResult: the ${coverageFile} file is empty.")
        return
      }

      generateReport(id: 'coverage', input: coverageFile, output: 'build', template: true, compare: true)
      def coverageContent = readFile(file: coverageMarkdown)
      // If no data to be reported then
      if (!coverageContent?.trim()) {
        log(level: 'INFO', text: "notifyBuildResult: the ${coverageMarkdown} file is empty.")
        return
      }
      githubPrComment(message: coverageContent, commentFile: 'coverage')
    } else {
      log(level: 'INFO', text: "notifyBuildResult: there are no ${coverageFile} file to be compared with.")
    }
  }
}

def notifyCommentWithGoBenchmarkReport() {
  catchError(message: 'There were some failures when notifying the go benchmark report', buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    def reportFileName = generateGoBenchmarkDiff.getReportFileName()
    unstash(reportFileName)
    // If no file to be reported
    if (!fileExists(reportFileName)) {
      log(level: 'INFO', text: "notifyBuildResult: there are no ${reportFileName} file to be reported.")
      return
    }

    // If no data to be reported then
    def rawContent = readFile(file: reportFileName)
    if (rawContent?.isEmpty()) {
      log(level: 'INFO', text: "notifyBuildResult: the ${reportFileName} file is empty.")
      return
    }

    def markdownContent = """
### :books: Go benchmark report

Diff with the `${env.CHANGE_TARGET}` branch

```
${rawContent}
```

_report generated with https://pkg.go.dev/golang.org/x/perf/cmd/benchstat_"""

    // Report GitHub comment
    githubPrComment(message: markdownContent, commentFile: reportFileName)
  }
}

def notifyIfNewBuildNotRunning(Closure body) {
  try {
    // As long as there is no a new build running.
    if (nextBuild && !nextBuild?.isBuilding()) {
      log(level: 'INFO', text: 'notifyIfPossible: notification was already done in a younger build.')
      return
    }
  } catch(err) {
    log(level: 'WARN', text: 'notifyIfPossible: could not fetch the nextBuild.')
  }
  body()
}

def aggregateGitHubCheck(def args=[:]) {
  if (args.when) {
    notifyIfNewBuildNotRunning() {
      log(level: 'DEBUG', text: 'aggregateGitHubCheck: aggregate all the messages in one single GitHub check.')
      def status = 'neutral'
      switch (currentBuild.currentResult) {
        case 'SUCCESS':
          status = 'success'
          break
        case 'FAILURE':
          status = 'failure'
          break
        case 'ABORTED':
          status = 'cancelled'
          break
        case 'UNSTABLE':
          status = 'failure'
          break
      }
      githubCheck(name: '.Status',
                  description: args.notifications?.join(''),
                  status: status,
                  detailsUrl: env.BUILD_URL)
    }
  } else {
    log(level: 'DEBUG', text: 'aggregateGitHubCheck: is disabled.')
  }
}

def aggregateGitHubComments(def args=[:]) {
  if (args.when) {
    notifyIfNewBuildNotRunning() {
      log(level: 'DEBUG', text: 'aggregateGitHubComments: aggregate all the messages in one single GH Comment.')
      // Reuse the same commentFile from the notifyPR method to keep backward compatibility with the existing PRs.
      githubPrComment(commentFile: 'comment.id', message: args.notifications?.join(''))
    }
  } else {
    log(level: 'DEBUG', text: 'aggregateGitHubComments: is disabled.')
  }
}

def addGitHubCustomComment(def args=[:]) {
  args.newPRComment.findAll { k, v ->
    catchError(message: "There were some failures when generating the customise comment for $k", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      unstash v
      (new NotificationManager()).customPRComment(commentFile: k, file: v)
    }
  }
}

def analyzeFlaky(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: "notifyBuildResult: Generating flakey test analysis.")
    catchError(message: "There were some failures when generating flakey test results", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      def flakyComment = (new NotificationManager()).analyzeFlakey(args.data)
      args.notifications << flakyComment
    }
  }
}

def notifyGitHubCommandsInPR(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: "notifyGitHubCommandsInPR: Add GitHub comment with the commands.")
    catchError(message: "There were some failures when notifying results in the PR", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      def prComment = (new NotificationManager()).notifyGitHubCommandsInPR(args.data)
      args.notifications << prComment
    }
  }
}

def createGitHubComment(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: "createGitHubComment: Create GitHub comment.")
    catchError(message: "There were some failures when notifying results in the PR", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      def prComment = (new NotificationManager()).notifyPR(args.data)
      args.notifications << prComment
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
    if (suffix?.trim() && !email.contains('+')) {
      return [email.replace('@', "+${suffix}@")]
    } else {
      return [email]
    }
  }
  return []
}

def generateBuildReport(def args=[:]) {
  log(level: 'DEBUG', text: 'notifyBuildResult: Generate build report.')
  catchError(message: "There were some failures when generating the build report", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
    return (new NotificationManager()).generateBuildReport(args.data)
  }
}

def notifyEmail(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: 'notifyBuildResult: Notifying results by email.')
    (new NotificationManager()).notifyEmail(args.data)
  }
}

def notifySlack(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: "notifyBuildResult: Notifying results in slack.")
    catchError(message: "There were some failures when notifying results in slack", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      (new NotificationManager()).notifySlack(args.data)
    }
  }
}

def createGitHubIssue(def args=[:]) {
  if(args.when) {
    log(level: 'DEBUG', text: "notifyBuildResult: Notifying results in github.")
    catchError(message: "There were some failures when notifying results in github", buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
      (new NotificationManager()).createGitHubIssue(args.data)
    }
  }
}
