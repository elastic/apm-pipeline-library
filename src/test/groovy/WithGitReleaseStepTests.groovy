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

class WithGitReleaseStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withGitRelease.groovy')
    env.GITHUB_USER = 'user'
    env.GITHUB_TOKEN = 'token'
    env.GIT_BASE_COMMIT = 'commit'
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
  }

  @Test
  void test() throws Exception {
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Setup git release'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Rollback git context'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Rollback git context'))
    assertTrue(assertMethodCallContainsPattern('sh', 'remote.origin.url "https://github.com/org/repo.git"'))
    assertTrue(assertMethodCallContainsPattern('sh', 'remote.upstream.url "https://github.com/org/repo.git"'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call(){}
    }
  }

  @Test
  void test_with_body_error() throws Exception {
    try {
      script.call {
        throw new Exception('Mock an error')
      }
    } catch(e){
      //NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'Setup git release'))
    assertTrue(assertMethodCallContainsPattern('error', 'withGitRelease: error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Rollback git context'))
    assertJobStatusFailure()
  }

  @Test
  void test_missing_base_commit() throws Exception {
    env.remove('GIT_BASE_COMMIT')
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGitRelease: GIT_BASE_COMMIT has not been set'))
    assertJobStatusFailure()
  }

  @Test
  void test_missing_branch_name() throws Exception {
    // When running simple pipelines but no Multibranch Pipelines
    env.remove('BRANCH_NAME')
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGitRelease: BRANCH_NAME has not been set'))
    assertJobStatusFailure()
  }
}
