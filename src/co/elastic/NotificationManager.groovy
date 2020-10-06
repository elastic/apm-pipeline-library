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

package co.elastic;

import groovy.text.StreamingTemplateEngine

/**
 * This method returns a string with the template filled with groovy variables
 */
def buildTemplate(params) {
    def template = params.containsKey('template') ? params.template : 'groovy-html-custom.template'
    def fileContents = libraryResource(template)
    def engine = new StreamingTemplateEngine()
    return engine.createTemplate(fileContents).make(params).toString()
}

/**
This method generates flakey test data from Jenkins test results
 * @param es Elasticsearch URL
 * @param secret Vault path to secrets which hold authentication information for Elasticsearch
 * @param jobInfo JobInfo data collected from job-info.json
 * @param testsErrors list of test failed, see src/test/resources/tests-errors.json
 * @param flakyReportIdx, what's the id.
 * @param flakyThreshold, to tweak the score.
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
*/ 
def analyzeFlakey(Map params = [:]) {
    def es = params.containsKey('es') ? params.es : error('analyzeFlakey: es parameter is not valid') 
    def secret = params.containsKey('es_secret') ? params.es_secret : null
    def flakyReportIdx = params.containsKey('flakyReportIdx') ? params.flakyReportIdx : error('analyzeFlakey: flakyReportIdx parameter is not valid')
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []
    def flakyThreshold = params.containsKey('flakyThreshold') ? params.flakyThreshold : 0.0
    def testsSummary = params.containsKey('testsSummary') ? params.testsSummary : null

    if (!flakyReportIdx || !flakyReportIdx.trim()) {
      error "Did not receive flakyReportIdx data" 
    }

    def q = toJSON(["query":["range": ["test_score": ["gt": flakyThreshold]]]])
    def c = '/' + flakyReportIdx + '/_search'
    def flakeyTestsRaw = sendDataToElasticsearch(es: es, secret: secret, data: q, restCall: c)
    def flakeyTestsParsed = toJSON(flakeyTestsRaw)

    def ret = []

    for (failedTest in testsErrors) {
      for (flakeyTest in flakeyTestsParsed["hits"]["hits"]) {
        if ((flakeyTest["_source"]["test_name"] == failedTest.name) && !(failedTest.name in ret)) {
          ret.add(failedTest.name)
        }
      }
    }

    def labels = 'flaky-test,ci-reported'
    def tests = lookForGitHubIssues(flakeyList: ret, labelsFilter: labels.split(','))
    // Create issues if they were not created
    def boURL = getBlueoceanDisplayURL()
    def flakyTestsWithIssues = [:]
    tests.each { k, v ->
      def issue = v
      if (!v?.trim()) {
        def issueDescription = buildTemplate([
          "template": 'flakey-github-issue.template',
          "testName": k,
          "jobUrl": boURL,
          "testData": testsErrors?.find { it.name.equals(k) }
        ])
        try {
          retryWithSleep(retries: 2, seconds: 5, backoff: true) {
            issue = githubCreateIssue(title: "Flaky Test [${k}]", description: issueDescription, labels: labels, returnStdout: true)
          }
        } catch(err) {
          issue = ''
        } finally {
          if(!issue?.trim()) {
            issue = ''
          }
        }
      }
      flakyTestsWithIssues[k] = issue
    }

    // Decorate comment
    def body = buildTemplate([
      "template": 'flakey-github-comment-markdown.template',
      "tests": flakyTestsWithIssues,
      "testsSummary": testsSummary
    ])
    writeFile(file: 'flakey.md', text: body)
    githubPrComment(commentFile: 'flakey.id', message: body)
    archiveArtifacts 'flakey.md'
}

/**
This method generates a custom PR comment with the given data
 * @param file
 * @param commentFile
*/
def customPRComment(Map args = [:]) {
    def file = args.containsKey('file') ? args.file : error('customPRComment: file parameter is not valid')
    def commentFile = args.containsKey('commentFile') ? args.commentFile : error('customPRComment: commentFile parameter is not valid')
    def msg = readFile(file: file)
    githubPrComment(message: msg, commentFile: "${commentFile}")
}

/**
 * This method send an email generated with data from Jenkins
 * @param build
 * @param buildStatus String with job result
 * @param emailRecipients Array with emails: emailRecipients = []
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
 * @param changeSet list of change set, see src/test/resources/changeSet-info.json
 * @param statsUrl URL to access to the stats
 * @param log String that contains the log
 * @param stepsErrors list of steps failed, see src/test/resources/steps-errors.json
 * @param testsErrors list of test failed, see src/test/resources/tests-errors.json
 */
