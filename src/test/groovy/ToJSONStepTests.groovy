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
import net.sf.json.JSONObject
import static org.junit.Assert.assertTrue

class ToJSONStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/toJSON.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.call("{'dummy': 'value'}")
    printCallStack()
    assertTrue(obj instanceof JSONObject)
    assertJobStatusSuccess()
  }

  @Test
  void testNoJSON() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.call("")
    printCallStack()
    assertTrue(obj == null)
    assertJobStatusSuccess()
  }

  @Test
  void testPOJO() throws Exception {
    def script = loadScript(scriptName)
    def pojo = [p1: 'value', p2: 'value']
    def obj = script.call(pojo)
    printCallStack()
    assertTrue(obj instanceof JSONObject)
    assertJobStatusSuccess()
  }
}
