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
  String scriptName = 'vars/lookForGitHubIssues.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_with_no_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_githubIssues_error() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubIssues', [Map.class], { throw new Exception('unknown command "foo" for "gh issue"') })
    def ret = script.call(flakyList: [ 'test-foo' ])
    printCallStack()
    assertTrue(ret['test-foo'].equals(''))
  }

  @Test
  void test_without_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubIssues', [Map.class], {
      return [ 1 : [ state: 'OPEN', title: 'title' ]] })
    def ret = script.call(flakyList: [ 'test-foo' ])
    printCallStack()
    assertTrue(ret['test-foo'].equals(''))
  }

  @Test
  void test_with_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubIssues', [Map.class], {
      return [ 1 : [ state: 'OPEN', title: 'title [test-foo]' ]] })
    def ret = script.call(flakyList: [ 'test-foo' ])
    println ret
    printCallStack()
    assertTrue(ret['test-foo'] == 1)
  }
}
