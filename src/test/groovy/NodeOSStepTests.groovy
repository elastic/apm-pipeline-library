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

class NodeOSStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/nodeOS.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "linux foo bar"
    def value = script.call()
    printCallStack()
    assertTrue(value == "linux")
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar windows"
    def value = script.call()
    printCallStack()
    assertTrue(value == "windows")
    assertJobStatusSuccess()
  }

  @Test
  void testDarwin() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo darwin bar"
    def value = script.call()
    printCallStack()
    assertTrue(value == "darwin")
    assertJobStatusSuccess()
  }

  @Test
  void testNotFound() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar"
    try {
      def value = script.call()
    } catch(e){
      assertTrue(e.getMessage() == "Unhandled OS name in NODE_LABELS: foo bar")
    }
    printCallStack()
    assertJobStatusFailure()
  }


  @Test
  void testLabelConflict() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "linux windows"
    try {
      def value = script.call()
    } catch(e){
      assertTrue(e.getMessage() == "Labels conflit OS name in NODE_LABELS: linux windows")
    }
    printCallStack()
    assertJobStatusFailure()
  }
}