def notifyEmail(Map params = [:]) {
    def build = params.containsKey('build') ? params.build : error('notifyEmail: build parameter it is not valid')
    def buildStatus = params.containsKey('buildStatus') ? params.buildStatus : error('notifyEmail: buildStatus parameter is not valid')
    def emailRecipients = params.containsKey('emailRecipients') ? params.emailRecipients : error('notifyEmail: emailRecipients parameter is not valid')
    def testsSummary = params.containsKey('testsSummary') ? params.testsSummary : null
    def changeSet = params.containsKey('changeSet') ? params.changeSet : []
    def statsUrl = params.containsKey('statsUrl') ? params.statsUrl : ''
    def log = params.containsKey('log') ? params.log : null
    def stepsErrors = params.containsKey('stepsErrors') ? params.stepsErrors : []
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []

    catchError(buildResult: 'SUCCESS', message: 'notifyEmail: Error sending the email') {
      def icon = "✅"
      def statusSuccess = true

      if(buildStatus != "SUCCESS") {
          icon = "❌"
          statusSuccess = false
      }

      def boURL = getBlueoceanDisplayURL()

      def body = buildTemplate([
          "jobUrl": boURL,
          "build": build,
          "jenkinsText": env.JOB_NAME,
          "jenkinsUrl": env.JENKINS_URL,
          "statusSuccess": statusSuccess,
          "testsSummary": testsSummary,
          "changeSet": changeSet,
          "statsUrl": statsUrl,
          "log": log,
          "stepsErrors": stepsErrors,
          "testsErrors": testsErrors
      ]);

      mail(to: emailRecipients.join(","),
        subject: "${icon} ${buildStatus} ${env.JOB_NAME}#${env.BRANCH_NAME ? env.BRANCH_NAME : ''} ${env.BUILD_NUMBER}",
        body: body,
        mimeType: 'text/html'
      );
    }
}

/**
 * This method sends a GitHub comment with data from Jenkins
 * @param build
 * @param buildStatus String with job result
 * @param changeSet list of change set, see src/test/resources/changeSet-info.json
 * @param docsUrl URL with the preview docs
 * @param log String that contains the log
 * @param statsUrl URL to access to the stats
 * @param stepsErrors list of steps failed, see src/test/resources/steps-errors.json
 * @param testsErrors list of test failed, see src/test/resources/tests-errors.json
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
 */
def notifyPR(Map params = [:]) {
    def build = params.containsKey('build') ? params.build : error('notifyPR: build parameter it is not valid')
    def buildStatus = params.containsKey('buildStatus') ? params.buildStatus : error('notifyPR: buildStatus parameter is not valid')
    def changeSet = params.containsKey('changeSet') ? params.changeSet : []
    def docsUrl = params.get('docsUrl', null)
    def log = params.containsKey('log') ? params.log : null
    def statsUrl = params.containsKey('statsUrl') ? params.statsUrl : ''
    def stepsErrors = params.containsKey('stepsErrors') ? params.stepsErrors : []
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []
    def testsSummary = params.containsKey('testsSummary') ? params.testsSummary : null

    catchError(buildResult: 'SUCCESS', message: 'notifyPR: Error commenting the PR') {
      def statusSuccess = (buildStatus == "SUCCESS")
      def boURL = getBlueoceanDisplayURL()
      def body = buildTemplate([
        "template": 'github-comment-markdown.template',
        "build": build,
        "buildStatus": buildStatus,
        "changeSet": changeSet,
        "docsUrl": docsUrl,
        "jenkinsText": env.JOB_NAME,
        "jenkinsUrl": env.JENKINS_URL,
        "jobUrl": boURL,
        "log": log,
        "statsUrl": statsUrl,
        "statusSuccess": statusSuccess,
        "stepsErrors": stepsErrors,
        "testsErrors": testsErrors,
        "testsSummary": testsSummary
      ])
      writeFile(file: 'build.md', text: body)
      githubPrComment(commentFile: 'comment.id', message: body)
      archiveArtifacts 'build.md'
    }
}

