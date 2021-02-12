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

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/installTools.groovy')
  }

  @Test
  void test_without_arguments() throws Exception {
    testMissingArgument('missing parameters,', '') {
      script.call()
    }
  }

  @Test
  void test_with_some_missing_arguments() throws Exception {
    testMissingArgument('version') {
      script.installTool([ tool: 'python3' ])
    }
  }

  @Test
  void test_with_unsupported_provider_in_windows() throws Exception {
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
    helper.registerAllowedMethod('isUnix', [], { false })
    script.installTool([ tool: 'foo', version: 'x.y.z' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('powershell', 'Install foo:x.y.z, script=.\\install-with-choco.ps1 foo x.y.z'))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_multiple_tools_in_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call([[ tool: 'foo', version: 'x.y.z', exclude: 'rc' ], [ tool: 'bar', version: 'z.y.x' ]])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('powershell', 'script=.\\install-with-choco.ps1 foo x.y.z rc'))
    assertTrue(assertMethodCallContainsPattern('powershell', 'script=.\\install-with-choco.ps1 bar z.y.x'))
    assertJobStatusSuccess()
  }

  @Test
  void test_install_tool_in_windows_with_all_flags_and_choco() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.installTool([ tool: 'foo', version: 'x.y.z', provider: 'choco', extraArgs: "--foo 'bar' 'foo'" ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('powershell', 'Install foo:x.y.z'))
    assertTrue(assertMethodCallContainsPattern('powershell', """script=choco install foo --no-progress -y --version 'x.y.z' "--foo 'bar' 'foo'" """))
    assertFalse(assertMethodCallContainsPattern('powershell', 'script=.\\install-with-choco.ps1'))
    assertJobStatusSuccess()
  }
}
