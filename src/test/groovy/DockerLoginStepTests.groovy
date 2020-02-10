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

class DockerLoginStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/dockerLogin.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void testRegistry() throws Exception {
    def script = loadScript(scriptName)
    script.call(secret: VaultSecret.SECRET_NAME.toString(), registry: "other.docker.io")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'docker login -u "${DOCKER_USER}" -p "${DOCKER_PASSWORD}" "other.docker.io"'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(secret: VaultSecret.SECRET_NAME.toString())
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'docker login -u %DOCKER_USER% -p %DOCKER_PASSWORD% docker.io'))
    assertJobStatusSuccess()
  }

  @Test
  void testRegistryInWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(secret: VaultSecret.SECRET_NAME.toString(), registry: 'other.docker.io')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'docker login -u %DOCKER_USER% -p %DOCKER_PASSWORD% other.docker.io'))
    assertJobStatusSuccess()
  }
}
