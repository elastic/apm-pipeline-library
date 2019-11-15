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
  void testWithoutpatterns() throws Exception {
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
      callArgsToString(call).contains('isGitRegionMatch: Missing patterns argument.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithEmptypatterns() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(patterns: [])
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('isGitRegionMatch: Missing patterns with values.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithoutEnvVariables() throws Exception {
    def script = loadScript(scriptName)
    def result = true
    result = script.call(patterns: [ 'foo' ])
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
    result = script.call(patterns: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchWithoutShouldMatchAll() throws Exception {
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
    result = script.call(patterns: [ '^file.txt' ], shouldMatchAll: false)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.script.contains('git diff')) {
        return changeset
      } else {
        return 0
      }
    })
    def result = true
    result = script.call(patterns: [ '^file.txt' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexGlobMatch() throws Exception {
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
    result = script.call(patterns: [ '^foo/**/file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexGlobMatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return changeset
        } else {
          return 0
        }
      })
    def result = false
    result = script.call(patterns: [ '^foo/**/file.txt' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternMatch() throws Exception {
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
    result = script.call(patterns: [ '^foo/**/file.txt', '^bar/**/file*.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternsMatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = ''' foo/bar/file.txt
                      | foo/bar/xxx/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.script.contains('git diff')) {
        return changeset
      } else {
        return (m.script.contains('^foo/**/file.txt') || m.script.contains('^foo/bar/**/file.txt')) ? 0 : 1
      }
    })
    def result = false
    result = script.call(patterns: [ '^foo/**/file.txt', '^foo/bar/**/file.txt' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternMatchWithShouldMatchAllAndRegexpComparator() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = ''' foo/bar/file.txt
                      | foo/bar/xxx/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.script.contains('git diff')) {
        return changeset
      } else {
        return (m.script.contains('^foo/.*') || m.script.contains('^foo/bar/.*')) ? 0 : 1
      }
    })
    def result = false
    result = script.call(patterns: [ '^foo/.*', '^foo/bar/.*' ], shouldMatchAll: true, comparator: 'regexp')
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
    printCallStack()
    assertFalse(script.call(patterns: [ '^unknown.txt' ]))
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternUnmatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_TARGET = 'foo'
    env.GIT_SHA = 'bar'
    def changeset = ''' foo/bar/file.txt
                      | foo
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { return true })
    def result = false
    result = script.call(patterns: [ '^foo/**/file.txt', '^foo/bar/**/file.txt' ], shouldMatchAll: true)
    printCallStack()
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

  @Test
  void testIsGlob() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.isGlob('glob'))
    assertFalse(script.isGlob('regexp'))
    assertJobStatusSuccess()
  }

  @Test
  void testIsGrepPatternFound() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      return (m.script.contains('foo') ? 0 : 1)
    })
    assertTrue(script.isGrepPatternFound('foo', 'foo'))
    assertFalse(script.isGrepPatternFound('bar', 'pattern'))
    assertJobStatusSuccess()
  }
}
