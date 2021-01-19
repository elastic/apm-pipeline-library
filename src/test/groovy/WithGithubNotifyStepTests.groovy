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

class WithGithubNotifyStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/withGithubNotify.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.BUILD_ID = '4'
    env.BRANCH_NAME = 'PR-60'
    env.JENKINS_URL = 'http://jenkins.example.com:8080'

    helper.registerAllowedMethod('withAPM', [Closure.class], { body -> body() })
  }

  @Test
  void testMissingArguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGithubNotify: Missing arguments'))
    assertJobStatusFailure()
  }

  @Test
  void testMissingDescriptionArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(description: 'foo'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGithubNotify: Missing arguments'))
    assertJobStatusFailure()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubNotify', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithAllArguments() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'tests', isBlueOcean: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubNotify', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertJobStatusSuccess()
  }

  @Test
  void testFailure() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    try{
      script.call(context: 'failed') {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertJobStatusFailure()
  }

  @Test
  void testSuccessWithIsBlueOcean() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'tests', isBlueOcean: false) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubNotify', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithURL() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'https://www.elastic.co') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'targetUrl=https://www.elastic.co'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithWrongTab() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('getTraditionalPageURL', [String.class], {
      updateBuildStatus('FAILURE')
      throw new Exception('getTraditionalPageURL: Unsupported type')
    })
    def isOK = false
    try {
      script.call(context: 'foo', description: 'bar', tab: 'htt://www.elastic.co') {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertFalse(assertMethodCallContainsPattern('githubNotify', 'targetUrl=htt://www.elastic.co'))
    assertJobStatusFailure()
  }

  @Test
  void test_when_github_is_not_accessible_then_retry_with_some_sleep() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubNotify', [Map.class], { m ->
      if (m.status == 'FAILURE') {
        throw new Exception("Server returned HTTP response code: 500, message: '500 Internal Server Error'")
      }
    })
    try {
      script.call(context: 'foo', description: 'bar') {
        throw new Exception('Force failure')
      }
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', '2 of 2 tries'))
    assertJobStatusFailure()
  }
}
