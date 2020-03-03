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
  void test_with_unsupported_provider_in_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.installTool([ tool: 'python3', provider: 'foo', version: '1' ])
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'installTools: unsupported provider'))
    assertJobStatusFailure()
  }

  @Test
  void test_install_tool_in_linux() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.installTool([ tool: 'foo', version: 'x.y.z' ])
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'TBD: install in linux'))
    assertJobStatusFailure()
  }

  @Test
  void test_install_tool_in_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.installTool([ tool: 'foo', version: 'x.y.z' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', "VERSION=x.y.z, TOOL=foo, EXTRA_ARGS=''"))
    assertTrue(assertMethodCallContainsPattern('powershell', 'Install foo:x.y.z, script=.\\install-with-choco.ps1'))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_multiple_tools_in_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call([[ tool: 'foo', version: 'x.y.z' ], [ tool: 'bar', version: 'z.y.x' ],
                 [ tool: 'some', version: 'z.y.x', extraArgs: "--foo 'bar' 'foo'" ]])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', "VERSION=x.y.z, TOOL=foo, EXTRA_ARGS=''"))
    assertTrue(assertMethodCallContainsPattern('withEnv', "VERSION=z.y.x, TOOL=bar, EXTRA_ARGS=''"))
    assertTrue(assertMethodCallContainsPattern('withEnv', "VERSION=z.y.x, TOOL=some, EXTRA_ARGS=--foo 'bar' 'foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_tool_in_windows_with_all_flags() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.installTool([ tool: 'foo', version: 'x.y.z', provider: 'choco', extraArgs: "--foo 'bar' 'foo'" ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('powershell', 'Install foo:x.y.z'))
    assertTrue(assertMethodCallContainsPattern('powershell', """script=choco install foo --no-progress -y --version 'x.y.z' "--foo 'bar' 'foo'" """))
    assertFalse(assertMethodCallContainsPattern('powershell', 'script=.\\install-with-choco.ps1'))
    assertJobStatusSuccess()
  }
}
