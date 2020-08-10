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

import co.elastic.BuildException
import co.elastic.NotificationManager
import co.elastic.TimeoutIssuesCause
import co.elastic.mock.StepsMock
import hudson.model.Result
import hudson.tasks.test.AbstractTestResultAction
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class NotifyBuildResultStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/notifyBuildResult.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.NOTIFY_TO = "myName@example.com"
    helper.registerAllowedMethod("getVaultSecret", [Map.class], {
      return [data: [user: "admin", password: "admin123"]]
    })
    helper.registerAllowedMethod("readFile", [Map.class], { return '{"field": "value"}' })

    co.elastic.NotificationManager.metaClass.notifyEmail{ Map m -> 'OK' }
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email'))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results in the PR.'))
    assertTrue(assertMethodCallOccurrences('deleteDir', 1))
  }

  @Test
  void testPullRequest() throws Exception {
    env.CHANGE_ID = "123"
    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testFailureBuild() throws Exception {
    // binding.getVariable('currentBuild').result = 'FAILURE' cannot be used otherwise the stage won't be executed!
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testSuccessBuild() throws Exception {
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallContainsPattern('sendDataToElasticsearch', "secret=${VaultSecret.SECRET_NAME.toString()}"))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutParams() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
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

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()

    // Then senddata to ElasticSearch happens
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    // Then unstable the stage
    assertTrue(assertMethodCallContainsPattern('catchError', 'buildResult=SUCCESS, stageResult=UNSTABLE'))
    // Then cleanup the workspace
    assertTrue(assertMethodCallOccurrences('deleteDir', 1))
    assertJobStatusSuccess()
  }

  @Test
  void testCatchError_with_elasticsearch() throws Exception {
    // When a failure
    helper.registerAllowedMethod('readFile', [Map.class], { throw new Exception('forced') })

    // Then the build is Success
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()

    // Then no further actions are executed afterwards
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 0))
    // Then unstable the stage
    assertTrue(assertMethodCallContainsPattern('catchError', 'buildResult=SUCCESS, stageResult=UNSTABLE'))
    // Then cleanup the workspace
    assertTrue(assertMethodCallOccurrences('deleteDir', 1))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithEmptyOrNull() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.customisedEmail('').equals(''))
    assertTrue(script.customisedEmail(null).equals(''))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithoutJOB_NAME() throws Exception {
    def script = loadScript(scriptName)
    env.REPO = 'foo'
    env.remove('JOB_NAME')
    def result = script.customisedEmail('build-apm@example.com')
    assertTrue(result.equals('build-apm+foo@example.com'))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithJOB_NAME() throws Exception {
    def script = loadScript(scriptName)
    env.REPO = 'foo'
    env.JOB_NAME = 'folder1/folder2/foo'
    assertTrue(script.customisedEmail('build-apm@example.com').equals('build-apm+folder1@example.com'))
    assertJobStatusSuccess()
  }

  @Test
  void testCustomisedEmailWithEmptyEnv() throws Exception {
    def script = loadScript(scriptName)
    env.REPO = ''
    env.JOB_NAME = ''
    assertTrue(script.customisedEmail('build-apm@example.com').equals('build-apm@example.com'))
    assertJobStatusSuccess()
  }

  @Test
  void test_email_without_NOTIFY_TO() throws Exception {
    def script = loadScript(scriptName)
    env.remove('NOTIFY_TO')
    script.call(shouldNotify: true)
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void test_email_with_NOTIFY_TO() throws Exception {
    def script = loadScript(scriptName)
    script.call(shouldNotify: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void test_email_with_to() throws Exception {
    def script = loadScript(scriptName)
    script.call(shouldNotify: true, to: ['foo@acme.com'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testRebuildWhenEnvIssueAlreadySet() throws Exception {
    def script = loadScript(scriptName)
    env.GIT_BUILD_CAUSE = 'pr'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('rebuildPipeline', 0))
    assertJobStatusSuccess()
  }

  @Test
  void testRebuildWhenEnvIssueAlreadySetWithAFailure() throws Exception {
    def script = loadScript(scriptName)
    env.GIT_BUILD_CAUSE = 'pr'
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('rebuildPipeline', 0))
    assertJobStatusSuccess()
  }

  @Test
  void testRebuildWhenEnvIssueUnset() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('rebuildPipeline', 0))
    assertJobStatusSuccess()
  }

  @Test
  void testRebuildWhenEnvIssueUnsetAndFailure() throws Exception {
    def script = loadScript(scriptName)
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('rebuildPipeline', 1))
  }

  @Test
  void testGitCheckoutIssue() throws Exception {
    def script = loadScript(scriptName)
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    def obj = script.isGitCheckoutIssue()
    assertTrue(obj)
  }

  @Test
  void testGitCheckoutIsNotAnIssue() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.isGitCheckoutIssue()
    assertFalse(obj)
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_no_downstreamjobs() throws Exception {
    def script = loadScript(scriptName)
    script.analyseDownstreamJobsFailures([:])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: there are no downstream jobs to be analysed'))
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_a_list_of_values() throws Exception {
    def script = loadScript(scriptName)
    script.analyseDownstreamJobsFailures([ 'foo': 'bar' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "analyseDownstreamJobsFailures just updated the description with 'dummy'."))
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_timeout_in_downstreams() throws Exception {
    def script = loadScript(scriptName)
    def downstreamBuildInfo = new FlowInterruptedException(Result.FAILURE, new TimeoutIssuesCause('foo', 1))
    script.analyseDownstreamJobsFailures(['foo': downstreamBuildInfo])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'foo#1 got a timeout checkout issue'))
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_timeout_in_downstreams_with_build_exception() throws Exception {
    def script = loadScript(scriptName)
    def downstreamBuildInfo = new BuildException("1", Result.FAILURE, new TimeoutIssuesCause('foo', 1))
    script.analyseDownstreamJobsFailures(['foo': downstreamBuildInfo])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'foo#1 got a timeout checkout issue'))
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_unstable_in_downstreams_for_test_failures() throws Exception {
    def script = loadScript(scriptName)
    def downstreamBuildInfo = StepsMock.mockRunWrapperWithUnstable('foo')
    script.analyseDownstreamJobsFailures(['foo': downstreamBuildInfo])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'foo#1 got 1 test failure(s)'))
  }

  @Test
  void test_AnalyseDownstreamJobsFailures_with_unstable_in_downstreams_for_something_else() throws Exception {
    def script = loadScript(scriptName)
    def downstreamBuildInfo = StepsMock.mockRunWrapperWithUnstable('foo', 0)
    script.analyseDownstreamJobsFailures(['foo': downstreamBuildInfo])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "analyseDownstreamJobsFailures just updated the description with 'dummy'."))
  }

  @Test
  void test_notify_pr() throws Exception {
    env.CHANGE_ID = "123"
    def script = loadScript(scriptName)
    script.call(prComment: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results in the PR.'))
  }

  @Test
  void test_notify_pr_in_a_branch() throws Exception {
    env.remove('CHANGE_ID')
    def script = loadScript(scriptName)
    script.call(prComment: true)
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results in the PR.'))
  }

  @Test
  void test_generateBuildReport() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Generate build report.'))
  }

  @Test
  void test_newPRComment_without_entries() throws Exception {
    env.CHANGE_ID = "123"
    def script = loadScript(scriptName)
    script.call(newPRComment: [:])
    printCallStack()
    assertTrue(assertMethodCallOccurrences('unstash', 0))
  }

  @Test
  void test_newPRComment_with_entries() throws Exception {
    env.CHANGE_ID = "123"
    def script = loadScript(scriptName)
    script.call(newPRComment: [ 'foo': 'bar'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('unstash', 'bar'))
  }

  @Test
  void test_newPRComment_with_multiples_entries() throws Exception {
    env.CHANGE_ID = "123"
    def script = loadScript(scriptName)
    script.call(newPRComment: [ 'foo': 'bar', 'bob': 'builder' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('unstash', 'bar'))
    assertTrue(assertMethodCallContainsPattern('unstash', 'builder'))
  }
}
