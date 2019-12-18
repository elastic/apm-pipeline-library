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
import co.elastic.NotificationManager

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
    try {
      script.call(es: EXAMPLE_URL, secret: "secret")

    } catch(e){
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email'))
  }

  @Test
  void testPullRequest() throws Exception {
    env.CHANGE_ID = "123"

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: "secret")
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testSuccessBuild() throws Exception {
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: "secret")
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutParams() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testWithoutSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('getBuildInfoJsonFiles', 1))
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('log', 'notifyBuildResult: Notifying results by email.'))
  }

  @Test
  void testCatchError() throws Exception {
    // When a failure
    helper.registerAllowedMethod("getBuildInfoJsonFiles", [String.class,String.class], { throw new Exception(s) })

    // Then the build is Success
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript(scriptName)
    script.call(es: EXAMPLE_URL, secret: "secret")
    printCallStack()

    // Then no further actions are executed afterwards
    assertTrue(assertMethodCallOccurrences('sendDataToElasticsearch', 0))
    // Then unstable the stage
    assertTrue(assertMethodCallContainsPattern('catchError', 'buildResult=SUCCESS, stageResult=UNSTABLE'))
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
}
