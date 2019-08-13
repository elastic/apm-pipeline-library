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

class DockerLoginStepTests extends BasePipelineTest {
  String scriptName = 'vars/dockerLogin.groovy'
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod('isUnix', [], { true })
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("sh", [Map.class], { m -> println m.script })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod("getVaultSecret", [Map.class], {
      return [data: [user: "my-user", password: "my-password"]]
      })
    helper.registerAllowedMethod("retry", [Integer.class, Closure.class], { i, c ->
      c.call()
    })
    helper.registerAllowedMethod("randomNumber", [Map.class], { m -> return m.min })
    helper.registerAllowedMethod("sleep", [Integer.class], { 'OK' })
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(secret: 'secret/team/ci/secret-name')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sh"
    }.any { call ->
        callArgsToString(call).contains('docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "docker.io"')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testRegistry() throws Exception {
    def script = loadScript(scriptName)
    script.call(secret: 'secret/team/ci/secret-name', registry: "other.docker.io")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sh"
    }.any { call ->
        callArgsToString(call).contains('docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "other.docker.io"')
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
      callArgsToString(call).contains('dockerLogin: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }
}
