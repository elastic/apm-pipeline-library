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

class PreCommitStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/preCommit.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.HOME = '/home'
  }

  @Test
  void testMissingCommitArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(commit: '')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'preCommit: git commit to compare with is required.'))
    assertJobStatusFailure()
  }

  @Test
  void testWithoutCommitAndEnvVariable() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'preCommit: git commit to compare with is required.'))
    assertJobStatusFailure()
  }

  @Test
  void testWithEnvVariable() throws Exception {
    def script = loadScript(scriptName)
    env.GIT_BASE_COMMIT = 'bar'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'bar | xargs pre-commit run --files'))
    assertTrue(assertMethodCallContainsPattern('withEnv', "HOME=${env.HOME}"))
    assertTrue(assertMethodCallContainsPattern('withEnv', "PATH=${env.HOME}/bin"))
    assertJobStatusSuccess()
  }

  @Test
  void testWithAllArguments() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', junit: true, credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sshagent', '[bar]'))
    assertTrue(assertMethodCallContainsPattern('sh', 'foo | xargs pre-commit run --files'))
    assertTrue(assertMethodCallContainsPattern('preCommitToJunit', 'input=pre-commit.out, output=pre-commit.out.xml'))
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=pre-commit.out.xml'))
    assertJobStatusSuccess()
  }

  void testWithRegistryAndSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', registry: 'bar', secretRegistry: 'mysecret')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('dockerLogin', '{secret=mysecret, registry=bar}'))
    assertJobStatusSuccess()
  }

  void testWithEmptyRegistryAndSecret() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo', registry: '', secretRegistry: '')
    printCallStack()
    assertFalse(assertMethodCall('dockerLogin'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithDefaultParameters() throws Exception {
    def script = loadScript(scriptName)
    script.call(commit: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sshagent', '[f6c7695a-671e-4f4f-a331-acdce44ff9ba]'))
    assertTrue(assertMethodCallContainsPattern('dockerLogin', '{secret=secret/apm-team/ci/docker-registry/prod, registry=docker.elastic.co}'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithoutHome() throws Exception {
    def script = loadScript(scriptName)
    env.remove('HOME')
    script.call(commit: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', "HOME=${env.WORKSPACE}"))
    assertTrue(assertMethodCallContainsPattern('withEnv', "PATH=${env.WORKSPACE}/bin"))
    assertJobStatusSuccess()
  }
}
