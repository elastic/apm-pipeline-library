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
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class GithubEnvStepTests extends ApmBasePipelineTest {
  String SHA1 = "${SHA}11"

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubEnv.groovy')

    env.GIT_URL = null
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git rev-list HEAD --parents -1'.equals(map.script)) {
        return "${SHA} ${SHA}"
      } else if(map.script.startsWith("git rev-parse origin/pr/")){
        return "${SHA1}"
      }
      return ""
    })
  }

  @Test
  void testNoGitURL() throws Exception {
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }

  @Test
  void testGitUrl() throws Exception {
    env.GIT_URL = REPO_URL
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }

  @Test
  void testChangeTarget() throws Exception {
    env.CHANGE_TARGET = "NotEmpty"
    env.CHANGE_ID = "NotEmpty"
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }

  @Test
  void testMerge() throws Exception {
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git rev-list HEAD --parents -1'.equals(map.script)) {
          return "${SHA} ${SHA} ${SHA}"
      }
      return ""
    })
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('merge'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }

  @Test
  void testSshUrl() throws Exception {
    env.GIT_URL = 'git@github.com:org/repo.git'
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }

  @Test
  void testChangeTargetBaseCommitOnNoMergeChangesInPR() throws Exception {
    env.CHANGE_TARGET = "NotEmpty"
    env.CHANGE_ID = "NotEmpty"
    env.GIT_COMMIT = SHA
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(SHA1.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnNoGitCommit() throws Exception {
    env.CHANGE_TARGET = "NotEmpty"
    env.CHANGE_ID = "NotEmpty"
    env.GIT_COMMIT = null
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(SHA1.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnMergeChangesInPR() throws Exception {
    env.CHANGE_ID = "NotEmpty"
    env.CHANGE_TARGET = "NotEmpty"
    env.GIT_COMMIT = 'different'
    env.SHA = 'different'
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    println(binding.getVariable('env').GIT_BASE_COMMIT)
    assertTrue(SHA1.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnBranch() throws Exception {
    env.CHANGE_ID = null
    env.CHANGE_TARGET = null
    env.GIT_COMMIT = SHA
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(SHA.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }
}
