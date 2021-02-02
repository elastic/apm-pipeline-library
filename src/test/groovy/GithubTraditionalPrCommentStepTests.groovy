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

import co.elastic.mock.PullRequestMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class GithubTraditionalPrCommenttStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubTraditionalPrComment.groovy'

  def commentInterceptor = [
    id: 1,
    node_id: "MDEyOklzc3VlQ29tbWVudDE=",
    url: "https://api.github.com/repos/octocat/Hello-World/issues/comments/1",
    html_url: "https://github.com/octocat/Hello-World/issues/1347#issuecomment-1",
    body: "Me too",
    user: [
        login: "octocat",
        id: 1,
    ],
    created_at: "2011-04-14T16:00:49Z",
    updated_at: "2011-04-14T16:00:49Z"
  ]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.ORG_NAME = 'octocat'
    env.REPO_NAME = 'Hello-World'
    env.CHANGE_ID = 'PR-1'
    helper.registerAllowedMethod('githubApiCall', [Map.class], {	
      return commentInterceptor	
    })
  }

  @Test
  void test_missing_details_parameter() throws Exception {
    def script = loadScript(scriptName)
    testMissingArgument('message') {
      script.call()
    }
  }

  @Test
  void test_in_a_branch() throws Exception {
    def script = loadScript(scriptName)
    env.remove('CHANGE_ID')
    script.call(message: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'is only available for PRs.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_add_a_new_comment() throws Exception {	
    def script = loadScript(scriptName)
    def obj = script.call(message: 'Me too')
    printCallStack()
    assertEquals(obj, 1)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'POST'))
  }

  @Test
  void test_edit_a_comment() throws Exception {	
    def script = loadScript(scriptName)
    def obj = script.call(message: 'Me too', id: 1)
    printCallStack()
    assertEquals(obj, 1)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'PATCH'))
  }
}
