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

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class NotificationManagerStepTests extends ApmBasePipelineTest {
  String scriptName = 'src/co/elastic/NotificationManager.groovy'
  def f

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    f = new File("src/test/resources/console-100-lines.log")
    env.TEST = "test"
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.notifyEmail(
      build: readJSON(file: "build-info.json"),
      buildStatus: "SUCCESS",
      emailRecipients: ["me@example.com"],
      testsSummary: readJSON(file: "tests-summary.json"),
      changeSet: readJSON(file: "changeSet-info.json"),
      statsUrl: "https://ecs.example.com/app/kibana",
      log: f.getText(),
      testsErrors: readJSON(file: "tests-errors.json"),
      stepsErrors: readJSON(file: "steps-errors.json")
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifyEmail: Error sending the email -'))
    assertJobStatusSuccess()
  }

  @Test
  void testMinParams() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testMinParams"
    script.notifyEmail(
      build: readJSON(file: "build-info.json"),
      buildStatus: "SUCCESS",
      emailRecipients: ["me@example.com"]
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'Error sending the email -'))
    assertJobStatusSuccess()
  }

  @Test
  void testFAILURE() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testFAILURE"
    script.notifyEmail(
      build: readJSON(file: "build-info.json"),
      buildStatus: "FAILURE",
      emailRecipients: ["me@example.com"],
      testsSummary: readJSON(file: "tests-summary.json"),
      changeSet: readJSON(file: "changeSet-info.json"),
      statsUrl: "https://ecs.example.com/app/kibana",
      log: f.getText(),
      testsErrors: readJSON(file: "tests-errors.json"),
      stepsErrors: readJSON(file: "steps-errors.json")
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifyEmail: Error sending the email -'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoBuildInfo() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testNoBuildInfo"
    try{
      script.notifyEmail(
        buildStatus: "FAILURE",
        emailRecipients: ["me@example.com"],
        testsSummary: readJSON(file: "tests-summary.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        statsUrl: "https://ecs.example.com/app/kibana",
        log: f.getText(),
        testsErrors: readJSON(file: "tests-errors.json"),
        stepsErrors: readJSON(file: "steps-errors.json")
      )
    } catch(e){
      //NOOP
      println e.toString()
      e.printStackTrace(System.out);
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'notifyEmail: build parameter it is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void testNoBuildStatus() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testNoBuildStatus"
    try{
      script.notifyEmail(
        build: readJSON(file: "build-info.json"),
        emailRecipients: ["me@example.com"],
        testsSummary: readJSON(file: "tests-summary.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        statsUrl: "https://ecs.example.com/app/kibana",
        log: f.getText(),
        testsErrors: readJSON(file: "tests-errors.json"),
        stepsErrors: readJSON(file: "steps-errors.json")
      )
    } catch(e){
      //NOOP
      println e.toString()
      e.printStackTrace(System.out);
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'notifyEmail: buildStatus parameter is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void testNoEmailRecipients() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testNoEmailRecipients"
    try{
      script.notifyEmail(
        build: readJSON(file: "build-info.json"),
        buildStatus: "FAILURE",
        testsSummary: readJSON(file: "tests-summary.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        statsUrl: "https://ecs.example.com/app/kibana",
        log: f.getText(),
        testsErrors: readJSON(file: "tests-errors.json"),
        stepsErrors: readJSON(file: "steps-errors.json")
      )
    } catch(e){
      //NOOP
      println e.toString()
      e.printStackTrace(System.out);
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'notifyEmail: emailRecipients parameter is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void test_notify_pr() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "SUCCESS",
      changeSet: readJSON(file: "changeSet-info.json"),
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'badge/docs-preview'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Succeeded'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoBuildStatus_notify_pr() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testNoBuildStatus"
    try{
      script.notifyPR(
        build: readJSON(file: "build-info.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        log: f.getText(),
        statsUrl: "https://ecs.example.com/app/kibana",
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json")
      )
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'notifyPR: buildStatus parameter is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void testNoBuildInfo_notify_pr() throws Exception {
    def script = loadScript(scriptName)
    env.TEST = "testNoBuildInfo"
    try {
      script.notifyPR(
        buildStatus: "FAILURE",
        changeSet: readJSON(file: "changeSet-info.json"),
        log: f.getText(),
        statsUrl: "https://ecs.example.com/app/kibana",
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"))
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'notifyPR: build parameter it is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void test_notify_pr_with_aborted_but_no_cancel_build() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "ABORTED",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Aborted'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', '> Either there was a build timeout or someone aborted the build.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_aborted() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info_aborted.json"),
      buildStatus: "ABORTED",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Aborted'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', '> There is a new build on-going so the previous on-going builds have been aborted'))
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'badge/docs-preview'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_failure() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "FAILURE",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unstable() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "UNSTABLE",
      changeSet: readJSON(file: "changeSet-info.json"),
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary_failed.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Tests Failed'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Test errors [![1]'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Steps errors [![3]'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'badge/docs-preview'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unstable_and_no_tests_failures() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "UNSTABLE",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unstable_and_multiple_steps_failures() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "UNSTABLE",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors-with-multiple.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Show only the first 10 steps failures'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unknown() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "UNKNOWN",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_foo() throws Exception {
    def script = loadScript(scriptName)
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "foo",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_aborted_but_no_cancel_build() throws Exception {
    def script = loadScript(scriptName)
    script.notifySlack(
      build: readJSON(file: "build-info.json"),
      buildStatus: "ABORTED",
      changeSet: readJSON(file: "changeSet-info.json"),
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json"),
      channel: 'test',
      credentialId: 'test',
      enabled: true
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('slackSend', 'ABORTED'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_aborted_but_no_cancel_build_and_disabled() throws Exception {
    def script = loadScript(scriptName)
    script.notifySlack(
      build: readJSON(file: "build-info.json"),
      buildStatus: "ABORTED",
      channel: 'test',
      credentialId: 'test',
      enabled: false
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('slackSend', 'ABORTED'))
    assertFalse(assertMethodCallContainsPattern('slackSend', '*Changes*: No push event to branch'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_aborted_and_no_commit_history() throws Exception {
    def script = loadScript(scriptName)
    script.notifySlack(
      build: readJSON(file: "build-info-manual.json"),
      buildStatus: "ABORTED",
      changeSet: readJSON(file: "changeSet-info-manual.json"),
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json"),
      channel: 'test',
      credentialId: 'test',
      enabled: true
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('slackSend', 'ABORTED'))
    assertTrue(assertMethodCallContainsPattern('slackSend', '*Changes*: No push event to branch'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_header() throws Exception {
    def script = loadScript(scriptName)
    script.notifySlack(
      build: readJSON(file: "build-info-manual.json"),
      buildStatus: "SUCCESS",
      changeSet: readJSON(file: "changeSet-info-manual.json"),
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json"),
      header: '*Header*: this is a header',
      channel: 'test',
      credentialId: 'test',
      enabled: true
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('slackSend', '*Header*: this is a header'))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_flaky_tests() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod(
      "sendDataToElasticsearch",
      [Map.class],
      {m -> readJSON(file: "flake-results.json")}
    )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "There are test failures but not known flaky tests."))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests_already_reported() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], {
      return [ foo: '123',bar: '456' ]
      }
    )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubCreateIssue', 0))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`foo` reported in the issue #123"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests_not_reported_yet() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], {
      return [ 'Test / windows-3.6-none / test_send - tests.transports.test_urllib3': '',
               bar: '' ]
      }
    )
    helper.registerAllowedMethod('githubCreateIssue', [Map.class], { return '100' } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** bar"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** Test / windows-3.6-none / test_send - tests.transports.test_urllib3"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`bar` reported in the issue #100"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], {
      return [ 'Test / windows-3.6-none / test_send - tests.transports.test_urllib3': '200',
               bar: '' ]
      }
    )
    helper.registerAllowedMethod('githubCreateIssue', [Map.class], { return '100' } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** bar"))
    assertFalse(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** Test / windows-3.6-none / test_send - tests.transports.test_urllib3"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`bar` reported in the issue #100"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`Test / windows-3.6-none / test_send - tests.transports.test_urllib3` reported in the issue #200"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_more_than_3_flaky_tests_not_reported_yet() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], {
      return [ 'test-a': '', 'test-b': '', 'test-c': '', 'test-d': '' ]
      }
    )
    helper.registerAllowedMethod('githubCreateIssue', [Map.class], { return '100' } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** test-a"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** test-b"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** test-c"))
    assertFalse(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** test-d"))
    assertTrue(assertMethodCallContainsPattern('log', "'Flaky Test [test-d]'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_failed_tests() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], { return [:] } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: [:],
      testsSummary: [ "failed": 0, "passed": 120, "skipped": 0, "total": 120 ]
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "Tests succeeded."))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_executed_tests() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], { return [:] } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: [:],
      testsSummary: [:]
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "No test was executed to be analysed."))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakeyThreshold() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod(
      "sendDataToElasticsearch",
      [Map.class],
      {m -> readJSON(file: "flake-results.json")}
    )
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      flakyThreshold: 0.5
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('toJSON', "0.5"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakeyNoJobInfo() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.analyzeFlakey(
        flakyReportIdx: '',
        es: "https://fake_url",
        testsErrors: readJSON(file: 'flake-tests-errors.json')
      )
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Did not receive flakyReportIdx data'))
    assertJobStatusFailure()
  }

  @Test
  void test_generateBuildReport() throws Exception {
    def script = loadScript(scriptName)
    script.generateBuildReport(
      build: readJSON(file: 'build-info.json'),
      buildStatus: 'SUCCESS',
      changeSet: readJSON(file: 'changeSet-info.json'),
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: 'https://ecs.example.com/app/kibana',
      stepsErrors: readJSON(file: 'steps-errors.json'),
      testsErrors: readJSON(file: 'tests-errors.json'),
      testsSummary: readJSON(file: 'tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'badge/docs-preview'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'Build Succeeded'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=build.md'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'build.md'))
    assertJobStatusSuccess()
  }

  @Test
  void test_customPRComment_without_file_argument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.customPRComment()
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'file parameter is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void test_customPRComment_without_commentFile_argument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.customPRComment(file: 'foo')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'commentFile parameter is not valid'))
    assertJobStatusFailure()
  }

  @Test
  void test_customPRComment() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('readFile', [Map.class], { 'awesome' })
    script.customPRComment(file: 'foo', commentFile: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'message=awesome'))
    assertJobStatusSuccess()
  }

  @Test
  void test_scrubName() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.scrubName('a"b*c\\d<e|f,g>h/i?j%20k', '-')
    printCallStack()
    assertTrue(ret.equals('a-b-c-d-e-f-g-h-i-j-k'))
    assertJobStatusSuccess()
  }
}
