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

class GithubCreatePullRequestStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubCreatePullRequest.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubCreatePullRequest: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_params() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubCreatePullRequest: title argument is required.'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_title() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken, passwordVariable=GITHUB_TOKEN, usernameVariable=GITHUB_USER'))
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo'"))
    assertFalse(assertMethodCallContainsPattern('sh', '--assign'))
    assertFalse(assertMethodCallContainsPattern('sh', '--reviewer'))
    assertFalse(assertMethodCallContainsPattern('sh', '--labels'))
    assertFalse(assertMethodCallContainsPattern('sh', '--draft'))
    assertFalse(assertMethodCallContainsPattern('sh', '--base'))
    assertFalse(assertMethodCallContainsPattern('sh', '--milestone'))
    assertTrue(assertMethodCallContainsPattern('sh', "git config remote.origin.url"))
    assertTrue(assertMethodCallContainsPattern('sh', 'sed "s#https://#https:'))
    assertJobStatusSuccess()
  }

  @Test
  void test_title_with_credentials() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=bar, passwordVariable=GITHUB_TOKEN, usernameVariable=GITHUB_USER'))
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_the_arguments() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', description: 'bar', assign: 'v1v', reviewer: 'r2p2', milestone: 'm1', labels: 'l1', base: 'master', draft: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo' --message 'bar' --draft --assign v1v --reviewer r2p2 --labels l1 --milestone m1 --base master"))
    assertJobStatusSuccess()
  }

  @Test
  void test_some_cornercases() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo foo', description: 'bar \n something else', reviewer: 'u3,u4', assign: 'u1,u2', labels: 'l1,l2')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo foo' --message 'bar \n something else'"))
    assertTrue(assertMethodCallContainsPattern('sh', '--assign u1,u2'))
    assertTrue(assertMethodCallContainsPattern('sh', '--reviewer u3,u4'))
    assertTrue(assertMethodCallContainsPattern('sh', '--labels l1,l2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_sh_error() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { s ->
      if(s.script.contains('hub pull-request')) {
        throw new Exception('Force an error')
      } else {
        'OK'
      }
    })
    def script = loadScript(scriptName)
    try {
      script.call(title: 'foo')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Force an error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'sed "s#.*@#https://#g"'))
    assertTrue(assertMethodCallContainsPattern('sh', 'git config remote.origin.url'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_force() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', force: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "--force"))
    assertJobStatusSuccess()
  }
}
