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

class WithGithubCheckStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    addEnvVar('BUILD_ID', '4')
    addEnvVar('BRANCH_NAME', 'PR-60')
    addEnvVar('GIT_BASE_COMMIT', 'abcdef')
    addEnvVar('JENKINS_URL', 'http://jenkins.example.com:8080')
    script = loadScript('vars/withGithubCheck.groovy')
    helper.registerAllowedMethod('withAPM', [Closure.class], { body -> body() })
  }

  @Test
  void test_missing_arguments() throws Exception {
    testMissingArgument('context') {
      script.call() {
        //NOOP
      }
    }
  }

  @Test
  void test_success() throws Exception {
    def isOK = false
    script.call(context: 'foo', description: 'bar') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubCheck', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertFalse(assertMethodCallContainsPattern('githubCheck', "status=failure"))
    assertJobStatusSuccess()
  }

  @Test
  void test_success_with_allArguments() throws Exception {
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'tests', isBlueOcean: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubCheck', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertTrue(assertMethodCallContainsPattern('githubCheck', "status=success"))
    assertJobStatusSuccess()
  }

  @Test
  void test_failure() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('githubCheck', "status=failure"))
    assertJobStatusFailure()
  }

  @Test
  void test_success_with_isBlueOcean() throws Exception {
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'tests', isBlueOcean: false) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubCheck', "${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertFalse(assertMethodCallContainsPattern('githubCheck', "status=failure"))
    assertJobStatusSuccess()
  }

  @Test
  void test_success_withURL() throws Exception {
    def isOK = false
    script.call(context: 'foo', description: 'bar', tab: 'https://www.elastic.co') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('githubCheck', 'detailsUrl=https://www.elastic.co'))
    assertFalse(assertMethodCallContainsPattern('githubCheck', "status=failure"))
    assertJobStatusSuccess()
  }

  @Test
  void test_success_with_wrong_tab() throws Exception {
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
    assertFalse(assertMethodCallContainsPattern('githubCheck', 'detailsUrl=htt://www.elastic.co'))
    assertJobStatusFailure()
  }

  @Test
  void test_isAvailable_without_env() throws Exception {
    def isOK = script.isAvailable()
    printCallStack()
    assertFalse(isOK)
  }

  @Test
  void test_isAvailable_with_arguments() throws Exception {
    def isOK = script.isAvailable(org: 'acme', repository: 'foo', commitId: 'abcde')
    printCallStack()
    assertTrue(isOK)
  }

  @Test
  void test_isAvailable_with_env() throws Exception {
    addEnvVar('ORG_NAME', 'acme')
    addEnvVar('REPO_NAME', 'foo')
    addEnvVar('GIT_BASE_COMMIT', 'abcde')
    def isOK = script.isAvailable(org: 'acme', repository: 'foo', commitId: 'abcde')
    printCallStack()
    assertTrue(isOK)
  }
}
