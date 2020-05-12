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

    catchError(buildResult: 'SUCCESS', message: 'notifyPR: Error sending the email') {
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
      githubPrComment(message: body)
    }
}
