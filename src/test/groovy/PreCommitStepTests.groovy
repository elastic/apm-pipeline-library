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

class PreCommitStepTests extends BasePipelineTest {
  String scriptName = 'vars/preCommit.groovy'

  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('env', env)
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod('dockerLogin', [Map.class], { 'OK' })
    helper.registerAllowedMethod('junit', [Map.class], { 'OK' })
    helper.registerAllowedMethod('preCommitToJunit', [Map.class], { 'OK' })
    helper.registerAllowedMethod('sh', [String.class], { 'OK' })
    helper.registerAllowedMethod('sshagent', [List.class, Closure.class], { m, body -> body() })
    helper.registerAllowedMethod('withVaultToken', [Closure.class], { body -> body() })
  }

  @Test
  void testMissingCommitArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(commit: '')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('preCommit: git commit to compare with is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithoutCommitAndEnvVariable() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('preCommit: git commit to compare with is required.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithEnvVariable() throws Exception {
    def script = loadScript(scriptName)
    env.GIT_BASE_COMMIT = 'bar'
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('bar | xargs pre-commit run --files')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWithAllArguments() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', junit: true, credentialsId: 'bar')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sshagent'
    }.any { call ->
      callArgsToString(call).contains('[bar]')
    })
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('foo | xargs pre-commit run --files')
    })
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'preCommitToJunit'
    }.any { call ->
      callArgsToString(call).contains('input=pre-commit.out, output=pre-commit.out.xml')
    })
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'junit'
    }.any { call ->
      callArgsToString(call).contains('testResults=pre-commit.out.xml')
    })
    assertJobStatusSuccess()
  }

  void testWithRegistryAndSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', registry: 'bar', secretRegistry: 'mysecret')
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'dockerLogin'
    }.any { call ->
      callArgsToString(call).contains('{secret=mysecret, registry=bar}')
    })
    assertJobStatusSuccess()
  }

  void testWithEmptyRegistryAndSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', registry: '', secretRegistry: '')
    printCallStack()
    assertJobStatusSuccess()
    assertFalse(helper.callStack.find { call ->
      call.methodName == 'dockerLogin'
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWithDefaultParameters() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sshagent'
    }.any { call ->
      callArgsToString(call).contains('[f6c7695a-671e-4f4f-a331-acdce44ff9ba]')
    })
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'dockerLogin'
    }.any { call ->
      callArgsToString(call).contains('{secret=secret/apm-team/ci/docker-registry/prod, registry=docker.elastic.co}')
    })
    assertJobStatusSuccess()
  }
}
