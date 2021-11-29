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

class GithubPullRequestsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubPullRequests.groovy')
  }

  @Test
  void test_with_no_data() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], { return '' })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_gh_error() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], { throw new Exception('unknown command "foo" for "gh issue"') })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_single_row() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return '#684	super-linter: enable it	v1v:fix/super-lint-defects' })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.size()==1)
    ret.each { k, v ->
      assertTrue(k == "684")
    }
  }

  @Test
  void test_with_multiple_data() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return '''#1078	Enable to customise GitHub PR comments	feature/override-notify-message-template
#684	super-linter: enable it	v1v:fix/super-lint-defects'''})
    def ret = script.call()
    printCallStack()
    println ret
    assertTrue(ret.size()==2)
    ret.each { k, v ->
      assertTrue(k == '684' || k == '1078')
    }
  }
  @Test
  void test_with_labels() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return '''#1078	Enable to customise GitHub PR comments	feature/override-notify-message-template'''})
    def ret = script.call(labels: ['foo', 'bar'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('gh', 'label=foo,bar'))
  }

  @Test
  void test_titleContains_with_spaces() throws Exception {
    script.call(titleContains: '[automation] foo bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('gh', 'search="[automation] foo bar" in:title'))
  }
}
