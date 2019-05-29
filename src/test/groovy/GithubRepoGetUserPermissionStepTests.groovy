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

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GithubRepoGetUserPermissionStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    helper.registerAllowedMethod("githubApiCall", [Map.class], { return [:]})
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/githubRepoGetUserPermission.groovy")
    def pr = script.call(token: 'token', repo: 'org/repo', user: 1)
    printCallStack()
    assertTrue(pr instanceof Map)
    assertJobStatusSuccess()
  }

  @Test
  void testErrorNoRepo() throws Exception {
    def script = loadScript("vars/githubRepoGetUserPermission.groovy")
    def pr = script.call(token: 'token', user: 1)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubRepoGetUserPermission: no valid repository.')
    })
  }

  @Test
  void testErrorNoUser() throws Exception {
    def script = loadScript("vars/githubRepoGetUserPermission.groovy")
    def pr = script.call(token: 'token', repo: 'org/repo')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubRepoGetUserPermission: no valid username.')
    })
  }
}
