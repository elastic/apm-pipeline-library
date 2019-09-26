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

class GitCmdStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/gitCmd.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable("ORG_NAME", "my_org")
    binding.setVariable("REPO_NAME", "my_repo")
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(cmd: 'push')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript(scriptName)
    script.call(cmd: "push", credentialsId: "my_credentials", args: '-f')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testNoCmd() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call(credentialsId: "my_credentials", args: '-f')
    } catch(err){
      //NOOP
      println err.toString()
      err.printStackTrace(System.out);
    }
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("gitCmd: missing git command")
    })
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void testParamsWithEmptyCredentials() throws Exception {
    def script = loadScript(scriptName)
    script.call(cmd: "push", credentialsId: '', args: '-f')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'usernamePassword'
    }.any { call ->
        callArgsToString(call).contains('2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testParamsWithAnotherCredentials() throws Exception {
    def script = loadScript(scriptName)
    script.call(cmd: "push", credentialsId: 'foo', args: '-f')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'usernamePassword'
    }.any { call ->
        callArgsToString(call).contains('foo')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testCmdIsPopulated() throws Exception {
    def script = loadScript(scriptName)
    script.call(cmd: 'push', credentialsId: 'foo')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'sh'
    }.any { call ->
        callArgsToString(call).contains('script=git push')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('gitCmd: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }
}
