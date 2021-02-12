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

class CodecovStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/codecov.groovy')

    env.BRANCH_NAME = "branch"
    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'

    helper.registerAllowedMethod("readJSON", [Map.class], {return [
      head: [
        repo: [
          owner: [
            login: 'user'
          ]
        ],
        ref: 'refs/'
      ]]})
  }

  @Test
  void testNoRepo() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Codecov: No repository specified.'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoToken() throws Exception {
    script.call(repo: "noToken", secret: "secret-bad")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Codecov: Repository not found: noToken'))
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    script.call(repo: "repo", basedir: "ws", secret: VaultSecret.SECRET_CODECOV.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('dir', 'ws'))
    assertTrue(assertMethodCallContainsPattern('sh', 'curl -sSLo codecov.sh https://codecov.io/bash'))
    assertTrue(assertMethodCallContainsPattern('sh', 'bash codecov.sh  ||'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags() throws Exception {
    script.call(repo: 'repo', flags: 'foo', secret: VaultSecret.SECRET_CODECOV.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('dir', '.'))
    assertTrue(assertMethodCallContainsPattern('sh', 'bash codecov.sh foo'))
    assertJobStatusSuccess()
  }

  @Test
  void testCache() throws Exception {
    script.call(repo: "repo", basedir: "ws", secret: VaultSecret.SECRET_CODECOV.toString())
    script.call(repo: "repo", basedir: "ws", secret: VaultSecret.SECRET_CODECOV.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Codecov: get the token from Vault.'))
    assertTrue(assertMethodCallContainsPattern('log', 'Codecov: get the token from cache.'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }
}
