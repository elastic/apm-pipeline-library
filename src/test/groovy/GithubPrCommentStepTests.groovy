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
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class GithubPrCommentStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubPrComment.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.GIT_BASE_COMMIT = '1'
    binding.getVariable('currentBuild').currentResult = 'SUCCESS'
  }

  @Test
  void testInBranch() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'githubPrComment: is only available for PRs.'))
    assertJobStatusSuccess()
  }

  @Test
  void testInPr() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_ID = 'PR-1'
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
    def result = script.commentTemplate(details: 'foo')
    printCallStack()
    assertTrue(result.contains('foo'))
    assertTrue(result.contains('Commit: 1'))
    assertTrue(result.contains('Build Succeeded'))
  }

  @Test
  void testCreateBuildInfoWithoutEnv() throws Exception {
    def script = loadScript(scriptName)
    def obj = script.createBuildInfo()
    printCallStack()
    assertJobStatusSuccess()
  }
}
