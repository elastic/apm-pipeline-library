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
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'github-comment-markdown.template'))
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
  void test_notify_pr_with_aborted() throws Exception {
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
      log: f.getText(),
      statsUrl: "https://ecs.example.com/app/kibana",
      stepsErrors: readJSON(file: "steps-errors.json"),
      testsErrors: readJSON(file: "tests-errors.json"),
      testsSummary: readJSON(file: "tests-summary.json")
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'Build Unstable'))
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
}
