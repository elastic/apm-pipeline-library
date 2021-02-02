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
  def script
  def f

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('src/co/elastic/NotificationManager.groovy')
    f = new File("src/test/resources/console-100-lines.log")
    env.TEST = "test"
  }

  @Test
  void test() throws Exception {
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
    env.TEST = "testNoBuildInfo"
    testMissingArgument('build') {
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
    }
  }

  @Test
  void testNoBuildStatus() throws Exception {
    env.TEST = "testNoBuildStatus"
    testMissingArgument('buildStatus') {
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
    }
  }

  @Test
  void testNoEmailRecipients() throws Exception {
    env.TEST = "testNoEmailRecipients"
    testMissingArgument('emailRecipients') {
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
    }
  }

  @Test
  void test_notify_pr() throws Exception {
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
    env.TEST = "testNoBuildStatus"
    testMissingArgument('buildStatus') {
      script.notifyPR(
        build: readJSON(file: "build-info.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        log: f.getText(),
        statsUrl: "https://ecs.example.com/app/kibana",
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json")
      )
    }
  }

  @Test
  void testNoBuildInfo_notify_pr() throws Exception {
    env.TEST = "testNoBuildInfo"
    testMissingArgument('build') {
      script.notifyPR(
        buildStatus: "FAILURE",
        changeSet: readJSON(file: "changeSet-info.json"),
        log: f.getText(),
        statsUrl: "https://ecs.example.com/app/kibana",
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"))
    }
  }

  @Test
  void test_notify_pr_with_aborted_but_no_cancel_build() throws Exception {
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
    assertTrue(assertMethodCallPatternOccurrences('githubPrComment', 'Error signal', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unstable_and_long_stacktraces() throws Exception {
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "UNSTABLE",
      changeSet: [],
      log: f.getText(),
      stepsErrors: [],
      testsErrors: readJSON(file: "tests-errors-with-long-stacktrace.json"),
      testsSummary: readJSON(file: 'tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Expand to view the error details'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Expand to view the stacktrace'))
    // When one of the fields is empty/null then it should not be shown
    assertTrue(assertMethodCallContainsPattern('githubPrComment', '<li>no error details</li>'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_unknown() throws Exception {
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
  void test_notify_pr_without_comment_notifications() throws Exception {
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "SUCCESS",
      changeSet: readJSON(file: "changeSet-info.json"),
      disableGHComment: true,
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertTrue(assertMethodCallOccurrences('githubPrComment', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_corrupted_builds() throws Exception {
    script.notifyPR(
      build: readJSON(file: "corrupted/build-info.json"),
      buildStatus: "SUCCESS",
      changeSet: readJSON(file: "corrupted/changeSet-info.json"),
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "corrupted/steps-errors.json"),
      testsErrors: readJSON(file: "corrupted/tests-errors.json"),
      testsSummary: readJSON(file: "corrupted/tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'Build Cause'))
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'Reason'))
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'Start Time'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Succeeded'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_failure_and_github_environmental_issue() throws Exception {
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "FAILURE",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors-with-github-environmental-issue.json"),
      testsErrors: [],
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Notifies GitHub of the status of a Pull Request'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_a_generated_comment() throws Exception {
    script.notifyPR(comment: 'My Comment')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'My Comment'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_a_null_comment() throws Exception {
    script.notifyPR(comment: null,
                    build: readJSON(file: "build-info.json"),
                    buildStatus: "FAILURE",
                    changeSet: readJSON(file: "changeSet-info.json"),
                    log: f.getText(),
                    statsUrl: "https://ecs.example.com/app/kibana",
                    stepsErrors: readJSON(file: "steps-errors-with-github-environmental-issue.json"),
                    testsErrors: [],
                    testsSummary: readJSON(file: "tests-summary.json"))
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_pr_with_failure_and_github_environmental_issue_and_further_errors() throws Exception {
    script.notifyPR(
      build: readJSON(file: "build-info.json"),
      buildStatus: "FAILURE",
      changeSet: readJSON(file: "changeSet-info.json"),
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors-with-also-github-environmental-issue.json"),
      testsErrors: [],
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Failed'))
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'Notifies GitHub of the status of a Pull Request'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_aborted_but_no_cancel_build() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('slackSend', 'Steps failures'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_aborted_but_no_cancel_build_and_disabled() throws Exception {
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
  void test_notify_slack_with_multiple_channels() throws Exception {
    try {
      script.notifySlack(
        build: readJSON(file: "build-info-manual.json"),
        buildStatus: "SUCCESS",
        changeSet: readJSON(file: "changeSet-info-manual.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        channel: 'foo, bar, baaz',
        credentialId: 'test',
        enabled: true
      )
    }
    catch(e) {
      println e
    }
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifySlack: Error with the slack comment'))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=foo'))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=bar'))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=baaz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_with_multiple_wrong_channels() throws Exception {
    try {
      script.notifySlack(
        build: readJSON(file: "build-info-manual.json"),
        buildStatus: "SUCCESS",
        changeSet: readJSON(file: "changeSet-info-manual.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        channel: ',', // valid for the iterator but not for valid channels
        credentialId: 'test',
        enabled: true
      )
    }
    catch(e) {
      println e
    }
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifySlack: Error with the slack comment'))
    assertTrue(assertMethodCallOccurrences('slackSend', 0))
    assertJobStatusSuccess()
  }


  @Test
  void test_notify_slack_without_steps_failures() throws Exception {
    script.notifySlack(
      build: readJSON(file: "build-info-manual.json"),
      buildStatus: "SUCCESS",
      changeSet: [],
      stepsErrors: [],
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json"),
      channel: 'test',
      credentialId: 'test',
      enabled: true
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('slackSend', 'Build'))
    assertFalse(assertMethodCallContainsPattern('slackSend', 'Steps failures'))
    assertJobStatusSuccess()
  }

  @Test
  void test_notify_slack_without_build() throws Exception {
    testMissingArgument('build') {
      script.notifySlack(
        buildStatus: "ABORTED",
        changeSet: readJSON(file: "changeSet-info.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        channel: 'test',
        credentialId: 'test',
        enabled: true
      )
    }
  }

  @Test
  void test_notify_slack_without_build_status() throws Exception {
    testMissingArgument('buildStatus') {
      script.notifySlack(
        build: readJSON(file: "build-info.json"),
        changeSet: readJSON(file: "changeSet-info.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        channel: 'test',
        credentialId: 'test',
        enabled: true
      )
    }
  }

  @Test
  void test_notify_slack_without_channel() throws Exception {
    testMissingArgument('channel') {
      script.notifySlack(
        build: readJSON(file: "build-info.json"),
        buildStatus: "ABORTED",
        changeSet: readJSON(file: "changeSet-info.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        credentialId: 'test',
        enabled: true
      )
    }
  }

  @Test
  void test_notify_slack_without_credentialId() throws Exception {
    testMissingArgument('credentialId') {
      script.notifySlack(
        build: readJSON(file: "build-info.json"),
        buildStatus: "ABORTED",
        changeSet: readJSON(file: "changeSet-info.json"),
        stepsErrors: readJSON(file: "steps-errors.json"),
        testsErrors: readJSON(file: "tests-errors.json"),
        testsSummary: readJSON(file: "tests-summary.json"),
        channel: 'test',
        enabled: true
      )
    }
  }

  @Test
  void test_analyzeFlakey_in_prs_with_empty_flaky_tests() throws Exception {
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: 'flake-empty-results.json')})
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors-without-match.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "There are test failures but not known flaky tests."))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "Genuine test errors [![1]"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_flaky_tests() throws Exception {
    helper.registerAllowedMethod(
      "sendDataToElasticsearch",
      [Map.class],
      {m -> readJSON(file: "flake-results.json")}
    )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors-without-match.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json')
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "There are test failures but not known flaky tests."))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "Genuine test errors [![1]"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests_already_reported() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `bar`"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `Test / windows-3.6-none / test_send - tests.transports.test_urllib3`"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`bar` reported in the issue #100"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `bar`"))
    assertFalse(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `Test / windows-3.6-none / test_send - tests.transports.test_urllib3`"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`bar` reported in the issue #100"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`Test / windows-3.6-none / test_send - tests.transports.test_urllib3` reported in the issue #200"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_more_than_3_flaky_tests_not_reported_yet() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `test-a`"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `test-b`"))
    assertTrue(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `test-c`"))
    assertFalse(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `test-d`"))
    assertTrue(assertMethodCallContainsPattern('log', "'Flaky Test [test-d]'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_with_flaky_tests_and_disabled_issue_creation() throws Exception {
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], { return [ bar: '' ] })
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: readJSON(file: 'flake-tests-errors.json'),
      testsSummary: readJSON(file: 'flake-tests-summary.json'),
      disableGHIssueCreation: true
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('githubCreateIssue', "**Test Name:** `bar`"))
    assertTrue(assertMethodCallContainsPattern('log', "issue has not been created since GitHub issues creation has been disabled."))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "The following tests failed"))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "`bar` has not been reported yet"))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_failed_tests() throws Exception {
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
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 0))
    assertTrue(assertMethodCallContainsPattern('githubPrComment', "Tests succeeded."))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakey_in_prs_without_executed_tests() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('sendDataToElasticsearch', '"gt" : 0.5'))
    assertJobStatusSuccess()
  }

  @Test
  void test_analyzeFlakeyNoJobInfo() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('error', 'analyzeFlakey: did not receive flakyReportIdx data'))
    assertJobStatusFailure()
  }

  @Test
  void test_analyzeFlakey_without_comment_notifications() throws Exception {
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], {readJSON(file: "flake-results.json")})
    helper.registerAllowedMethod('lookForGitHubIssues', [Map.class], { return [:] } )
    helper.registerAllowedMethod('isPR', { return true })
    script.analyzeFlakey(
      disableGHComment: true,
      flakyReportIdx: 'reporter-apm-agent-python-apm-agent-python-master',
      es: "https://fake_url",
      testsErrors: [:],
      testsSummary: [:]
    )
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubPrComment', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_generateBuildReport() throws Exception {
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
  void test_generateBuildReport_without_archive() throws Exception {
    script.generateBuildReport(
      build: readJSON(file: 'build-info.json'),
      buildStatus: 'SUCCESS',
      changeSet: readJSON(file: 'changeSet-info.json'),
      docsUrl: 'foo',
      log: f.getText(),
      statsUrl: 'https://ecs.example.com/app/kibana',
      stepsErrors: readJSON(file: 'steps-errors.json'),
      testsErrors: readJSON(file: 'tests-errors.json'),
      testsSummary: readJSON(file: 'tests-summary.json'),
      archiveFile: false
    )
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('writeFile', 'file=build.md'))
    assertFalse(assertMethodCallContainsPattern('archiveArtifacts', 'build.md'))
    assertJobStatusSuccess()
  }

  @Test
  void test_customPRComment_without_file_argument() throws Exception {
    testMissingArgument('file') {
      script.customPRComment()
    }
  }

  @Test
  void test_customPRComment_without_commentFile_argument() throws Exception {
    testMissingArgument('commentFile') {
      script.customPRComment(file: 'foo')
    }
  }

  @Test
  void test_customPRComment() throws Exception {
    helper.registerAllowedMethod('readFile', [Map.class], { 'awesome' })
    script.customPRComment(file: 'foo', commentFile: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'message=awesome'))
    assertJobStatusSuccess()
  }
}
