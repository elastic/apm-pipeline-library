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

class InstallToolsStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/installTools.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_without_arguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'installTools: missing params'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_some_missing_arguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.installTool([ tool: 'python3' ])
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'installTools: missing version param'))
    assertJobStatusFailure()
  }

  @Test
  void test_install_tool() throws Exception {
    def script = loadScript(scriptName)
    script.installTool([ tool: 'foo', version: 'x.y.z' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('echo', "Tool='foo' Version='x.y.z'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_multiple_tools() throws Exception {
    def script = loadScript(scriptName)
    script.call([[ tool: 'foo', version: 'x.y.z' ], [ tool: 'bar', version: 'z.y.x' ]])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('echo', "Tool='foo' Version='x.y.z'"))
    assertTrue(assertMethodCallContainsPattern('echo', "Tool='bar' Version='z.y.x'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_tool_in_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.installTool([ tool: 'foo', version: 'x.y.z' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('echo', "Tool='foo' Version='x.y.z'"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'VERSION=x.y.z, TOOL=foo'))
    assertTrue(assertMethodCallContainsPattern('powershell', 'Install foo:x.y.z, script=.\\install-with-choco.ps1'))
    assertJobStatusSuccess()
  }
}
