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
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GithubBranchRefStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubBranchRef.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
    env.CHANGE_ID = '1'
    helper.registerAllowedMethod('githubPrInfo', [Map.class], {
      return [
        head: [
          ref: 'master',
          repo: [
            owner: [
              login: 'username'
            ]
          ]
        ],
        title: 'dummy PR',
        user: [login: 'username'],
        author_association: 'NONE'
        ]
      })
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call()
    printCallStack()
    assertTrue(ret == 'username/master')
    assertJobStatusSuccess()
  }

  @Test
  void testNoPR() throws Exception {
    env.CHANGE_ID = null
    def script = loadScript(scriptName)
    def ret = script.call()
    printCallStack()
    assertTrue(ret == 'master')
    assertJobStatusSuccess()
  }

  @Test
  void testEnvError() throws Exception {
    env.ORG_NAME = null
    env.REPO_NAME = null
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubBranchRef: Environment not initialized, try to call githubEnv step before')
    })
  }
}
