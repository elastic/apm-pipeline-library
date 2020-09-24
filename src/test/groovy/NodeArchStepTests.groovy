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

class NodeArchStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/nodeArch.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_i386() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "i386 foo bar"
    def value = script.call()
    printCallStack()
    assertTrue(value == "i386")
    assertJobStatusSuccess()
  }

  @Test
  void test_x86_64() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar x86_64"
    def value = script.call()
    printCallStack()
    assertTrue(value == "x86_64")
    assertJobStatusSuccess()
  }

  @Test
  void test_arm() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar arm"
    def value = script.call()
    printCallStack()
    assertTrue(value == "arm")
    assertJobStatusSuccess()
  }

  @Test
  void test_arm_with_aarch64() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar arm aarch64"
    def value = script.call()
    printCallStack()
    assertTrue(value == "aarch64")
    assertJobStatusSuccess()
  }

  @Test
  void test_notFound() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "foo bar"
    try {
      def value = script.call()
    } catch(e){
      assertTrue(e.getMessage() == 'Unhandled arch in NODE_LABELS: foo bar')
    }
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void test_label_conflict() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "i386 x86_64"
    try {
      def value = script.call()
    } catch(e){
      assertTrue(e.getMessage() == 'Labels conflict arch in NODE_LABELS: i386 x86_64')
    }
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void test_x86_64_with_swarm_label() throws Exception {
    def script = loadScript(scriptName)
    env.NODE_LABELS = "swarm x86_64"
    def value = script.call()
    printCallStack()
    assertTrue(value == "x86_64")
    assertJobStatusSuccess()
  }
}
