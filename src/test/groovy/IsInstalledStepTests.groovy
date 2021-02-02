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

class IsInstalledStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/isInstalled.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
  }

  @Test
  void test_without_tool_parameter() throws Exception {
    def script = loadScript(scriptName)
    def ret = false
    testMissingArgument('tool') {
      ret = script.call()
    }
    assertFalse(ret)
  }

  @Test
  void test_tool() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(tool: 'docker')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker'))
    assertTrue(assertMethodCallContainsPattern('cmd', '>/dev/null'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_tool_with_flag() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(tool: 'docker', flag: '--version')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker --version >/dev/null'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_tool_with_flag_in_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    def ret = script.call(tool: 'docker', flag: '--version')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker --version >NUL'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }
}
