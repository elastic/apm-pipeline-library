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
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class Junit2OtelStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    helper.registerAllowedMethod('isUnix', [], { true })

    script = loadScript('vars/junit2otel.groovy')
  }

  @Test
  void test_missing_testResults() throws Exception {
    testMissingArgument('testResults') {
      script.call()
    }
  }

  @Test
  void test_docker_not_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })

    try {
      script.call()
    } catch(e){
      //NOOP
    }

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'docker is not installed but required.'))
    assertJobStatusFailure()
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_calculateFallbackServiceVersion() throws Exception {
    // reset branch name first
    env.BRANCH_NAME = ''

    String version = script.calculateFallbackServiceVersion()
    assertEquals(version, 'unknown')

    env.TAG_NAME = 'v7.8.9'
    version = script.calculateFallbackServiceVersion()
    assertEquals(version, 'v7.8.9')
    env.TAG_NAME = ''

    env.CHANGE_ID = 'PR-23'
    version = script.calculateFallbackServiceVersion()
    assertEquals(version, 'PR-23')
    env.CHANGE_ID = ''

    env.BRANCH_NAME = 'feature/foo'
    version = script.calculateFallbackServiceVersion()
    assertEquals(version, 'feature/foo')
    env.BRANCH_NAME = ''
  }

  @Test
  void test_results() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2otel-master-junit2otel'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_otel_variables() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice', serviceVersion: '1.2.3', traceName: 'mytrace')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-1.2.3-mytrace'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_otel_and_branch_version() throws Exception {
    env.BRANCH_NAME = "feature/foo"
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice', traceName: 'mytrace')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-feature/foo-mytrace'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_otel_and_pr_version() throws Exception {
    env.CHANGE_ID = "PR-123"
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice', traceName: 'mytrace')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-PR-123-mytrace'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_otel_and_tag_version() throws Exception {
    env.TAG_NAME = "v1.2.3"
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice', traceName: 'mytrace')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-v1.2.3-mytrace'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_repo_variables() throws Exception {
    env.REPO = "myrepo"
    script.call(testResults: 'test-results/TEST-*.xml', serviceVersion: '1.2.3')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myrepo-1.2.3-myrepo'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_repo_and_branch_version() throws Exception {
    env.REPO = "myrepo"
    env.BRANCH_NAME = "feature/foo"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myrepo-feature/foo-myrepo'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_repo_and_pr_version() throws Exception {
    env.REPO = "myrepo"
    env.CHANGE_ID = "PR-123"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myrepo-PR-123-myrepo'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_repo_and_tag_version() throws Exception {
    env.REPO = "myrepo"
    env.TAG_NAME = "v1.2.3"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myrepo-v1.2.3-myrepo'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_service_name() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-master-junit2otel'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_service_name_and_repo() throws Exception {
    env.REPO = "myrepo"
    script.call(testResults: 'test-results/TEST-*.xml', serviceName: 'myservice')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-junit-master-myrepo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_service_version() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml', serviceVersion: '1.2.3')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2otel-1.2.3-junit2otel'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_trace_name() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml', traceName: 'mytrace')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2otel-master-mytrace'"))
    assertJobStatusSuccess()
  }

}
