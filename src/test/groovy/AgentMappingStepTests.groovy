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
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertNull

class AgentMappingStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/agentMapping.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testEmptyArgumentInEnvVar() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.envVar('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertAny('error', 'envVar: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testEmptyArgumentInYamlVar() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.agentVar('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertAny('error', 'agentVar: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testEmptyArgumentInID() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.id('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertAny('error', 'id: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInApp() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.app(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertAny('error', 'app: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInYamlVersionFile() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.yamlVersionFile(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertAny('error', 'yamlVersionFile: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testDotnetIntEnvVar() throws Exception {
    def script = loadScript(scriptName)
    def value = script.envVar('dotnet')
    printCallStack()
    assertTrue(value.contains('DOTNET'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInAgentVar() throws Exception {
    def script = loadScript(scriptName)
    def value = script.agentVar('dotnet')
    printCallStack()
    assertTrue(value.contains('DOTNET'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInId() throws Exception {
    def script = loadScript(scriptName)
    def value = script.id('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInApp() throws Exception {
    def script = loadScript(scriptName)
    def value = script.app('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInYamlVersionFile() throws Exception {
    def script = loadScript(scriptName)
    def value = script.yamlVersionFile('dotnet')
    printCallStack()
    assertTrue(value.equals('tests/versions/dotnet.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void testUnexistingKeyInYamlVersionFile() throws Exception {
    def script = loadScript(scriptName)
    def value = script.yamlVersionFile('foo')
    printCallStack()
    assertNull(value)
    assertJobStatusSuccess()
  }
}
