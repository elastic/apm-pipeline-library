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

class Junit2OtelStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.JUNIT_2_OTLP = "true"

    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    helper.registerAllowedMethod('isUnix', [], { true })

    script = loadScript('vars/junit2Otel.groovy')
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
  void test_results() throws Exception {
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default junit'))
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2Otel-0.0.0-junit2Otel'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_otel_variables() throws Exception {
    env.OTEL_SERVICE_NAME = "myservice"
    env.JUNIT_OTEL_SERVICE_VERSION = "1.2.3"
    env.JUNIT_OTEL_TRACE_NAME = "mytrace"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default junit'))
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-1.2.3-mytrace'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_with_repo_variables() throws Exception {
    env.REPO = "myrepo"
    env.JUNIT_OTEL_SERVICE_VERSION = "1.2.3"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default junit'))
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myrepo-1.2.3-myrepo'"))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_results_without_feature_flag() throws Exception {
    env.JUNIT_2_OTLP = ""

    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'Override default junit'))
    assertFalse(assertMethodCallContainsPattern('log', "Sending traces for 'junit2Otel-0.0.0-junit2Otel'"))
    assertFalse(assertMethodCallContainsPattern('libraryResource', 'scripts/junit2otel.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_service_name() throws Exception {
    env.OTEL_SERVICE_NAME = "myservice"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'myservice-0.0.0-junit2Otel'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_service_version() throws Exception {
    env.JUNIT_OTEL_SERVICE_VERSION = "1.2.3"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2Otel-1.2.3-junit2Otel'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_trace_name() throws Exception {
    env.JUNIT_OTEL_TRACE_NAME = "mytrace"
    script.call(testResults: 'test-results/TEST-*.xml')

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Sending traces for 'junit2Otel-0.0.0-mytrace'"))
    assertJobStatusSuccess()
  }

}
