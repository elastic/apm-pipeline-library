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
import static org.junit.Assert.assertTrue

class NodeJSDefaultVersionStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    addEnvVar('BASE_DIR', './src')
    script = loadScript('vars/nodeJSDefaultVersion.groovy')
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
  void test_NodeVersion_from_file() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { s-> return s.equals('.nvmrc') })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooFromFile\n' })
    String version = script.call()
    printCallStack()
    assertEquals("fooFromFile", version)
    assertTrue(assertMethodCallOccurrences('fileExists', 1))
    assertTrue(assertMethodCallContainsPattern('readFile', '.nvmrc'))
    assertJobStatusSuccess()
  }

  @Test
  void test_nodeVersion_from_file_fallback() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { s-> return s.contains('/.nvmrc') })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooFromFile\n' })
    String version = script.call()
    printCallStack()
    assertEquals("fooFromFile", version)
    assertTrue(assertMethodCallOccurrences('fileExists', 2))
    assertTrue(assertMethodCallContainsPattern('readFile', '/.nvmrc'))
    assertJobStatusSuccess()
  }
}
