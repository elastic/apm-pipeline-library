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

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isGitRegionMatch.groovy')
    env.CHANGE_TARGET = 'foo'
    env.GIT_BASE_COMMIT = 'bar'
  }

  @Test
  void testWithoutpatterns() throws Exception {
    testMissingArgument('patterns') {
      script.call()
    }
  }

  @Test
  void testWithEmptypatterns() throws Exception {
    testMissingArgument('patterns', 'with values.') {
      script.call(patterns: [])
    }
  }

  @Test
  void testWithoutEnvVariables() throws Exception {
    def result = true
    env.remove('CHANGE_TARGET')
    env.remove('GIT_BASE_COMMIT')
    result = script.call(patterns: [ 'foo' ])
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'isGitRegionMatch: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_change_target_use_git_base_commit() throws Exception {
    env.GIT_BASE_COMMIT = 'bar'
    env.remove('CHANGE_TARGET')
    script.call(patterns: [ 'foo' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'bar...bar'))
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatch() throws Exception {
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: found with regex [^file.txt]"))
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleMatchPreviousCommit() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
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
    def changeset = ''' foo/bar/file.txt
                      | foo/bar/xxx/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*', '^foo/bar/.*' ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: found with regex [^foo/.*, ^foo/bar/.*]"))
    assertJobStatusSuccess()
  }

  @Test
  void testSimpleUnmatch() throws Exception {
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    printCallStack()
    assertFalse(script.call(patterns: [ '^unknown.txt' ]))
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: not found with regex [^unknown.txt]"))
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternUnmatchWithShouldMatchAll() throws Exception {
    def changeset = ''' foo/bar/file.txt
                      | foo
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt', '^foo/bar/.*/file.txt' ], shouldMatchAll: true)
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: not found with regex [^foo/.*/file.txt, ^foo/bar/.*/file.txt]"))
    assertJobStatusSuccess()
  }

  @Test
  void testBeatsOnlyDocsWithMatch() throws Exception {
    def changeset = ''' README.md
                      | deploy/kubernetes/auditbeat-kubernetes.yaml
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '(.*\\.(asciidoc|md)|deploy/kubernetes/.*-kubernetes\\.yaml)'  ], shouldMatchAll: true)
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: found with regex [(.*\\.(asciidoc|md)|deploy/kubernetes/.*-kubernetes\\.yaml)]"))
    assertJobStatusSuccess()
  }

  @Test
  void testBeatsOnlyDocsWithoutMatch() throws Exception {
    def changeset = ''' README.md
                      | deploy/kubernetes/auditbeat-kubernetes.yaml
                      | Makefile
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '(.*\\.(asciidoc|md)|deploy/kubernetes/.*-kubernetes\\.yaml)'  ], shouldMatchAll: true)
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('log', "isGitRegionMatch: not found with regex [(.*\\.(asciidoc|md)|deploy/kubernetes/.*-kubernetes\\.yaml)]"))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void testNoChangerequest() throws Exception {
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

  @Test
  void testWithFromAndTo() throws Exception {
    def changeset = ''' foo/bar/file.txt
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt' ], from: 'something', to: 'else')
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...else'))
    assertJobStatusSuccess()
  }

  @Test
  void testFromAndToWithEmptyValues() throws Exception {
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt' ], from: '', to: '')
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'isGitRegionMatch: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void testToWithEmptyValue() throws Exception {
    def result = false
    result = script.call(patterns: [ '^foo/.*/file.txt' ], to: '')
    printCallStack()
    assertFalse(result)
    assertTrue(assertMethodCallContainsPattern('echo', 'isGitRegionMatch: CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void testMultiplePatternMatchWithArrayString() throws Exception {
    def changeset = ''' foo
                      | bar
                    '''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    def String[] patterns = [ '^foo$', '^bar$' ]
    result = script.call(patterns: patterns)
    printCallStack()
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_branch_first_build() throws Exception {
    env.remove('GIT_PREVIOUS_COMMIT')
    env.remove('CHANGE_TARGET')
    env.GIT_BASE_COMMIT = 'bar'
    def changeset = 'file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def result = false
    result = script.call(patterns: [ '^file.txt' ])
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('sh', 'bar...bar'))
    assertJobStatusSuccess()
  }
}
