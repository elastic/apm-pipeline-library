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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class IsGitRegionMatchStepTests extends BasePipelineTest {
  String scriptName = 'vars/isGitRegionMatch.groovy'
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('env', env)
    helper.registerAllowedMethod('echo', [String.class], { 'OK' })
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
  }

  @Test
  void testWithoutRegexps() throws Exception {
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
      callArgsToString(call).contains('isGitRegionMatch: Missing regexps argument.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithEmptyRegexps() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(regexps: [])
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('isGitRegionMatch: Missing regexps with values.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithoutEnvVariables() throws Exception {
    def script = loadScript(scriptName)
    def result = true
    result = script.call(regexps: [ 'foo' ])
    printCallStack()
    assertFalse(result)
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'echo'
    }.any { call ->
      callArgsToString(call).contains('isGitRegionMatch: CHANGE_TARGET and GIT_SHA env variables are required to evaluate the changes.')
    })
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return 'file.txt'
        } else {
          return 0
        }
      })
    def result = false
    result = script.call(regexps: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return 'foo/anotherfolder/file.txt'
        } else {
          return 0
        }
      })
    def result = false
    result = script.call(regexps: [ '^foo/**/file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleUnmatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return 'file.txt'
        } else {
          return 1
        }
      })
    def result = false
    result = script.call(regexps: [ '^unknown.txt' ])
    printCallStack()
    assertFalse(result)
    assertJobStatusSuccess()
  }
}
