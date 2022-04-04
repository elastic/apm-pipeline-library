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

import co.elastic.NotificationManager
import co.elastic.mock.RunWrapperMock
import co.elastic.mock.StepsMock
import hudson.model.Result
import hudson.tasks.test.AbstractTestResultAction
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class NotifyBuildResultStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/notifyBuildResult.groovy')
    binding.setVariable('nextBuild', null)
    env.NOTIFY_TO = "myName@example.com"
    helper.registerAllowedMethod("getVaultSecret", [Map.class], {
      return [data: [user: "admin", password: "admin123"]]
    })
    helper.registerAllowedMethod('fileExists', [String.class], { return true })
    helper.registerAllowedMethod("readFile", [Map.class], { return '{"field": "value"}' })

    co.elastic.NotificationManager.metaClass.notifyEmail{ Map m -> 'OK' }
  }

  @Test
  void test() throws Exception {
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email'))
    assertTrue(assertMethodCallContainsPattern('log', 'createGitHubComment: Create GitHub comment.'))
  }

  @Test
  void testPullRequest() throws Exception {
    env.CHANGE_ID = "123"
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testFailureBuild() throws Exception {
    // binding.getVariable('currentBuild').result = 'FAILURE' cannot be used otherwise the stage won't be executed!
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testSuccessBuild() throws Exception {
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
    assertTrue(assertMethodCallContainsPattern('sendDataToElasticsearch', "secret=${VaultSecret.SECRET_NAME.toString()}"))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutParams() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutSecret() throws Exception {
    script.call(es: EXAMPLE_URL)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
    assertTrue(assertMethodCallContainsPattern('sendDataToElasticsearch', 'secret=secret/observability-team/ci/jenkins-stats-cloud'))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testCatchError_with_notifications() throws Exception {
    // When a failure
    helper.registerAllowedMethod("getBuildInfoJsonFiles", [String.class,String.class], { throw new Exception(s) })

    // Then the build is Success
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()

    // Then senddata to ElasticSearch happens
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
    // Then unstable the stage
    assertTrue(assertMethodCallContainsPattern('catchError', 'buildResult=SUCCESS, stageResult=UNSTABLE'))
    assertJobStatusSuccess()
  }

  @Test
  void testCatchError_with_elasticsearch() throws Exception {
    // When a failure
    helper.registerAllowedMethod('readFile', [Map.class], { throw new Exception('forced') })

    // Then the build is Success
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()

    // Then no further actions are executed afterwards
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 0))
    // Then unstable the stage
    assertTrue(assertMethodCallContainsPattern('catchError', 'buildResult=SUCCESS, stageResult=UNSTABLE'))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithEmptyOrNull() throws Exception {
    assertTrue(script.customisedEmail('').equals([]))
    assertTrue(script.customisedEmail(null).equals([]))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithoutJOB_NAME() throws Exception {
    env.REPO = 'foo'
    env.remove('JOB_NAME')
    def result = script.customisedEmail('build-apm@example.com')
    assertTrue(result.equals(['build-apm+foo@example.com']))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithJOB_NAME() throws Exception {
    env.REPO = 'foo'
    env.JOB_NAME = 'folder1/folder2/foo'
    assertTrue(script.customisedEmail('build-apm@example.com').equals(['build-apm+folder1@example.com']))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithEmptyEnv() throws Exception {
    env.REPO = ''
    env.JOB_NAME = ''
    assertTrue(script.customisedEmail('build-apm@example.com').equals(['build-apm@example.com']))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithFilter() throws Exception {
    env.REPO = 'foo'
    env.JOB_NAME = 'folder1/folder2/foo'
    assertTrue(script.customisedEmail('build-apm+foo@example.com').equals(['build-apm+foo@example.com']))
    assertJobStatusSuccess()
  }

  @Test
  void test_email_without_NOTIFY_TO() throws Exception {
    env.remove('NOTIFY_TO')
    script.call(shouldNotify: true)
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void test_email_with_NOTIFY_TO() throws Exception {
    script.call(shouldNotify: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void test_email_with_to() throws Exception {
    script.call(shouldNotify: true, to: ['foo@acme.com'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void test_notify_pr() throws Exception {
    env.CHANGE_ID = "123"
    script.call(prComment: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'createGitHubComment: Create GitHub comment.'))
  }

  @Test
  void test_notify_pr_in_a_branch() throws Exception {
    env.remove('CHANGE_ID')
    script.call(prComment: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'createGitHubComment: Create GitHub comment.'))
  }

  @Test
  void test_generateBuildReport() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generate build report.'))
  }

  @Test
  void test_newPRComment_without_entries() throws Exception {
    env.CHANGE_ID = "123"
    script.call(newPRComment: [:])
    printCallStack()
    assertTrue(assertMethodCallOccurrences('unstash', 0))
  }

  @Test
  void test_newPRComment_with_entries() throws Exception {
    env.CHANGE_ID = "123"
    script.call(newPRComment: [ 'foo': 'bar'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('unstash', 'bar'))
  }

  @Test
  void test_newPRComment_with_multiples_entries() throws Exception {
    env.CHANGE_ID = "123"
    script.call(newPRComment: [ 'foo': 'bar', 'bob': 'builder' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('unstash', 'bar'))
    assertTrue(assertMethodCallContainsPattern('unstash', 'builder'))
  }

  @Test
  void test_no_flakey_when_aborted() throws Exception {
    // When aborted
    binding.getVariable('currentBuild').result = "ABORTED"
    binding.getVariable('currentBuild').currentResult = "ABORTED"

    script.call(analyzeFlakey: true)
    printCallStack()

    // Then no flakey test analysis
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generating flakey test analysis'))
  }

  @Test
  void test_flakey_when_unstable() throws Exception {
    // When unstable
    binding.getVariable('currentBuild').result = "UNSTABLE"
    binding.getVariable('currentBuild').currentResult = "UNSTABLE"

    script.call(analyzeFlakey: true)
    printCallStack()

    // Then flakey test analysis
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generating flakey test analysis'))
  }

  @Test
  @Ignore("Class does not have access to the built-in steps. Error: No signature of method: co.elastic.NotificationManager.catchError()")
  void test_flakey_and_prcomment_with_aggregation() throws Exception {
    // When PR
    helper.registerAllowedMethod('isPR', { return true })

    script.call(aggregateComments: true, analyzeFlakey: true, flakyReportIdx: 'foo', notifyPRComment: true)
    printCallStack()

    // Then flakey test analysis
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generating flakey test analysis'))
    // with pr comment
    assertTrue(assertMethodCallContainsPattern('log', 'createGitHubComment: Create GitHub comment.'))
    // with github pr comment
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_flakey_and_prcomment_without_aggregation() throws Exception {
    // When PR
    helper.registerAllowedMethod('isPR', { return true })

    script.call(aggregateComments: false, analyzeFlakey: true, flakyReportIdx: 'foo', notifyPRComment: true)
    printCallStack()

    // Then flakey test analysis
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generating flakey test analysis'))
    // with pr comment
    assertTrue(assertMethodCallContainsPattern('log', 'createGitHubComment: Create GitHub comment.'))
    // with github pr comment
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_no_flakey_and_no_prcomment_with_aggregation() throws Exception {
    // When PR
    helper.registerAllowedMethod('isPR', { return true })

    script.call(aggregateComments: true, analyzeFlakey: false, notifyPRComment: false)
    printCallStack()

    // Then no github pr comment
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_bulk_update() throws Exception {
    // When PR and there is a builk file
    helper.registerAllowedMethod('isPR', { return true })

    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()

    // Then sendDataToElasticsearch happens three times
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 2))
  }

  @Test
  void test_aggregateGitHubComments_with_latest_build() throws Exception {
    script.aggregateGitHubComments(when: true, notifications: ['foo'])
    printCallStack()

    // Then github pr comment
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_aggregateGitHubComments_with_previous_build_and_new_build_running() throws Exception {
    // When there is already a new build still running
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'RUNNING'))
    script.aggregateGitHubComments(when: true, notifications: ['foo'])
    printCallStack()

    // Then github pr comment should happen
    assertTrue(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_aggregateGitHubComments_with_previous_build_and_new_build_already_finished() throws Exception {
    // When there is already a new build that finished.
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'SUCCESS'))
    script.aggregateGitHubComments(when: true, notifications: ['foo'])
    printCallStack()

    // Then github pr comment should not happen
    assertFalse(assertMethodCallContainsPattern('githubPrComment', 'commentFile=comment.id'))
  }

  @Test
  void test_aggregateGitHubCheck_with_latest_build() throws Exception {
    binding.getVariable('currentBuild').currentResult = 'ABORTED'
    script.aggregateGitHubCheck(when: true, notifications: ['foo'])
    printCallStack()

    // Then githubCheck should happen
    assertTrue(assertMethodCallContainsPattern('githubCheck', 'status=cancelled'))
  }

  @Test
  void test_aggregateGitHubCheck_with_previous_build_and_new_build_running() throws Exception {
    // When there is already a new build still running
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'RUNNING'))
    script.aggregateGitHubCheck(when: true, notifications: ['foo'])
    printCallStack()

    // Then githubCheck should happen
    assertTrue(assertMethodCallContainsPattern('githubCheck', 'status'))
  }

  @Test
  void test_aggregateGitHubCheck_with_previous_build_and_new_build_already_finished() throws Exception {
    // When there is already a new build that finished.
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'SUCCESS'))
    script.aggregateGitHubCheck(when: true, notifications: ['foo'])
    printCallStack()

    // Then githubCheck should not happen
    assertFalse(assertMethodCallContainsPattern('githubCheck', 'status'))
  }

  @Test
  void test_notifyIfNewBuildNotRunning_with_previous_build_and_new_build_running() throws Exception {
    // When there is already a new build still running
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'RUNNING'))
    def ret = false
    script.notifyIfNewBuildNotRunning() {
      println 'It should run the closure'
      ret = true
    }
    printCallStack()
    // Then it should run the closure correctly
    assertTrue(ret)
  }

  @Test
  void test_notifyIfNewBuildNotRunning_with_previous_build_and_new_build_already_finished() throws Exception {
    // When there is already a new build that finished.
    binding.setVariable('nextBuild', new RunWrapperMock(rawBuild: null, number: 1, result: 'SUCCESS'))
    def ret = false
    script.notifyIfNewBuildNotRunning() {
      println 'It should not run the closure'
      ret = true
    }
    printCallStack()

    // Then it should not run the closure
    assertFalse(ret)
  }

  @Test
  void test_notifyIfNewBuildNotRunning_with_the_very_first_build() throws Exception {
    // When there is already a new build that finished.
    binding.setVariable('nextBuild', null)
    def ret = false
    script.notifyIfNewBuildNotRunning() {
      println 'It should run the closure'
      ret = true
    }
    printCallStack()

    // Then it should not run the closure
    assertTrue(ret)
  }
}
