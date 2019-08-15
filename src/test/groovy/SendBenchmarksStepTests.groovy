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
import static org.junit.Assert.assertFalse

class SendBenchmarksStepTests extends BasePipelineTest {
  String scriptName = 'vars/sendBenchmarks.groovy'
  Map env = [:]
  def URL = 'https://ec.example.com:9200'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.BRANCH_NAME = "branch"
    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    binding.setVariable('env', env)

    helper.registerAllowedMethod('isUnix', [], { true })
    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod("httpRequest", [Map.class], { m -> println "httpRequest: ${m.toString()}" })
    helper.registerAllowedMethod("readFile", [Map.class], { return "{field1: 1, field2: 2}"})
    helper.registerAllowedMethod("base64encode", [Map.class], { return "dXNlcjpwYXNzd29yZA==" })
    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("getVaultSecret", [Map.class], { v ->
      def s = v.secret
      if("secret".equals(s) || "secret/apm-team/ci/java-agent-benchmark-cloud".equals(s)){
        return [data: [ user: 'user', password: 'password', url: "${URL}"]]
      }
      if("secretError".equals(s)){
        return [errors: 'Error message']
      }
      if("secretNotValid".equals(s)){
        return [data: [ user: null, password: null, url: null]]
      }
      return null
    })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript(scriptName)
    script.call(file: 'bench.out', index: 'index-name', url: 'https://vault.example.com', secret: 'secret', archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testSecretNotFound() throws Exception {
    def script = loadScript(scriptName)
    try{
      def ret = script.call(secret: 'secretNotValid')
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: was not possible to get authentication info to send benchmarks")
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(secret: 'secretError')
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: Unable to get credentials from the vault: Error message")
    })
    assertJobStatusFailure()
  }

  @Test
  void testWrongProtocol() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(secret: 'secret', url: 'ht://wrong.example.com')
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: unknow protocol, the url is not http(s).")
    })
    assertJobStatusFailure()
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
      callArgsToString(call).contains('sendBenchmarks: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingSecretArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.prepareAndRun() {

      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('prepareAndRun: secret argument is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingUrlArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.prepareAndRun(secret: 'secret') {

      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('prepareAndRun: url_var argument is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingUserArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.prepareAndRun(secret: 'secret', url_var: 'URL_') {

      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('prepareAndRun: user_var argument is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingPassArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.prepareAndRun(secret: 'secret', url_var: 'URL_', user_var: 'USER_') {

      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('prepareAndRun: pass_var argument is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testPrepareAndRunWithSecretError() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    try {
      script.prepareAndRun(secret: 'secretError', url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
        isOK = true
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains("prepareAndRun: Unable to get credentials from the vault: Error message")
    })
    assertJobStatusFailure()
  }

  @Test
  void testPrepareAndRunWithSecretNotFound() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    try{
      script.prepareAndRun(secret: 'secretNotValid', url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains('prepareAndRun: was not possible to get authentication info to send benchmarks')
    })
    assertJobStatusFailure()
  }

  @Test
  void testPrepareAndRun() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.prepareAndRun(secret: 'secret', url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
      isOK = true
    }
    assertTrue(isOK)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'withEnv'
    }.any { call ->
        callArgsToString(call).contains("URL_=${URL}, USER_=user, PASS_=password")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testPrepareAndRunInWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.prepareAndRun() {

      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('prepareAndRun: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }
}
