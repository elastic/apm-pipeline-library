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

package co.elastic

import groovy.text.StreamingTemplateEngine
import org.jenkinsci.plugins.pipeline.github.trigger.IssueCommentTrigger
import com.cloudbees.groovy.cps.NonCPS

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
 * @param testsSummary object with the test results summary, see src/test/resources/tests-summary.json
 * @param querySize The maximum value of results to be reported. Default 500
 * @param queryTimeout Specifies the period of time to wait for a response. Default 20s
 * @param disableGHComment whether to disable the GH comment notification.
 * @param disableGHIssueCreation whether to disable the GH create issue if any flaky matches.
 * @param jobName
*/
def analyzeFlakey(Map args = [:]) {
    def es = args.containsKey('es') ? args.es : error('analyzeFlakey: es parameter is required')
    def jobName = args.containsKey('jobName') ? args.jobName : error('analyzeFlakey: jobName parameter is required')
    def secret = args.containsKey('es_secret') ? args.es_secret : null
    def testsErrors = args.containsKey('testsErrors') ? args.testsErrors : []
    def testsSummary = args.containsKey('testsSummary') ? args.testsSummary : null
    def querySize = args.get('querySize', 500)
    def disableGHComment = args.get('disableGHComment', false)
    def disableGHIssueCreation = args.get('disableGHIssueCreation', false)
    def labels = 'flaky-test,ci-reported'
    def boURL = getBlueoceanDisplayURL()
    def flakyTestsWithIssues = [:]
    def genuineTestFailures = []

    // 1. Only if there are test failures to analyse
    // 2. Only continue if we have a jobName passed in. This does not raise an error to preserve
    // backward compatibility.
    if(testsErrors.size() > 0 && jobName?.trim()) {

      def query = "/flaky-tests/_search?filter_path=aggregations.test_name.buckets"
      def flakeyTestsRaw = sendDataToElasticsearch(es: es,
                                                  secret: secret,
                                                  data: queryFilter(jobName),
                                                  restCall: query)
      def flakeyTestsParsed = toJSON(flakeyTestsRaw)

      // Normalise both data structures with their names
      // Intesection what tests are failing and also scored as flaky.
      // Subset of genuine test failures, aka, those failures that were not scored as flaky previously.
      def testFailures = testsErrors.collect { it.name }
      def testFlaky = flakeyTestsParsed?.aggregations?.test_name?.buckets?.collect { it['key'] }

      // The following is code which is included here as a possible future enhancement if at some point
      // we wish to include the number of flakes found in the time period. (Currently hard-coded to 90d)
      //
      //def testFlakyFreq = [:]
      //flakeyTestsParsed?.aggregations?.test_name?.buckets?.each { it -> testFlakyFreq[it['key']] = it['doc_count'] }

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
          if (disableGHIssueCreation) {
            log(level: 'INFO', text: "'${title}' issue has not been created since GitHub issues creation has been disabled.")
          } else {
            def data = createFlakyIssue(numberOfSupportedIssues: numberOfSupportedIssues,
                                        numberOfCreatedtedIssues: numberOfCreatedtedIssues,
                                        title: title,
                                        issueDescription: issueDescription,
                                        labels: labels)
            numberOfCreatedtedIssues = data.numberOfCreatedtedIssues
            issue = data.issue
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

def createFlakyIssue(Map args=[:]) {
  def output = ''
  try {
    if (args.numberOfCreatedtedIssues < args.numberOfSupportedIssues) {
      retryWithSleep(retries: 2, seconds: 5, backoff: true) {
        output = githubCreateIssue(title: args.title, description: args.issueDescription, labels: args.labels)
      }
      args.numberOfCreatedtedIssues++
    } else {
      log(level: 'INFO', text: "'${args.title}' issue has not been created since ${args.numberOfSupportedIssues} issues has been created.")
    }
  } catch(err) {
    log(level: 'WARN', text: "Something bad happened when creating '${args.title}' issue. See: ${err.toString()}")
  } finally {
    if(!output?.trim()) {
      output = ''
    }
  }
  return [issue: output, numberOfCreatedtedIssues: args.numberOfCreatedtedIssues]
}

/**
This method generates a custom PR comment with the given data
 * @param file
 * @param commentFile
*/
def customPRComment(Map args = [:]) {
    def file = args.containsKey('file') ? args.file : error('customPRComment: file parameter is required')
    def commentFile = args.containsKey('commentFile') ? args.commentFile : error('customPRComment: commentFile parameter is required')
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
    def build = params.containsKey('build') ? params.build : error('notifyEmail: build parameter is required')
    def buildStatus = params.containsKey('buildStatus') ? params.buildStatus : error('notifyEmail: buildStatus parameter is required')
    def emailRecipients = params.containsKey('emailRecipients') ? params.emailRecipients : error('notifyEmail: emailRecipients parameter is required')
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
 * This method sends a GitHub comment with data from Jenkins. It uses generateBuildReport()
 * @param comment the content of the message, see generateBuildReport()
 * @param disableGHComment whether to disable the GH comment notification.
 * @param see generateBuildReport(), if required to use the previous behaviour
 */
def notifyPR(Map args = [:]) {
    def disableGHComment = args.get('disableGHComment', false)
    def body = args.get('comment', '')

    // In case body is empty let's fallback to the previous behaviour for compatibility reasons.
    if (!body?.trim()) {
      def arguments = args
      arguments['archiveFile'] = false
      body = generateBuildReport(arguments)
    }
    catchError(buildResult: 'SUCCESS', message: 'notifyPR: Error commenting the PR') {
      if (!disableGHComment) {
        githubPrComment(commentFile: 'comment.id', message: body)
      }
    }
    return body
}

/**
 * This method sends a GitHub comment with the GitHub commands that are enabled in the pipeline.
 * @param disableGHComment whether to disable the GH comment notification.
*/
def notifyGitHubCommandsInPR(Map args = [:]) {
    def disableGHComment = args.get('disableGHComment', false)

    // Decorate comment
    def body = buildTemplate([
      "template": 'commands-github-comment-markdown.template',
      "githubCommands": getSupportedGithubCommands()
    ])
    writeFile(file: 'comment.md', text: body)
    catchError(buildResult: 'SUCCESS', message: 'notifyGitHubCommandsInPR: Error commenting the PR') {
      if (!disableGHComment) {
        githubPrComment(commentFile: 'comment.id', message: body)
      }
    }
    archiveArtifacts 'comment.md'
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
    def build = args.containsKey('build') ? args.build : error('notifySlack: build parameter is required')
    def buildStatus = args.containsKey('buildStatus') ? args.buildStatus : error('notifySlack: buildStatus parameter is required')
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
        def duration = (build.durationInMillis?.toString()?.trim()) ? hudson.Util.getTimeSpanString(build.durationInMillis) : ''
        def statusSuccess = (buildStatus == "SUCCESS")
        def boURL = getBlueoceanDisplayURL()
        def body = buildTemplate([
          "header": header,
          "template": 'slack-markdown.template',
          "build": build,
          "buildStatus": buildStatus,
          "changeSet": changeSet,
          "docsUrl": docsUrl,
          "duration": duration,
          "jenkinsText": env.JOB_NAME,
          "jenkinsUrl": env.JENKINS_URL,
          "jobUrl": boURL,
          "log": log,
          "observabilityUrl": env.OTEL_ELASTIC_URL,
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
 * This method generates the build report, archive it and returns the build report
 * @param archiveFile whether to create and archive the file.
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
def generateBuildReport(Map args = [:]) {
    def build = args.containsKey('build') ? args.build : error('generateBuildReport: build parameter is required')
    def buildStatus = args.containsKey('buildStatus') ? args.buildStatus : error('generateBuildReport: buildStatus parameter is required')
    def changeSet = args.get('changeSet', [])
    def docsUrl = args.get('docsUrl', null)
    def log = args.get('log', null)
    def statsUrl = args.get('statsUrl', '')
    def stepsErrors = args.get('stepsErrors', [])
    def testsErrors = args.get('testsErrors', [])
    def testsSummary = args.get('testsSummary', null)
    def archiveFile = args.get('archiveFile', true)
    def output = ''
    catchError(buildResult: 'SUCCESS', message: 'generateBuildReport: Error generating build report') {
      def statusSuccess = (buildStatus == "SUCCESS")
      def boURL = getBlueoceanDisplayURL()
      output = buildTemplate([
        "template": 'github-comment-markdown.template',
        "build": build,
        "buildStatus": buildStatus,
        "changeSet": changeSet,
        "docsUrl": docsUrl,
        "env": env,
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
      if (archiveFile) {
        writeFile(file: 'build.md', text: output)
        archiveArtifacts 'build.md'
      }
    }
    return output
}

def queryFilter(jobName) {
  return """
{
  "aggs": {
    "test_name": {
      "terms": {
        "field": "test.name.keyword",
        "order": {
          "_count": "desc"
        }
      }
    }
  },
  "size": 0,
  "query": {
    "bool": {
      "must": [],
      "filter": [
        {
          "bool": {
            "should": [
              {
                "match_phrase": {
                  "job.fullName": "${jobName}"
                }
              }
            ],
            "minimum_should_match": 1
          }
        },
        {
          "range": {
            "build.startTime": {
              "gte": "now-90d",
              "format": "strict_date_optional_time"
            }
          }
        }
      ],
      "should": [],
      "must_not": []
    }
  }
}
  """
}


/**
 * This method searches for the IssueCommentTrigger in the project itself
 * and if so, then look for the GitHub comment triggers which are supported.
 */
def getSupportedGithubCommands() {
  def issueCommentTrigger = findIssueCommentTrigger()

  if (issueCommentTrigger == null) {
    log(level: 'WARN', text: "No IssueCommentTrigger has been triggered")
    return [:]
  }

  def comments = [:]

  // In order to avoid re-triggering a build when commenting the
  // build status as a PR comment, it's required to filter here
  // what GitHub comment triggers are allowed to be notified in the
  // PR comment.
  // For instance, '^/test' pattern won't trigger a build if the
  // PR comment includes the section for the support trigger comments
  //
  if (issueCommentTrigger.getCommentPattern().contains('^/test')) {
    comments['/test'] = 'Re-trigger the build.'
  }

  // Support for APM server
  if (issueCommentTrigger.getCommentPattern().contains('hey-apm|package')) {
    comments['/hey-apm'] = 'Run the hey-apm benchmark.'
    comments['/package'] = 'Generate and publish the docker images.'
  }

  // Support for benchmark tests
  if (issueCommentTrigger.getCommentPattern().contains('^run benchmark tests')) {
    comments['run benchmark tests'] = 'Run the benchmark test.'
  }

  // Support for the nodejs APM agent
  if (issueCommentTrigger.getCommentPattern().contains('^run (module|benchmark) tests')) {
    comments['run module tests for <modules>'] = 'Run TAV tests for one or more modules, where `<modules>` can be either a comma separated list of modules (e.g. memcached,redis) or the string literal ALL to test all modules'
    comments['run benchmark tests'] = 'Run the benchmark test only.'
  }

  // Support for the java APM agent
  if (issueCommentTrigger.getCommentPattern().contains('^run (compatibility|benchmark|integration)')) {
    comments['run benchmark tests'] = 'Run the benchmark test.'
    comments['run compatibility tests'] = 'Run the JDK Compatibility test.'
    comments['run integration tests'] = 'Run the APM-ITs.'
  }

  // Support for the APM pipeline library
  if (issueCommentTrigger.getCommentPattern().contains('^run infra tests')) {
    comments['run infra tests'] = 'Run the test-infra test.'
  }

  // Support for the Beats specific GitHub commands
  if (isProjectSupported('beats')) {
    comments['/package'] = 'Generate the packages and run the E2E tests.'
    comments['/beats-tester'] = 'Run the installation tests with beats-tester.'
  }

  // Support for the Obs11 test environments specific GitHub commands
  if (isProjectSupported('observability-test-environments')) {
    comments['/test ansible'] = 'Run the ansible tests.'
    comments['/test cypress'] = 'Run the cypress tests.'
    comments['/test tools'] = 'Build and test the CLI tools.'
  }

  if (isProjectSupported('apm-agent-python')) {
    comments['/test linters'] = 'Run the Python linters only.'
    comments['/test full'] = 'Run the full matrix of tests.'
    comments['/test benchmark'] = 'Run the APM Agent Python benchmarks tests.'
  }

  // Support for the elasticsearch-ci/docs GitHub command in all the repositories they use it
  if (isElasticsearchDocsSupported()) {
    // `run` is needed to avoid the comment to trigger a build itself!
    comments['`run` `elasticsearch-ci/docs`'] = 'Re-trigger the docs validation. (use unformatted text in the comment!)'
  }

  return comments
}

private isProjectSupported(String value) {
  return env.REPO?.equals(value) || env.REPO_NAME?.equals(value)
}

private isElasticsearchDocsSupported() {
  return isElasticsearchDocsSupported(env.REPO) || isElasticsearchDocsSupported(env.REPO_NAME)
}

private isElasticsearchDocsSupported(String value) {
  return value?.startsWith('apm') ||
         value?.startsWith('ecs') ||
         value?.equals('beats') ||
         value?.equals('observability-docs')
}

@NonCPS
def findIssueCommentTrigger() {
  return currentBuild.rawBuild.getParent().getTriggers().collect { it.value }.find { it instanceof IssueCommentTrigger }
}
