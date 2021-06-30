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

class WithNodeStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withNode.groovy')
  }

  @Test
  void test_labels() throws Exception {
    testMissingArgument('labels') {
      script.call() {
        //
      }
    }
  }

  @Test
  void test_with_defaults() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return false })
    def isOK = false
    script.call(labels: 'foo') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPatternOccurrences('sleep', 0))
    assertFalse(assertMethodCallContainsPattern('node', 'foo && extra/'))
    assertTrue(assertMethodCallOccurrences('ws', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_defaults_static_worker() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return true })
    def isOK = false
    script.call(labels: 'arm') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('node', 'arm'))
    assertFalse(assertMethodCallContainsPattern('node', 'extra/'))
    assertTrue(assertMethodCallOccurrences('ws', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_forced_worker() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return false })
    def isOK = false
    script.call(labels: 'foo', forceWorker: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('node', 'foo && extra/'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_forced_static_worker() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return true })
    def isOK = false
    script.call(labels: 'arm', forceWorker: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('node', 'arm'))
    assertFalse(assertMethodCallContainsPattern('node', 'extra/'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_forced_workspace() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [ Map.class ], { return false })
    def isOK = false
    script.call(labels: 'foo', forceWorkspace: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('node', 'foo'))
    assertTrue(assertMethodCallOccurrences('ws', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_sleep_min() throws Exception {
    def isOK = false
    script.call(labels: 'foo', sleepMin: 1) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('randomNumber', '1'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_sleep_max() throws Exception {
    def isOK = false
    script.call(labels: 'foo', sleepMax: 100) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('randomNumber', '100'))
    assertJobStatusSuccess()
  }

  @Test
  void test_getWorkspace_with_variables() throws Exception {
    addEnvVar('JOB_BASE_NAME', 'foo')
    addEnvVar('BUILD_NUMBER', '1')
    def workspace = script.getWorkspace('abcde')
    printCallStack()
    assertTrue(workspace == 'workspace/foo-1-abcde')
    assertJobStatusSuccess()
  }

  @Test
  void test_getWorkspace_without_variables() throws Exception {
    env.remove('JOB_BASE_NAME')
    env.remove('BUILD_NUMBER')
    def workspace = script.getWorkspace('abcde')
    printCallStack()
    println workspace
    assertTrue(workspace == 'workspace/unknown-unknown-abcde')
    assertJobStatusSuccess()
  }

  @Test
  void test_with_disable_workers() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return false })
    def isOK = true
    script.call(labels: 'windows-7-32-bits', disableWorkers: true) {
      isOK = false
    }
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('node', 'windows-7-32-bits'))
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_disable_workers_and_another_worker() throws Exception {
    helper.registerAllowedMethod('isStaticWorker', [Map.class], { return false })
    def isOK = true
    script.call(labels: 'windows-2019', disableWorkers: true) {
      isOK = false
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('node', 'windows-2019'))
    assertFalse(isOK)
    assertJobStatusSuccess()
  }
}
