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

class LookForGitHubIssuesStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/lookForGitHubIssues.groovy')
  }

  @Test
  void test_with_no_data() throws Exception {
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_githubIssues_error() throws Exception {
    helper.registerAllowedMethod('githubIssues', [Map.class], { throw new Exception('unknown command "foo" for "gh issue"') })
    def ret = script.call(flakyList: [ 'test-foo' ])
    assertTrue(ret['test-foo'].equals(''))
    printCallStack()
  }

  @Test
  void test_flakySearch_without_match() throws Exception {
    def issues = [ 1 : [ state: 'OPEN', title: 'title' ]]
    def ret = script.searchFlakyIssues(flakyList: [ 'test-foo' ], issues: issues)
    printCallStack()
    assertTrue(ret['test-foo'].equals(''))
  }

  @Test
  void test_flakySearch_with_match() throws Exception {
    def issues = [ 1 : [ state: 'OPEN', title: 'title [test-foo]' ]]
    def ret = script.searchFlakyIssues(flakyList: [ 'test-foo' ], issues: issues)
    printCallStack()
    assertTrue(ret['test-foo'] == 1)
  }

  @Test
  void test_with_error() throws Exception {
    helper.registerAllowedMethod('githubIssues', [Map.class], { throw new Exception('force a failure') })
    def ret = script.call(flakyList: [ 'test-foo' ])
    printCallStack()
    assertTrue(ret.containsKey('test-foo'))
  }
}
