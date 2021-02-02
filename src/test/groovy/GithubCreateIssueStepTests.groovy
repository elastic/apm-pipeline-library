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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GithubCreateIssueStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubCreateIssue.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_without_params() throws Exception {
    def script = loadScript(scriptName)
    testMissingArgument('title') {
      script.call()
    }
  }

  @Test
  void test_with_title() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "issue create --title='foo'"))
    assertFalse(assertMethodCallContainsPattern('sh', '--assignee'))
    assertFalse(assertMethodCallContainsPattern('sh', '--label'))
    assertFalse(assertMethodCallContainsPattern('sh', '--milestone'))
    assertJobStatusSuccess()
  }

  @Test
  void test_title_with_credentials() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=bar, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "issue create --title='foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_the_arguments() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', description: 'bar', assign: 'v1v', milestone: 'm1', labels: 'l1')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "issue create --assignee='v1v' --label='l1' --milestone='m1' --title='foo' --body='bar'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_some_cornercases() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo foo', description: 'bar \n something else', assign: 'u1,u2', labels: 'l1,l2')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "--title='foo foo'"))
    assertTrue(assertMethodCallContainsPattern('sh', "--body='bar \n something else'"))
    assertTrue(assertMethodCallContainsPattern('sh', "--assignee='u1,u2'"))
    assertTrue(assertMethodCallContainsPattern('sh', "--label='l1,l2'"))
    assertJobStatusSuccess()
  }
}
