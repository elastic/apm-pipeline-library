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
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class GithubPrCommentStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubPrComment.groovy'

  def commentInterceptor = [[
      url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/comments/2",
      issue_url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/1",
      id: 2,
      user: [
        login: "elasticmachine",
        id: 3,
      ],
      created_at: "2020-01-03T16:16:26Z",
      updated_at: "2020-01-03T16:16:26Z",
      body: "\n## :green_heart: Build Succeeded\n* [pipeline](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/PR-1/2/display/redirect)\n* Commit: 1\n\n\n<!--PIPELINE\n{\"commit\":\"1\",\"number\":\"2\",\"status\":\"SUCCESS\",\"url\":\"https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/PR-1/2/\"}\nPIPELINE-->\n"
    ],
    [
      url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/comments/55",
      issue_url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/11",
      id: 55,
      user: [
        login: "foo",
        id: 11,
      ],
      created_at: "2020-01-04T16:16:26Z",
      updated_at: "2020-01-04T16:16:26Z",
      body: "LGTM"
    ],
  ]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.GIT_BASE_COMMIT = '1'
    env.ORG_NAME = 'elastic'
    env.REPO_NAME = 'apm-pipeline-library'
    env.CHANGE_ID = 'PR-1'
    binding.getVariable('currentBuild').currentResult = 'SUCCESS'
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return commentInterceptor
    })
  }

  @Test
  void testInBranch() throws Exception {
    def script = loadScript(scriptName)
    env.remove('CHANGE_ID')
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: is only available for PRs.'))
    assertJobStatusSuccess()
  }

  @Test
  void testInPr() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testCommentTemplateWithFailure() throws Exception {
    def script = loadScript(scriptName)
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    def result = script.commentTemplate()
    printCallStack()
    assertTrue(result.contains('Build Failed'))
  }

  @Test
  void testCommentTemplateWithDefault() throws Exception {
    def script = loadScript(scriptName)
    def result = script.commentTemplate()
    printCallStack()
    assertFalse(result.contains('Further details'))
  }

  @Test
  void testCommentTemplateWithDetails() throws Exception {
    def script = loadScript(scriptName)
    env.RUN_DISPLAY_URL = ''
    def result = script.commentTemplate(details: 'foo')
    printCallStack()
    assertFalse(result.contains('redirect'))
    assertTrue(result.contains('foo'))
    assertTrue(result.contains('Commit: 1'))
    assertTrue(result.contains('Build Succeeded'))
  }

  @Test
  void testCreateBuildInfoWithRunDisplay() throws Exception {
    def script = loadScript(scriptName)
    def result = script.commentTemplate()
    printCallStack()
    assertTrue(result.contains('redirect'))
    assertJobStatusSuccess()
  }

  @Test
  void testCreateBuildInfoWithoutEnv() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.createBuildInfo()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testGetComments() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.getComments()
    printCallStack()
    assertTrue(obj.size == 2)
    assertJobStatusSuccess()
  }

  @Test
  void testGetLatestBuildComment() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.getLatestBuildComment()
    printCallStack()
    assertNotNull(obj)
    assertJobStatusSuccess()
  }

  @Test
  void testEditCommentForExistingId() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.addOrEditComment('foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "githubPrComment: Edit comment with id '2'."))
    assertJobStatusSuccess()
  }

  @Test
  void testAddCommentForUnexisting() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return [[]]} )
    def obj = script.addOrEditComment('foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: Add a new comment.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_override_default_message() throws Exception {
    def script = loadScript(scriptName)
    def obj = script(message: 'foo')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('commentTemplate', ''))
    assertJobStatusSuccess()
  }
}
