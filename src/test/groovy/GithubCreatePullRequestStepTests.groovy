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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GithubCreatePullRequestStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubCreatePullRequest.groovy')
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_without_params() throws Exception {
    testMissingArgument('title') {
      script.call()
    }
  }

  @Test
  void test_with_title() throws Exception {
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
    script.call(title: 'foo', credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=bar, passwordVariable=GITHUB_TOKEN, usernameVariable=GITHUB_USER'))
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_the_arguments() throws Exception {
    script.call(title: 'foo', description: 'bar', assign: 'v1v', reviewer: 'r2p2', milestone: 'm1', labels: 'l1', base: 'master', draft: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo' --message 'bar' --draft --assign v1v --reviewer r2p2 --labels l1 --milestone m1 --base master"))
    assertJobStatusSuccess()
  }

  @Test
  void test_some_cornercases() throws Exception {
    script.call(title: 'foo foo', description: 'bar \n something else', reviewer: 'u3,u4', assign: 'u1,u2', labels: 'l1,l2')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request --push --message 'foo foo' --message 'bar \n something else'"))
    assertTrue(assertMethodCallContainsPattern('sh', '--assign u1,u2'))
    assertTrue(assertMethodCallContainsPattern('sh', '--reviewer u3,u4'))
    assertTrue(assertMethodCallContainsPattern('sh', '--labels l1,l2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_assign() throws Exception {
    script.call(title: 'foo', credentialsId: 'bar', assign: '')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request"))
    assertFalse(assertMethodCallContainsPattern('sh', '--assign'))
    assertJobStatusSuccess()

  }

  @Test
  void test_with_empty_reviewer() throws Exception {
    script.call(title: 'foo', credentialsId: 'bar', reviewer: '')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub pull-request"))
    assertFalse(assertMethodCallContainsPattern('sh', '--reviewer'))
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
    def ret
    try {
      ret = script.call(title: 'foo')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Force an error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'sed "s#.*@#https://#g"'))
    assertTrue(assertMethodCallContainsPattern('sh', 'git config remote.origin.url'))
    assertNull(ret)
    assertJobStatusFailure()
  }

  @Test
  void test_with_force() throws Exception {
    script.call(title: 'foo', force: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "--force"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_stdout() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { s ->
      return 'https://github.com/acme/my-repo/pull/1'
    })
    def ret = script.call(title: 'foo')
    printCallStack()
    assertTrue(ret.equals('https://github.com/acme/my-repo/pull/1'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiline_with_quotes() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { s ->
      return 'https://github.com/acme/my-repo/pull/1'
    })
    def actions = """
        1. A single quote ' foo
        1. Something else."""
    try {
      script.call(title: 'foo', description: "${actions}")
    } catch(err) {
      //NOOP
    }
    assertTrue(assertMethodCallContainsPattern('error', 'single quotes are not allowed'))
    assertJobStatusFailure()
  }
}
