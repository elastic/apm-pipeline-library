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

class IsGitRegionMatchStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/isGitRegionMatch.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
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
  void testSimpleMatchWithoutExactMatch() throws Exception {
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
    result = script.call(regexps: [ '^file.txt' ], isExactMatch: false)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchWithExactMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { return changeset })
    def result = true
    result = script.call(regexps: [ '^file.txt' ], isExactMatch: true)
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
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: matched")
    })
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexMatchWithExactMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { return changeset })
    def result = false
    result = script.call(regexps: [ '^foo/**/file.txt' ], isExactMatch: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: matched")
    })
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultipleRegexpMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return true
        } else {
          if (m.script.contains('^bar/**/file*.txt')) {
            return 0
          } else {
            return 1
          }
        }
      })
    def result = false
    result = script.call(regexps: [ '^foo/**/file.txt', '^bar/**/file*.txt' ])
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: matched")
    })
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultipleRegexpMatchWithExactMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = ''' foo/bar/file.txt
                      | foo/bar/xxx/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { return true })
    def result = false
    result = script.call(regexps: [ '^foo/**/file.txt', '^foo/bar/**/file.txt' ], isExactMatch: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: matched")
    })
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
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: not matched")
    })
    assertFalse(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultipleRegexpUnmatchWithExactMatch() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = ''' foo/bar/file.txt
                      | foo
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { return true })
    def result = false
    result = script.call(regexps: [ '^foo/**/file.txt', '^foo/bar/**/file.txt' ], isExactMatch: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("isGitRegionMatch: not matched")
    })
    assertFalse(result)
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
      callArgsToString(call).contains('isGitRegionMatch: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }
}
