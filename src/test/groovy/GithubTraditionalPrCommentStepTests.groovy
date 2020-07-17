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
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GithubTraditionalPrCommenttStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubTraditionalPrComment.groovy'

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
      body: "\n## :green_heart: Build Succeeded\n* [pipeline](https://apm-ci.elastic.co/job/apm-shared/job/apm-pipeline-library-mbp/job/PR-1/2/display/redirect)\n* Commit: 1\n\n\n<!--COMMENT_GENERATED-->\n"
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
    env.ORG_NAME = 'elastic'
    env.REPO_NAME = 'apm-pipeline-library'
    env.CHANGE_ID = 'PR-1'
    helper.registerAllowedMethod('githubApiCall', [Map.class], {	
      return commentInterceptor	
    })
  }

  @Test
  void test_missing_details_parameter() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'message parameter is required'))
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
    def obj = script.call(message: '<!--COMMENT_GENERATED-->')
    printCallStack()
    assertNotNull(obj)
  }

  @Test
  void test_edit_a_comment() throws Exception {	
    def script = loadScript(scriptName)
    def obj = script.call(message: '<!--COMMENT_GENERATED-->', id: 1)
    printCallStack()
    assertNotNull(obj)
  }
}
