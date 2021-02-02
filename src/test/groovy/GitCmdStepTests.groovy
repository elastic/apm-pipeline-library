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

class GitCmdStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/gitCmd.groovy')
    addEnvVar("ORG_NAME", "my_org")
    addEnvVar("REPO_NAME", "my_repo")
    addEnvVar('GIT_PASSWORD', 'password')
    addEnvVar('GIT_USERNAME', 'username')
    helper.registerAllowedMethod('readFile', [String.class], { return 'git clone https://username:password@repo.git' })
  }

  @Test
  void test() throws Exception {
    script.call(cmd: 'push')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', '> push.log'))
    assertFalse(assertMethodCallContainsPattern('archiveArtifacts', 'push.log'))
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    script.call(cmd: "push", credentialsId: "my_credentials", args: '-f')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testNoCmd() throws Exception {
    testMissingArgument('cmd') {
      script.call(credentialsId: "my_credentials", args: '-f')
    }
  }

  @Test
  void testParamsWithEmptyCredentials() throws Exception {
    script.call(cmd: "push", credentialsId: '', args: '-f')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('usernamePassword', '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken'))
    assertJobStatusSuccess()
  }

  @Test
  void testParamsWithAnotherCredentials() throws Exception {
    script.call(cmd: "push", credentialsId: 'foo', args: '-f')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('usernamePassword', 'foo'))
    assertJobStatusSuccess()
  }

  @Test
  void testCmdIsPopulated() throws Exception {
    script.call(cmd: 'push', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'script=git push'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_less_verbose_output() throws Exception {
    script.call(cmd: 'push', credentialsId: 'foo', store: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'script=git push'))
    assertTrue(assertMethodCallContainsPattern('sh', '> .git/push.log 2>&1'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'push.log'))
    assertJobStatusSuccess()
  }

  @Test
  void test_if_store_with_error_works() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], {
      throw new Exception('Force Failure an error')
    })
    try{
      script.call(cmd: 'push', credentialsId: 'foo', store: true)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'push.log'))
    assertTrue(assertMethodCallContainsPattern('readFile', 'push.log'))
  }

  @Test
  void test_if_store_in_git_folder_no_exist() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return false })
    script.call(cmd: 'push', credentialsId: 'foo', store: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'push.log'))
    assertTrue(assertMethodCallContainsPattern('readFile', 'push.log'))
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }
}
