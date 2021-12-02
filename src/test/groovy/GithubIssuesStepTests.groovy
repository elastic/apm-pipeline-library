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

class GithubIssuesStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubIssues.groovy')
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
      return '277	OPEN	Document pre-commit validation	2019-11-22 18:22:29 +0000 UTC' })
    def ret = script.call()
    printCallStack()
    assertFalse(ret.isEmpty())
  }

  @Test
  void test_with_multiple_data() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return '''277	OPEN	Document pre-commit validation	2019-11-22 18:22:29 +0000 UTC
300	OPEN	Foo bar	2020-10-10 18:22:29 +0000 UTC'''})
    def ret = script.call()
    printCallStack()
    assertFalse(ret.isEmpty())
  }
}
