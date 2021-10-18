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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class RunE2eStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/runE2e.groovy')
    addEnvVar('JENKINS_URL', 'https://beats-ci.elastic.co/')
    addEnvVar('CHANGE_TARGET', 'main')
    addEnvVar('JOB_BASE_NAME', '7.x')
    addEnvVar('GIT_BASE_COMMIT', 'abcdef')
    addEnvVar('REPO', 'my-repo')
  }

  @Test
  void test_without_beatVersion() throws Exception {
    testMissingArgument('beatVersion') {
      script.call()
    }
  }

  @Test
  void test_without_gitHubCheckName() throws Exception {
    testMissingArgument('gitHubCheckName') {
      script.call(beatVersion: 'foo')
    }
  }

  @Test
  void test_prs() throws Exception {
    helper.registerAllowedMethod('isPR', { return true })
    script.call(beatVersion: 'foo', gitHubCheckName: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=e2e-tests/e2e-testing-mbp/main'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=false'))
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=bar'))
    assertJobStatusSuccess()
  }

  @Test
  void test_branches() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(beatVersion: 'foo', gitHubCheckName: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=e2e-tests/e2e-testing-mbp/7.x'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=true'))
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=bar'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_notifyOnGreenBuilds() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(beatVersion: 'foo', gitHubCheckName: 'bar', notifyOnGreenBuilds: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=true'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_jobName() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(beatVersion: 'foo', gitHubCheckName: 'bar', jobName: 'my-job')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=my-job/7.x'))
    assertJobStatusSuccess()
  }

  @Test
  void test_unsupported_ci_controller() throws Exception {
    addEnvVar('JENKINS_URL', 'https://apm-ci.elastic.co/')
    try {
      script.call(beatVersion: 'foo', gitHubCheckName: 'bar')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'runE2e: e2e pipeline is defined in https://beats-ci.elastic.co/'))
  }
}
