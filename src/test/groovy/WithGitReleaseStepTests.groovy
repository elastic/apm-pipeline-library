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
  String scriptName = 'vars/withGitRelease.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.GITHUB_USER = 'user'
    env.GITHUB_TOKEN = 'token'
    env.GIT_BASE_COMMIT = 'commit'
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
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
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    def isOK = false
    try {
      script.call() {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGitRelease: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_body_error() throws Exception {
    def script = loadScript(scriptName)
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
  void test_missing_github_user() throws Exception {
    def script = loadScript(scriptName)
    env.remove('GITHUB_USER')
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withGitRelease: GITHUB_USER / GITHUB_TOKEN have not been set'))
    assertJobStatusFailure()
  }

  @Test
  void test_missing_base_commit() throws Exception {
    def script = loadScript(scriptName)
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
}
