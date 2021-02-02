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
import static org.junit.Assert.assertNull

class AgentMappingStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/agentMapping.groovy')
  }

  @Test
  void testEmptyArgumentInEnvVar() throws Exception {
    try {
      script.envVar('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'envVar: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testEmptyArgumentInYamlVar() throws Exception {
    try {
      script.agentVar('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'agentVar: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testEmptyArgumentInID() throws Exception {
    try {
      script.id('')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'id: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInApp() throws Exception {
    try {
      script.app(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'app: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInOpbeansApp() throws Exception {
    try {
      script.opbeansApp(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'opbeansApp: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testNullArgumentInYamlVersionFile() throws Exception {
    try {
      script.yamlVersionFile(null)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'yamlVersionFile: Missing key'))
    assertJobStatusFailure()
  }

  @Test
  void testDotnetIntEnvVar() throws Exception {
    def value = script.envVar('dotnet')
    printCallStack()
    assertTrue(value.contains('DOTNET'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInAgentVar() throws Exception {
    def value = script.agentVar('dotnet')
    printCallStack()
    assertTrue(value.contains('DOTNET'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInId() throws Exception {
    def value = script.id('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInApp() throws Exception {
    def value = script.app('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInOpbeansApp() throws Exception {
    def value = script.opbeansApp('.NET')
    printCallStack()
    assertTrue(value.equals('dotnet'))
    assertJobStatusSuccess()
  }

  @Test
  void testDotnetInYamlVersionFile() throws Exception {
    def value = script.yamlVersionFile('dotnet')
    printCallStack()
    assertTrue(value.equals('tests/versions/dotnet.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void testUnexistingKeyInYamlVersionFile() throws Exception {
    def value = script.yamlVersionFile('foo')
    printCallStack()
    assertNull(value)
    assertJobStatusSuccess()
  }
}
