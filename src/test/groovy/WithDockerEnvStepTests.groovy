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

class WithDockerEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withDockerEnv.groovy')
  }

  @Test
  void test() throws Exception {
    script.call(secret: VaultSecret.SECRET_NAME.toString()){}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void testDockerContext() throws Exception {
    def isOK = false
    script.call(secret: VaultSecret.SECRET_NAME.toString()){
      if(binding.getVariable("DOCKER_USER") == "username"
      && binding.getVariable("DOCKER_PASSWORD") == "user_password"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testRegistry() throws Exception {
    script.call(secret: VaultSecret.SECRET_NAME.toString(), registry: "other.docker.io"){}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "other.docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(secret: VaultSecret.SECRET_NAME.toString()){}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'docker login -u "%DOCKER_USER%" -p "%DOCKER_PASSWORD%" "docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void testRegistryInWindows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(secret: VaultSecret.SECRET_NAME.toString(), registry: 'other.docker.io'){}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'docker login -u "%DOCKER_USER%" -p "%DOCKER_PASSWORD%" "other.docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_role_secret() throws Exception {
    script.call(secret: VaultSecret.SECRET_NAME.toString(), role_id: "role-id", secret_id: 'secret-id'){}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'role_id=role-id, secret_id=secret-id'))
    assertTrue(assertMethodCallContainsPattern('sh', 'docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "docker.io"'))
    assertJobStatusSuccess()
  }
}
