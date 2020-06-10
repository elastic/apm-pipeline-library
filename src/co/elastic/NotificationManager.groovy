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
*/ 
def analyzeFlakey(Map params = [:]) {
    def es = params.containsKey('es') ? params.es : error('analyzeFlakey: es parameter is not valid') 
    def secret = params.containsKey('es_secret') ? params.es_secret : null
    def jobInfo = params.containsKey('jobInfo') ? params.jobInfo : error('analyzeFlakey: jobInfo parameter is not valid')
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []
    
    def q = toJSON(["query":["range": ["test_score": ["gt": 0.0]]]])
    def c = '/' + jobInfo['fullName'] + '/_search'
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
    def msg = "❄️ The following tests failed but also have a history of flakiness and may not be related to this change: " + ret.toString()
    
    if (ret) {
      githubPrComment(message: msg)
    } 
    
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
      githubPrComment(message: body)
      archiveArtifacts 'build.md'
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
