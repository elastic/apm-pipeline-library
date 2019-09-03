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

import co.elastic.TestUtils
import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class WithSecretVaultStepTests extends BasePipelineTest {
  String scriptName = "vars/withSecretVault.groovy"
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.BRANCH_NAME = "branch"
    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("getVaultSecret", [Map.class], { m ->
      def s = m.secret
      if("secret".equals(s)){
        return [data: [ user: 'username', password: 'user_password']]
      }
      if("secretError".equals(s)){
        return [errors: 'Error message']
      }
      if("secretNotValid".equals(s)){
        return [data: [ user: null, password: null]]
      }
      return null
    })
  }

  @Test
  void testMissingArguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(secret: 'secret', user_var_name: 'foo'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('withSecretVault: Missing variables')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(secret: 'secretError', user_var_name: 'foo', pass_var_name: 'bar'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('withSecretVault: Unable to get credentials from the vault: Error message')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretNotFound() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call(secret: 'secretNotExists', user_var_name: 'foo', pass_var_name: 'bar'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("withSecretVault: was not possible to get authentication info")
    })
    assertJobStatusFailure()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(secret: 'secret', user_var_name: 'foo', pass_var_name: 'bar'){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(secret: 'secret', user_var_name: 'U1', pass_var_name: 'P1'){
      if(binding.getVariable("U1") == "username"
        && binding.getVariable("P1") == "user_password"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }
}
