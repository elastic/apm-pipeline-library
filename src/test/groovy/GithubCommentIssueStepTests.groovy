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

class GithubCommentIssueStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubCommentIssue.groovy')
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_without_params() throws Exception {
    testMissingArgument('comment') {
      script.call()
    }
  }

  @Test
  void test_without_id() throws Exception {
    testMissingArgument('id') {
      script.call(comment: 'foo')
    }
  }

  @Test
  void test_without_org_repo() throws Exception {
    env.remove('ORG_NAME')
    try {
      script.call(id: '1', comment: 'foo')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubCommentIssue: org/repo are empty'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_env_variables() throws Exception {
    env.ORG_NAME = 'acme'
    env.REPO_NAME = 'foo'
    script.call(id: '1', comment: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub api repos/acme/foo/issues/1/comments -f body='foo'"))
    assertJobStatusSuccess()
  }
  
  @Test
  void test_with_all_the_arguments() throws Exception {
    script.call(id: '1', comment: 'foo', org: 'acme', repo: 'my-repo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+HUB'))
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "hub api repos/acme/my-repo/issues/1/comments -f body='foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_credentials() throws Exception {
    script.call(id: '1', comment: 'foo', org: 'acme', repo: 'my-repo', credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=bar, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "hub"))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_lines() throws Exception {
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: 'bar \n something else')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "body='bar \n something else'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_normalisation() throws Exception {
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "'bar'")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "body='\"bar\"'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "foo")
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "bar")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+HUB'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache_without_gh_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "foo")
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "bar")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+HUB'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertTrue(assertMethodCallContainsPattern('log', 'githubCommentIssue: get the hubLocation from cache.'))
    assertTrue(assertMethodCallContainsPattern('log', 'githubCommentIssue: set the hubLocation.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_hub_already_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "foo")
    script.call(id: '1', org: 'acme', repo: 'my-repo', comment: "bar")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+HUB'))
    assertFalse(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }
}
