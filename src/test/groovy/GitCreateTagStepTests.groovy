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

class GitCreateTagStepTests extends BasePipelineTest {
  String scriptName = 'vars/gitCreateTag.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('BUILD_TAG', 'foo')

    helper.registerAllowedMethod('sh', [String.class], { "OK" })
    helper.registerAllowedMethod('sh', [Map.class], { "OK" })
    helper.registerAllowedMethod("gitCmd", [Map.class], { return "OK" })
    helper.registerAllowedMethod("gitPush", [Map.class], { return "OK" })
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'sh'
    }.any { call ->
        callArgsToString(call).contains("git tag -a -m 'chore: Create tag foo' 'foo'")
    })
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'gitPush'
    }.any { call ->
        callArgsToString(call).contains('credentialsId=, args=--tags')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript(scriptName)
    script.call(tag: "my_tag", credentialsId: "my_credentials", force: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'gitPush'
    }.any { call ->
        callArgsToString(call).contains('credentialsId=my_credentials, args=--tags -f')
    })
    assertJobStatusSuccess()
  }
}
