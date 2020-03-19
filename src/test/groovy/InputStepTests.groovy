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

class InputStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/input.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_default_behavior() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default input'))
    assertFalse(env.INPUT_ABORTED)
  }

  @Test
  void test_when_timeout() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(message: 'failure-system')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default input'))
    assertTrue(assertMethodCallContainsPattern('log', 'input was aborted, timeout reason'))
    assertTrue(env.INPUT_ABORTED)
  }

  @Test
  void test_when_abort_by_user() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(message: 'failure-user')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override default input'))
    assertTrue(assertMethodCallContainsPattern('log', 'input was aborted by user'))
    assertTrue(env.INPUT_ABORTED)
  }
}
