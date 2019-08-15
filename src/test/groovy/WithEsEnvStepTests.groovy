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

class WithEsEnvStepTests extends BasePipelineTest {
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

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("getVaultSecret", [String.class], { s ->
      if("secret".equals(s) || "java-agent-benchmark-cloud".equals(s)){
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
  void test() throws Exception {
    def script = loadScript("vars/withEsEnv.groovy")
    def isOK = false
    script.call(secret: 'secret'){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/withEsEnv.groovy")
    def isOK = false
    script.call(url: 'https://es.example.com', secret: 'secret'){
      if(binding.getVariable("CLOUD_URL") == "https://username:user_password@es.example.com"
      && binding.getVariable("CLOUD_ADDR") == "https://es.example.com"
      && binding.getVariable("CLOUD_USERNAME") == "username"
      && binding.getVariable("CLOUD_PASSWORD") == "user_password"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testSecretNotFound() throws Exception {
    def script = loadScript("vars/withEsEnv.groovy")
    try{
      script.call(secret: 'secretNotExists'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("withEsEnv: was not possible to get authentication info")
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    def script = loadScript("vars/withEsEnv.groovy")
    try {
      script.call(secret: 'secretError'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("withEsEnv: Unable to get credentials from the vault: Error message")
    })
    assertJobStatusFailure()
  }

  @Test
  void testWrongProtocol() throws Exception {
    def script = loadScript("vars/withEsEnv.groovy")
    try {
      script.call(secret: 'secret', url: 'ht://wrong.example.com'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("withEsEnv: unknow protocol, the url is not http(s).")
    })
    assertJobStatusFailure()
  }
}
