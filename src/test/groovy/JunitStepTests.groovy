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

class JunitStepTests extends ApmBasePipelineTest {


  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/junit.groovy')
  }

  @Test
  void test_no_testResults() throws Exception {
    try {
      script.call()
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'junit: testResults parameter is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_callback_parameter() throws Exception {
    def initialCount = 0

    try {
      def callback = { initialCount++ }

      script.call(testResults: 'build/TEST-*.xml', callback: callback)
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=build/TEST-*.xml'))
    assertTrue(assertMethodCallContainsPattern('log', 'junit: Running callback operations after junit'))
    assertEquals(initialCount, 1)
    assertJobStatusSuccess()
  }

  @Test
  void test_without_callback_parameter() throws Exception {
    def initialCount = 0
    try {
      script.call(testResults: 'build/TEST-*.xml')
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=build/TEST-*.xml'))
    assertFalse(assertMethodCallContainsPattern('log', 'junit: Running callback operations after junit'))
    assertEquals(initialCount, 0)
    assertJobStatusSuccess()
  }
}
