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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class IsGitRegionMatchStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/isGitRegionMatch.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.CHANGE_TARGET = 'foo'
    env.GIT_BASE_COMMIT = 'bar'
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
    assertTrue(assertMethodCallContainsPattern('error', 'isGitRegionMatch: Missing patterns argument.'))
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
    assertTrue(assertMethodCallContainsPattern('error', 'isGitRegionMatch: Missing patterns with values.'))
    assertJobStatusFailure()
  }

  @Test
  void testWithoutEnvVariables() throws Exception {
    def script = loadScript(scriptName)
    def result = true
    env.remove('CHANGE_TARGET')
    result = script.call(patterns: [ 'foo' ])
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'isGitRegionMatch: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatch() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchPreviousCommit() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchWithoutShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^file.txt' ], shouldMatchAll: false)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexMatch() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        if (m.script.contains('git diff')) {
          return 'foo/anotherfolder/file.txt'
        } else {
          return 0
        }
      })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt' ])
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testComplexWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt', '^foo/.*/.*\\.txt' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternMatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    def changeset = ''' foo/bar/file.txt
                      | foo/bar/xxx/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*', '^foo/bar/.*' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleUnmatch() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    printCallStack()
    assertFalse(script.call(patterns: [ '^unknown.txt' ]))
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternUnmatchWithShouldMatchAll() throws Exception {
    def script = loadScript(scriptName)
    def changeset = ''' foo/bar/file.txt
                      | foo
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt', '^foo/bar/.*/file.txt' ], shouldMatchAll: true)
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
    assertTrue(assertMethodCallContainsPattern('error', 'isGitRegionMatch: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void testNoChangerequest() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        assertTrue(m.script.contains('origin/'))
      })
    def result = false
    result = script.call(patterns: [ 'foo' ])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testChangerequest() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        assertFalse(m.script.contains('origin/'))
      })
    def result = false
    result = script.call(patterns: [ 'foo' ])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testChangerTargetEmpty() throws Exception {
    env.CHANGE_TARGET = " "
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        assertFalse(m.script.contains('origin/'))
      })
    def result = false
    result = script.call(patterns: [ 'foo' ])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testWithFrom() throws Exception {
    def script = loadScript(scriptName)
    def changeset = ''' foo/bar/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt' ], from: 'something')
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...bar'))
    assertJobStatusSuccess()
  }
}