/**
 * This method sends a slack message with data from Jenkins
 * @param build
 * @param buildStatus String with job result
 * @param changeSet list of change set, see src/test/resources/changeSet-info.json
 * @param docsUrl URL with the preview docs
 * @param log String that contains the log
 * @param statsUrl URL to access to the stats
 * @param stepsErrors list of steps failed, see src/test/resources/steps-errors.json
 * @param testsErrors list of test failed, see src/test/resources/tests-errors.json
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
 */
def notifySlack(Map args = [:]) {
    def build = args.containsKey('build') ? args.build : error('notifySlack: build parameter it is not valid')
    def buildStatus = args.containsKey('buildStatus') ? args.buildStatus : error('notifySlack: buildStatus parameter is not valid')
    def changeSet = args.containsKey('changeSet') ? args.changeSet : []
    def docsUrl = args.get('docsUrl', null)
    def log = args.containsKey('log') ? args.log : null
    def statsUrl = args.containsKey('statsUrl') ? args.statsUrl : ''
    def stepsErrors = args.containsKey('stepsErrors') ? args.stepsErrors : []
    def testsErrors = args.containsKey('testsErrors') ? args.testsErrors : []
    def testsSummary = args.containsKey('testsSummary') ? args.testsSummary : null
    def enabled = args.get('enabled', false)
    def channel = args.containsKey('channel') ? args.channel : error('notifySlack: channel parameter is not required')
    def credentialId = args.containsKey('credentialId') ? args.credentialId : error('notifySlack: credentialId parameter is not required')

    if (enabled) {
      catchError(buildResult: 'SUCCESS', message: 'notifySlack: Error with the slack comment') {
        def statusSuccess = (buildStatus == "SUCCESS")
        def boURL = getBlueoceanDisplayURL()
        def body = buildTemplate([
          "template": 'slack-markdown.template',
          "build": build,
          "buildStatus": buildStatus,
          "changeSet": changeSet,
          "docsUrl": docsUrl,
          "jenkinsText": env.JOB_NAME,
          "jenkinsUrl": env.JENKINS_URL,
          "jobUrl": boURL,
          "log": log,
          "statsUrl": statsUrl,
          "statusSuccess": statusSuccess,
          "stepsErrors": stepsErrors,
          "testsErrors": testsErrors,
          "testsSummary": testsSummary
        ])
        def color = (buildStatus == "SUCCESS") ? 'good' : 'warning'
        slackSend(channel: channel, color: color, message: "${body}", tokenCredentialId: credentialId)
      }
    }
}

/**
 * This method generates the build report and archive it
 * @param build
 * @param buildStatus String with job result
 * @param changeSet list of change set, see src/test/resources/changeSet-info.json
 * @param docsUrl URL with the preview docs
 * @param log String that contains the log
 * @param statsUrl URL to access to the stats
 * @param stepsErrors list of steps failed, see src/test/resources/steps-errors.json
 * @param testsErrors list of test failed, see src/test/resources/tests-errors.json
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
 */
def generateBuildReport(Map params = [:]) {
    def build = params.containsKey('build') ? params.build : error('generateBuildReport: build parameter it is not valid')
    def buildStatus = params.containsKey('buildStatus') ? params.buildStatus : error('generateBuildReport: buildStatus parameter is not valid')
    def changeSet = params.containsKey('changeSet') ? params.changeSet : []
    def docsUrl = params.get('docsUrl', null)
    def log = params.containsKey('log') ? params.log : null
    def statsUrl = params.containsKey('statsUrl') ? params.statsUrl : ''
    def stepsErrors = params.containsKey('stepsErrors') ? params.stepsErrors : []
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []
    def testsSummary = params.containsKey('testsSummary') ? params.testsSummary : null

    catchError(buildResult: 'SUCCESS', message: 'generateBuildReport: Error generating build report') {
      def statusSuccess = (buildStatus == "SUCCESS")
      def boURL = getBlueoceanDisplayURL()
      def body = buildTemplate([
        "template": 'github-comment-markdown.template',
        "build": build,
        "buildStatus": buildStatus,
        "changeSet": changeSet,
        "docsUrl": docsUrl,
        "jenkinsText": env.JOB_NAME,
        "jenkinsUrl": env.JENKINS_URL,
        "jobUrl": boURL,
        "log": log,
        "statsUrl": statsUrl,
        "statusSuccess": statusSuccess,
        "stepsErrors": stepsErrors,
        "testsErrors": testsErrors,
        "testsSummary": testsSummary
      ])
      writeFile(file: 'build.md', text: body)
      archiveArtifacts 'build.md'
    }
}
