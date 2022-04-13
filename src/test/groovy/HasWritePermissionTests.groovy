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
  }

  @Test
  void test() throws Exception {
    def ret = script.call(repo: "owner/repo", user: "foo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubRepoGetUserPermission', 'repo=owner/repo'))
    assertTrue(assertMethodCallContainsPattern('githubRepoGetUserPermission', 'user=foo'))
  }

  @Test
  void test_missing_repo() throws Exception {
    testMissingArgument('repo') {
      script.call()
    }
  }

  @Test
  void test_missing_user() throws Exception {
    testMissingArgument('user') {
      script.call(repo: "owner/repo")
    }
  }

  @Test
  void test_with_no_match() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], { return [:] })
    def ret = script.call(repo: "elastic/repo", user: "foo")
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
    def ret = script.call(repo: "elastic/repo", user: "foo")
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
    def ret = script.call(repo: "elastic/repo", user: "foo")
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_invalid_repo_format() throws Exception {
    try {
      script.call(repo: "repo", user: "foo")
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'hasWritePermission: invalid repository format'))
  }
}
