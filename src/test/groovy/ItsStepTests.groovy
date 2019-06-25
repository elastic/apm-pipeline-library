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

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class ItsStepTests extends BasePipelineTest {
  String scriptName = "vars/its.groovy"
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('env', env)
    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
  }

  @Test
  void testEmptyArgumentInAgentYamlVar() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.agentYamlVar('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('agentYamlVar: Missing key')
    })
    assertJobStatusFailure()
  }

  @Test
  void testEmptyArgumentInMapAgentsIDs() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.mapAgentsIDs('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('mapAgentsIDs: Missing key')
    })
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInMapAgentsApps() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.mapAgentsApps(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('mapAgentsApps: Missing key')
    })
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInYmlFiles() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.ymlFiles(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('ymlFiles: Missing key')
    })
    assertJobStatusFailure()
  }

  @Test
  void testDotnetInAgentYamlVar() throws Exception {
    def script = loadScript(scriptName)
    def value = script.agentYamlVar('dotnet')
    printCallStack()
    assertTrue(value.contains('DOTNET'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInMapAgentsIDs() throws Exception {
    def script = loadScript(scriptName)
    def value = script.mapAgentsIDs('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInMapAgentsApps() throws Exception {
    def script = loadScript(scriptName)
    def value = script.mapAgentsApps('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInYmlFiles() throws Exception {
    def script = loadScript(scriptName)
    def value = script.ymlFiles('dotnet')
    printCallStack()
    assertTrue(value.equals('tests/versions/dotnet.yml'))
    assertJobStatusSuccess()
  }
}
