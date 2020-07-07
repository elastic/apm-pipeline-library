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

import co.elastic.mock.RunMock
import co.elastic.mock.RunWrapperMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class GithubPrCheckApprovedStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubPrCheckApproved.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
  }

  @Test
  void testNoPR() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testNotAllow() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return []
      })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'NONE']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return []
      })
    def runBuilding = new RunMock(building: true)
    def build = new RunWrapperMock(rawBuild: runBuilding, number: 1, result: 'FAILURE')
    binding.setVariable('currentBuild', build)
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubPrCheckApproved: The PR is not allowed to run in the CI yet'))
    assertTrue(assertMethodCallContainsPattern('log', "The PR is not allowed to run in the CI yet"))
    assertJobStatusFailure()
  }

  @Test
  void testIsApprobed() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return []
      })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'NONE']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return [
              [
                "id": 80,
                "node_id": "MDE3OlB1bGxSZXF1ZXN0UmV2aWV3ODA=",
                "user": [
                  "login": "octocat",
                  "id": 1,
                  "type": "User",
                  "site_admin": false
                ],
                "body": "Here is the body for the review.",
                "commit_id": "ecdd80bb57125d7ba9641ffaa4d7d2c19d3f3091",
                "state": "APPROVED",
                "author_association": "MEMBER",
              ]
            ]
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testIsRejected() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return []
      })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'NONE']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return [
              [
                "id": 80,
                "node_id": "MDE3OlB1bGxSZXF1ZXN0UmV2aWV3ODA=",
                "user": [
                  "login": "octocat",
                  "id": 1,
                  "type": "User",
                  "site_admin": false
                ],
                "body": "Here is the body for the review.",
                "commit_id": "ecdd80bb57125d7ba9641ffaa4d7d2c19d3f3091",
                "state": "REQUEST_CHANGES",
                "author_association": "MEMBER",
              ]
            ]
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    def ret = false
    try {
      ret = script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(ret)
    assertJobStatusFailure()
  }

  @Test
  void testHasWritePermision() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        "permission": "write",
        "user": [
          "login": "username",
        ]
      ]
    })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'MEMBER']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return []
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testHasAdminPermision() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        "permission": "admin",
        "user": [
          "login": "username",
        ]
      ]
    })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'MEMBER']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return []
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testIsAuthorizedBot() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        permission: "NONE",
        user: [
          login: "greenkeeper[bot]",
          type: "Bot",
        ]
      ]
    })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [
        title: 'dummy PR',
        user: [
          login: 'greenkeeper[bot]',
          type: "Bot",
          ],
        author_association: 'NONE'
        ]
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return []
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }


  @Test
  void testAPIContractViolationOnUserObject() throws Exception {
    helper.registerAllowedMethod("githubRepoGetUserPermission", [Map.class], {
      return [
        "permission": "write"
      ]
    })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', author_association: 'MEMBER']
      })
    helper.registerAllowedMethod("githubPrReviews", [Map.class], {
      return []
      })
    def script = loadScript(scriptName)
    env.CHANGE_ID = 1
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubPrCheckApproved: The PR is not allowed to run in the CI yet'))
    assertJobStatusFailure()
  }

  @Test
  void test_isAuthorizedUser_that_it_exists_in_the_approval_list() throws Exception {
    def script = loadScript(scriptName)
    env.REPO_NAME = 'apm-agent-go'
    helper.registerAllowedMethod('readYaml', [Map.class], { [ 'USERS' : ['v1v', 'another']] })
    def ret = script.isAuthorizedUser('v1v')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isAuthorizedUser_that_it_is_quite_similar_to_an_existing_one_in_the_approval_list() throws Exception {
    def script = loadScript(scriptName)
    env.REPO_NAME = 'apm-agent-go'
    helper.registerAllowedMethod('readYaml', [Map.class], { [ 'USERS' : ['v1v1', 'another']] })
    def ret = script.isAuthorizedUser('v1v')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isAuthorizedUser_with_an_existing_user_in_the_approval_list() throws Exception {
    def script = loadScript(scriptName)
    env.REPO_NAME = 'apm-agent-go'
    helper.registerAllowedMethod('readYaml', [Map.class], { '' })
    def ret = script.isAuthorizedUser('foo')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isAuthorizedUser_that_it_exists_in_the_approval_list_without_env_repo_name() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.isAuthorizedUser('v1v')
    helper.registerAllowedMethod('readYaml', [Map.class], { [ 'USERS' : ['v1v', 'another']] })
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
