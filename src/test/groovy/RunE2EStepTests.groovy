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

class RunE2EStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/runE2E.groovy')
    addEnvVar('JENKINS_URL', 'https://beats-ci.elastic.co/')
    addEnvVar('CHANGE_TARGET', 'main')
    addEnvVar('JOB_BASE_NAME', '7.x')
    addEnvVar('GIT_BASE_COMMIT', 'abcdef')
    addEnvVar('REPO', 'my-repo')
  }

  @Test
  void test_prs_with_default() throws Exception {
    helper.registerAllowedMethod('isPR', { return true })
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=e2e-tests/e2e-testing-mbp/main'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=false'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'forceSkipGitChecks, value=true'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'forceSkipPresubmit, value=true'))
    assertFalse(assertMethodCallContainsPattern('string', 'testMatrixFile'))
    assertFalse(assertMethodCallContainsPattern('string', 'runTestsSuites'))
    assertTrue(assertMethodCallOccurrences('githubNotify', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_branches_with_default() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=e2e-tests/e2e-testing-mbp/7.x'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=true'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'forceSkipGitChecks, value=true'))
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'forceSkipPresubmit, value=true'))
    assertFalse(assertMethodCallContainsPattern('string', 'testMatrixFile'))
    assertFalse(assertMethodCallContainsPattern('string', 'runTestsSuites'))
    assertTrue(assertMethodCallOccurrences('githubNotify', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_notifyOnGreenBuilds() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(notifyOnGreenBuilds: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('booleanParam', 'notifyOnGreenBuilds, value=true'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_jobName() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(jobName: 'job-foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=e2e-tests/e2e-testing-mbp/job-foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_jobName() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    testMissingArgument('jobName', 'is empty') {
      script.call(jobName: '')
    }
  }

  @Test
  void test_with_gitHubCheckName() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(gitHubCheckName: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=bar'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_disableGitHubCheck() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(gitHubCheckName: 'bar', disableGitHubCheck: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubNotify', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_testMatrixFile() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(testMatrixFile: '.ci/test.yml')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('string', 'testMatrixFile, value=.ci/test.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_runTestsSuites() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(runTestsSuites: 'my-suite')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('string', 'runTestsSuites, value=my-suite'))
    assertJobStatusSuccess()
  }

  @Test
  void test_addStringParameterIfValue() throws Exception {
    assertTrue(script.addStringParameterIfValue('key', 'value', null) == null)
    assertTrue(script.addStringParameterIfValue('key', 'value', []).size() == 1)
  }

  @Test
  void test_createParameters() throws Exception {
    assertTrue(script.createParameters().size() == 4)
    assertTrue(script.createParameters(testMatrixFile: '.ci/test.yml').size() == 5)
    assertTrue(script.createParameters(testMatrixFile: '.ci/test.yml', gitHubCheckName: 'bar').size() == 6)
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
    assertTrue(assertMethodCallContainsPattern('error', 'https://beats-ci.elastic.co/'))
  }

  @Test
  void test_prs_with_default_fleet_ci() throws Exception {
    addEnvVar('JENKINS_URL', 'https://fleet-ci.elastic.co/')
    helper.registerAllowedMethod('isPR', { return true })
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubNotify', 0))
    assertJobStatusSuccess()
  }
}
