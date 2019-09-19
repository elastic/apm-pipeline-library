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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class OpbeansPipelineStepTests extends BaseDeclarativePipelineTest {
  String scriptName = 'vars/opbeansPipeline.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    binding.setProperty('BASE_DIR', '/')
    binding.setProperty('DOCKERHUB_SECRET', 'secret')
    super.setUp()
  }

  @Test
  void test_when_master_branch() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'stage' }.any { call ->
      callArgsToString(call).contains('Build')
    })
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'stage' }.any { call ->
      callArgsToString(call).contains('Test')
    })
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'stage' }.any { call ->
      callArgsToString(call).contains('Release')
    })
    assertJobStatusSuccess()
  }

  @Test
  void test_when_no_release() throws Exception {
    def script = loadScript(scriptName)
    // When the branch doesn't match
    env.BRANCH_NAME = 'foo'
    script.call()
    printCallStack()
    // Then no publish shell step
    assertFalse(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('make publish')
    })
    assertJobStatusSuccess()
  }

}
