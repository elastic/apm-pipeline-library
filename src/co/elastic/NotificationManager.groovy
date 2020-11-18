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
 * @param querySize The maximum value of results to be reported. Default 500
 * @param queryTimeout Specifies the period of time to wait for a response. Default 20s
 * @param disableGHComment whether to disable the GH comment notification.
*/ 
def analyzeFlakey(Map args = [:]) {
    def es = args.containsKey('es') ? args.es : error('analyzeFlakey: es parameter is not valid')
    def secret = args.containsKey('es_secret') ? args.es_secret : null
    def flakyReportIdx = args.containsKey('flakyReportIdx') ? args.flakyReportIdx : error('analyzeFlakey: flakyReportIdx parameter is not valid')
    def testsErrors = args.containsKey('testsErrors') ? args.testsErrors : []
    def flakyThreshold = args.containsKey('flakyThreshold') ? args.flakyThreshold : 0.0
    def testsSummary = args.containsKey('testsSummary') ? args.testsSummary : null
    def querySize = args.get('querySize', 500)
    def queryTimeout = args.get('queryTimeout', '20s')
    def disableGHComment = args.get('disableGHComment', false)

    def labels = 'flaky-test,ci-reported'
    def boURL = getBlueoceanDisplayURL()
    def flakyTestsWithIssues = [:]
    def genuineTestFailures = []

    if (!flakyReportIdx?.trim()) {
      error 'analyzeFlakey: did not receive flakyReportIdx data'
    }

    // Only if there are test failures to analyse
    if(testsErrors.size() > 0) {

      // Query only the test_name field since it's the only used and don't want to overkill the
      // jenkins instance when using the toJSON step since it reads in memory the json response.
      // for 500 entries it's about 2500 lines versus 8000 lines if no filter_path
      def query = "/${flakyReportIdx}/_search?size=${querySize}&filter_path=hits.hits._source.test_name,hits.hits._index"
      def flakeyTestsRaw = sendDataToElasticsearch(es: es,
                                                  secret: secret,
                                                  data: queryFilter(queryTimeout, flakyThreshold),
                                                  restCall: query)
      def flakeyTestsParsed = toJSON(flakeyTestsRaw)

      // Normalise both data structures with their names
      // Intesection what tests are failing and also scored as flaky.
      // Subset of genuine test failures, aka, those failures that were not scored as flaky previously.
      def testFailures = testsErrors.collect { it.name }
      def testFlaky = flakeyTestsParsed?.hits?.hits?.collect { it['_source']['test_name'] }
      def foundFlakyList = testFlaky?.size() > 0 ? testFailures.intersect(testFlaky) : []
      genuineTestFailures = testFailures.minus(foundFlakyList)
      log(level: 'DEBUG', text: "analyzeFlakey: Flaky tests raw: ${flakeyTestsRaw}")
      log(level: 'DEBUG', text: "analyzeFlakey: Flaky matched tests: ${foundFlakyList.join('\n')}")

      def tests = lookForGitHubIssues(flakyList: foundFlakyList, labelsFilter: labels)
      // To avoid creating a few dozens of issues, let's say we won't create more than 3 issues per build
      def numberOfSupportedIssues = 3
      def numberOfCreatedtedIssues = 0
      tests.each { k, v ->
        def issue = v
        def issueDescription = buildTemplate([
            "template": 'flaky-github-issue.template',
            "testName": k,
            "jobUrl": boURL,
            "PR": env.CHANGE_ID?.trim() ? "#${env.CHANGE_ID}" : '',
            "commit": env.GIT_BASE_COMMIT?.trim() ?: '',
            "testData": testsErrors?.find { it.name.equals(k) }])
        if (v?.trim()) {
          try {
            issueWithoutUrl = v.startsWith('https') ? v.replaceAll('.*/', '') : v
            githubCommentIssue(id: issueWithoutUrl, comment: issueDescription)
          } catch(err) {
            log(level: 'WARN', text: "Something bad happened when commenting the issue '${v}'. See: ${err.toString()}")
          }
        } else {
          def title = "Flaky Test [${k}]"
          try {
            if (numberOfCreatedtedIssues < numberOfSupportedIssues) {
              retryWithSleep(retries: 2, seconds: 5, backoff: true) {
                issue = githubCreateIssue(title: title, description: issueDescription, labels: labels)
              }
              numberOfCreatedtedIssues++
            } else {
              log(level: 'INFO', text: "'${title}' issue has not been created since ${numberOfSupportedIssues} issues has been created.")
            }
          } catch(err) {
            log(level: 'WARN', text: "Something bad happened when creating '${title}' issue. See: ${err.toString()}")
            issue = ''
          } finally {
            if(!issue?.trim()) {
              issue = ''
            }
          }
        }
        flakyTestsWithIssues[k] = issue
      }
    }

    // Decorate comment
    def body = buildTemplate([
      "template": 'flaky-github-comment-markdown.template',
      "flakyTests": flakyTestsWithIssues,
      "jobUrl": boURL,
      "testsErrors": genuineTestFailures,
      "testsSummary": testsSummary
    ])
    writeFile(file: 'flaky.md', text: body)
    if (!disableGHComment) {
      githubPrComment(commentFile: 'flaky.id', message: body)
    }
    archiveArtifacts 'flaky.md'
    return body
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
 * @param disableGHComment whether to disable the GH comment notification.
 */
def notifyPR(Map args = [:]) {
    def build = args.containsKey('build') ? args.build : error('notifyPR: build parameter it is not valid')
    def buildStatus = args.containsKey('buildStatus') ? args.buildStatus : error('notifyPR: buildStatus parameter is not valid')
    def changeSet = args.containsKey('changeSet') ? args.changeSet : []
    def docsUrl = args.get('docsUrl', null)
    def log = args.containsKey('log') ? args.log : null
    def statsUrl = args.containsKey('statsUrl') ? args.statsUrl : ''
    def stepsErrors = args.containsKey('stepsErrors') ? args.stepsErrors : []
    def testsErrors = args.containsKey('testsErrors') ? args.testsErrors : []
    def testsSummary = args.containsKey('testsSummary') ? args.testsSummary : null
    def disableGHComment = args.get('disableGHComment', false)
    def body = ''
    catchError(buildResult: 'SUCCESS', message: 'notifyPR: Error commenting the PR') {
      def statusSuccess = (buildStatus == "SUCCESS")
      def boURL = getBlueoceanDisplayURL()
      body = buildTemplate([
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
      if (!disableGHComment) {
        githubPrComment(commentFile: 'comment.id', message: body)
      }
      archiveArtifacts 'build.md'
    }
    return body
}

/**
 * This method sends a slack message with data from Jenkins
 * @param build
 * @param buildStatus String with job result
 * @param changeSet list of change set, see src/test/resources/changeSet-info.json
 * @param docsUrl URL with the preview docs
 * @param log String that contains the log
 * @param header String that contains a custom header for the message (optional)
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
    def channel = args.containsKey('channel') ? args.channel : error('notifySlack: channel parameter is required')
    def header = args.containsKey('header') ? args.header : ''
    def credentialId = args.containsKey('credentialId') ? args.credentialId : error('notifySlack: credentialId parameter is required')

    if (enabled) {
      catchError(buildResult: 'SUCCESS', message: 'notifySlack: Error with the slack comment') {
        def statusSuccess = (buildStatus == "SUCCESS")
        def boURL = getBlueoceanDisplayURL()
        def body = buildTemplate([
          "header": header,
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
        channel.split(',').each { chan ->
          if (chan?.trim()) {
            // only send to slack when the channel is valid
            slackSend(channel: chan?.trim(), color: color, message: "${body}", tokenCredentialId: credentialId)
          }
        }
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

def queryFilter(timeout, flakyThreshold) {
  return """{
                "timeout": "${timeout}",
                "sort" : [
                  { "timestamp" : "desc" },
                  { "test_score" : "desc" }
                ],
                "query" : {
                  "range" : {
                    "test_score" : {
                      "gt" : ${flakyThreshold}
                    }
                  }
                }
              }"""
}
