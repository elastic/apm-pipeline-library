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

class RubygemsLoginStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/rubygemsLogin.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call() {
        // NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
      callArgsToString(call).contains('rubygemsLogin: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWindowsWithApi() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.withApi() {
        // NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
      callArgsToString(call).contains('rubygemsLogin.withApi: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingSecret() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call() {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin: No valid secret to looking for.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingSecretWithApi() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.withApi() {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin.withApi: No valid secret to looking for.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretNotFound() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call(secret: 'secretNotValid') {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin: was not possible to get authentication details.')
    })
    assertJobStatusFailure()
  }


  @Test
  void testSecretNotFoundWithApi() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.withApi(secret: 'secretNotValid') {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin.withApi: was not possible to get authentication details.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call(secret: 'secretError') {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin: Unable to get credentials from the vault')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSecretErrorWithApi() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.withApi(secret: 'secretError') {
        // NOOP
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'error' }.any { call ->
        callArgsToString(call).contains('rubygemsLogin.withApi: Unable to get credentials from the vault')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(secret: 'secret/team/ci/secret-name') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('curl -u "\${RUBY_USER}:\${RUBY_PASS}" https://rubygems.org/api/v1/api_key.yaml')
    })
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('rm ~/.gem/credentials')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithApi() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.withApi(secret: 'secret/team/ci/secret-name') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('echo ":rubygems_api_key: \${RUBY_API_KEY}" >> ~/.gem/credentials')
    })
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('rm ~/.gem/credentials')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWithBodyError() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(secret: 'secret/team/ci/secret-name') {
        updateBuildStatus('FAILURE')
        throw new Exception('Force error')
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('rm ~/.gem/credentials')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithBodyErrorWithApi() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.withApi(secret: 'secret/team/ci/secret-name') {
        updateBuildStatus('FAILURE')
        throw new Exception('Force error')
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call -> call.methodName == 'sh' }.any { call ->
      callArgsToString(call).contains('rm ~/.gem/credentials')
    })
    assertJobStatusFailure()
  }
}
