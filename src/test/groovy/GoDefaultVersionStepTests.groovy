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
import static org.junit.Assert.assertNotEquals

class GoDefaultVersionStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/goDefaultVersion.groovy')
    helper.registerAllowedMethod('fileExists', [String.class], { false })
  }

  @Test
  void test() throws Exception {
    String version = script.call()
    printCallStack()
    assertNotEquals("", version)
    assertJobStatusSuccess()
  }

  @Test
  void testGoVersionEnvVar() throws Exception {
    env.GO_VERSION = "foo"
    String version = script.call()
    printCallStack()
    assertEquals("foo", version)
    assertJobStatusSuccess()
  }

  @Test
  void testGoVersionFromFile() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooFromFile\n' })
    String version = script.call()
    printCallStack()
    assertEquals("fooFromFile", version)
    assertJobStatusSuccess()
  }

  @Test
  void testGoVersionEnvVarPrecedence() throws Exception {
    env.GO_VERSION = "foo"
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooFromFile\n' })
    String version = script.call()
    printCallStack()
    assertEquals("foo", version)
    assertJobStatusSuccess()
  }
}
