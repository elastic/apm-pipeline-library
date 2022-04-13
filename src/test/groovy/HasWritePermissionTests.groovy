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

class HasWritePermissionTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/hasWritePermission.groovy')
    helper.registerAllowedMethod('githubApiCall', [Map.class], { m -> [ user: [ login: "foo" ] ] })
  }

  @Test
  void test() throws Exception {
    def ret = script.call(repoName: "owner/repo", commentId: "1")
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'url=https://api.github.com/repos/owner/repo/issues/comments/1'))
    assertTrue(assertMethodCallContainsPattern('githubRepoGetUserPermission', 'repo=owner/repo'))
    assertTrue(assertMethodCallContainsPattern('githubRepoGetUserPermission', 'user=foo'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoRepo() throws Exception {
    testError("hasWritePermission: repoName params is required"){
      script.call(commentId: "1")
    }
  }

  @Test
  void test_with_empty_value() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], { return [:] })
    def ret = script.call(repoName: "elastic/repo", commentId: "1")
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_admin_match() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        "permission": "admin",
        "user": [
          "login": "username",
        ]
      ]
    })
    def ret = script.call(repoName: "elastic/repo", commentId: "1")
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_write_match() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        "permission": "write",
        "user": [
          "login": "username",
        ]
      ]
    })
    def ret = script.call(repoName: "elastic/repo", commentId: "1")
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_invalid_repo_format() throws Exception {
    try {
      script.call(repoName: "repo", commentId: "1")
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'hasCommentAuthorWritePermissions: invalid repository format'))
  }
}
