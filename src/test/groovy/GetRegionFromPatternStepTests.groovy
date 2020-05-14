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

class GetRegionFromPatternStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/getRegionFromPattern.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.CHANGE_TARGET = 'foo'
    env.GIT_BASE_COMMIT = 'bar'
  }

  @Test
  void test_without_pattern_parameter() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getRegionFromPattern: Missing pattern argument.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_env_variables() throws Exception {
    def script = loadScript(scriptName)
    def result = true
    env.remove('CHANGE_TARGET')
    result = script.call(pattern: 'foo')
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'getRegionFromPattern: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(patterns: '^file.txt')
    printCallStack()
    // TODO assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('log', "getRegionFromPattern: found with regex [^file.txt]"))
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_match_with_previous_commit_env_variable() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    // TODO
    def module = script.call(pattern: '^file.txt')
    printCallStack()
    //assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_complex_match() throws Exception {
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
    def module = script.call(pattern: '^foo/.*/file.txt')
    printCallStack()
    //assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_unmatch() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '^unknown.txt')
    printCallStack()
    assertTrue(module.equals(''))
    assertTrue(assertMethodCallContainsPattern('log', "getRegionFromPattern: not found with regex [^unknown.txt]"))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getRegionFromPattern: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_change_request_env_variable() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        assertFalse(m.script.contains('origin/'))
      })
    def module = script.call(pattern: 'foo')
    printCallStack()
    assertJobStatusSuccess()
    // TODO
  }

  @Test
  void test_with_empty_change_target_env_variable() throws Exception {
    env.CHANGE_TARGET = " "
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
        assertFalse(m.script.contains('origin/'))
      })
    def module = script.call(pattern: 'foo')
    printCallStack()
    assertJobStatusSuccess()
    // TODO
  }

  @Test
  void test_with_from_parameter() throws Exception {
    def script = loadScript(scriptName)
    def changeset = ''' foo/bar/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '^foo/.*/file.txt', from: 'something')
    printCallStack()
    // TODO assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...bar'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_from_and_to_parameters() throws Exception {
    def script = loadScript(scriptName)
    def changeset = ''' foo/bar/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '^foo/.*/file.txt', from: 'something', to: 'else')
    printCallStack()
    // TODO assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...else'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_values_for_from_and_to_parameters() throws Exception {
    def script = loadScript(scriptName)
    def module = script.call(pattern: '^foo/.*/file.txt', from: '', to: '')
    printCallStack()
    //TODO assertFalse(module)
    assertTrue(assertMethodCallContainsPattern('echo', 'getRegionFromPattern: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_eith_empty_value_for_to_parameter() throws Exception {
    def script = loadScript(scriptName)
    def module = script.call(patterns: [ '^foo/.*/file.txt' ], to: '')
    printCallStack()
    //TODO assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'getRegionFromPattern: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }
}
