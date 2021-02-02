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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GithubPrCommentStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubPrComment.groovy')
    env.GIT_BASE_COMMIT = '1'
    env.ORG_NAME = 'elastic'
    env.REPO_NAME = 'apm-pipeline-library'
    env.CHANGE_ID = 'PR-1'
    binding.getVariable('currentBuild').currentResult = 'SUCCESS'
  }

  @Test
  void testInBranch() throws Exception {
    env.remove('CHANGE_ID')
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: is only available for PRs.'))
    assertJobStatusSuccess()
  }

  @Test
  void testInPr() throws Exception {
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testCommentTemplateWithFailure() throws Exception {
    binding.getVariable('currentBuild').currentResult = 'FAILURE'
    def result = script.commentTemplate()
    printCallStack()
    assertTrue(result.contains('Build Failed'))
  }

  @Test
  void testCommentTemplateWithDefault() throws Exception {
    def result = script.commentTemplate()
    printCallStack()
    assertFalse(result.contains('Further details'))
  }

  @Test
  void testCommentTemplateWithDetails() throws Exception {
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
    def result = script.commentTemplate()
    printCallStack()
    assertTrue(result.contains('redirect'))
    assertJobStatusSuccess()
  }

  @Test
  void testAddCommentForUnexisting() throws Exception {
    script.addOrEditComment(commentFile: 'comment.id', details: 'foo')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubTraditionalPrComment', 0))
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: Add a new comment.'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=comment.id'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'artifacts=comment.id'))
    assertJobStatusSuccess()
  }

  @Test
  void testAddCommentForUnexistingComment_with_file() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true} )
    helper.registerAllowedMethod('readFile', [String.class], { return '2' } )
    script.addOrEditComment(commentFile: 'comment.id', details: 'foo')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubTraditionalPrComment', 0))
    assertTrue(assertMethodCallContainsPattern('copyArtifacts', 'filter=comment.id'))
    assertTrue(assertMethodCallContainsPattern('log', "githubPrComment: Edit comment with id '2'."))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=comment.id'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'artifacts=comment.id'))
    assertJobStatusSuccess()
  }

  @Test
  void test_addOrEditComment_fallback_to_traditional() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true } )
    helper.registerAllowedMethod('readFile', [String.class], { return "${PullRequestMock.ERROR}" } )
    script.addOrEditComment(commentFile: 'comment.id', details: 'foo')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubTraditionalPrComment', 1))
    assertTrue(assertMethodCallContainsPattern('copyArtifacts', 'filter=comment.id'))
    assertTrue(assertMethodCallContainsPattern('log', "pullRequest.editComment failed with error"))
    assertFalse(assertMethodCallContainsPattern('log', 'githubPrComment: Add a new comment.'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=comment.id'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'artifacts=comment.id'))
    assertJobStatusSuccess()
  }

  @Test
  void test_addOrEditComment_fallback_to_addComment() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true } )
    helper.registerAllowedMethod('readFile', [String.class], { return "${PullRequestMock.ERROR}" } )
    helper.registerAllowedMethod('githubTraditionalPrComment', [Map.class], {
      throw new Exception('Something bad happened')
    })
    script.addOrEditComment(commentFile: 'comment.id', details: 'foo')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubTraditionalPrComment', 1))
    assertTrue(assertMethodCallContainsPattern('copyArtifacts', 'filter=comment.id'))
    assertTrue(assertMethodCallContainsPattern('log', "pullRequest.editComment failed with error"))
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: Add a new comment.'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=comment.id'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'artifacts=comment.id'))
    assertJobStatusSuccess()
  }

  @Test
  void test_override_default_message() throws Exception {
    def obj = script(message: 'foo')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('commentTemplate', ''))
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentFromFile_with_file() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true} )
    helper.registerAllowedMethod('readFile', [String.class], { return '2' } )
    def ret = script.getCommentFromFile(commentFile: 'comment.id')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('copyArtifacts', 'filter=comment.id'))
    assertTrue(assertMethodCallContainsPattern('readFile', 'comment.id'))
    assertEquals(ret, "2")
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentFromFile_without_file() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return false} )
    def ret = script.getCommentFromFile(commentFile: 'comment.id')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('copyArtifacts', 'filter=comment.id'))
    assertFalse(assertMethodCallContainsPattern('readFile', 'comment.id'))
    assertEquals(ret, '')
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentIfAny_with_file_match() {
	  
    helper.registerAllowedMethod('fileExists', [String.class], { return true } )
    helper.registerAllowedMethod('readFile', [String.class], { return '2' } )
    def ret = script.getCommentIfAny(commentFile: 'comment.id')
    printCallStack()
    assertEquals(ret, 2)
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentIfAny_without_file_match_and_pr_comment_match() {
    helper.registerAllowedMethod('fileExists', [String.class], { return false } )
    helper.registerAllowedMethod('githubPrLatestComment', [Map.class], {
      return [
          url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/comments/2",
          issue_url: "https://api.github.com/repos/elastic/apm-pipeline-library/issues/1",
          id: 2,
          user: [
            login: "elasticmachine",
            id: 3,
          ],
          created_at: "2020-02-03T17:21:44Z",
          updated_at: "2020-02-03T17:21:44Z",
          body: "<!--COMMENT_GENERATED_WITH_ID_comment.id-->"
        ]
    } )
    def ret = script.getCommentIfAny(commentFile: 'comment.id')
    printCallStack()
    assertEquals(ret, 2)
    assertTrue(assertMethodCallContainsPattern('githubPrLatestComment', '<!--COMMENT_GENERATED_WITH_ID_comment.id-->'))
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentIfAny_without_any_match() {
    helper.registerAllowedMethod('fileExists', [String.class], { return false } )
    helper.registerAllowedMethod('githubPrLatestComment', [Map.class], null)
    def ret = script.getCommentIfAny(commentFile: 'comment.id')
    printCallStack()
    assertEquals(ret, script.errorId())
    assertTrue(assertMethodCallContainsPattern('githubPrLatestComment', '<!--COMMENT_GENERATED_WITH_ID_comment.id-->'))
    assertJobStatusSuccess()
  }

  @Test
  void test_getCommentIfAny_without_any_match_and_an_exception() {
    helper.registerAllowedMethod('fileExists', [String.class], { return false } )
    helper.registerAllowedMethod('githubPrLatestComment', [Map.class], { throw new Exception('Force failure') })
    def ret = script.getCommentIfAny(commentFile: 'comment.id')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubPrLatestComment', '<!--COMMENT_GENERATED_WITH_ID_comment.id-->'))
    assertTrue(assertMethodCallContainsPattern('log', 'a new GitHub comment will be created'))
    assertEquals(ret, script.errorId())
    assertJobStatusSuccess()
  }

  @Test
  void test_metadata() {
    def ret = script.metadata(commentFile: 'foo')
    printCallStack()
    assertTrue(ret.equals('<!--COMMENT_GENERATED_WITH_ID_foo-->'))
    assertJobStatusSuccess()
  }

  @Test
  void test_addComment_fallback_to_githubTraditionalPrComment() throws Exception {
    def ret = script.addComment('error')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubTraditionalPrComment', 'message=error'))
    assertJobStatusSuccess()
  }

  @Test
  void test_editComment_fallback_to_githubTraditionalPrComment() throws Exception {
    script.editComment(PullRequestMock.ERROR, 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubTraditionalPrComment', "message=foo, id=${PullRequestMock.ERROR}"))
    assertJobStatusSuccess()
  }
}
