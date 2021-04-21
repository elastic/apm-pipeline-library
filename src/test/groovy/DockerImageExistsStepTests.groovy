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

class DockerImageExistsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script = loadScript('vars/dockerImageExists.groovy')
  }

  @Test
  void testLinux_DockerIsNotInstalled() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    try {
      script.call(image: 'hello-world:latest')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dockerImageExists: Docker is not installed'))
    assertJobStatusFailure()
  }

  @Test
  void testLinux_ImageDoesNotExists() throws Exception {
    helper.registerAllowedMethod('cmd', [Map.class], { m ->
      if (m.script.contains('docker manifest inspect')) {
        return 0
      }
      return 1
    })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist in the Docker host: checking registry'))
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker manifest inspect hello-world:latest >/dev/null'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker registry'))
    assertJobStatusSuccess()
  }

  @Test
  void testLinux_ImageExists() throws Exception {
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker host'))
    assertJobStatusSuccess()
  }

  @Test
  void testLinux_ImageIsNotSet() throws Exception {
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dockerImageExists: image parameter is required'))
    assertJobStatusFailure()
  }

  @Test
  void testLinux_NotExists() throws Exception {
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 1 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist in the Docker host: checking registry'))
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker manifest inspect hello-world:latest >/dev/null'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist at all'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_DockerIsNotInstalled() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    try {
      script.call(image: 'hello-world:latest')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dockerImageExists: Docker is not installed'))
    assertJobStatusFailure()
  }

  @Test
  void testWindows_ImageDoesNotExists_Pull() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('cmd', [Map.class], { m ->
      if (m.script.contains('docker manifest inspect')) {
        return 0
      }
      return 1
    })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist in the Docker host: checking registry'))
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker manifest inspect hello-world:latest >NUL'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker registry'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_ImageExists() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker host'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_ImageIsNotSet() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dockerImageExists: image parameter is required'))
    assertJobStatusFailure()
  }

  @Test
  void testWindows_NotExists() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 1 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker inspect -f "{{.Id}}" hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist in the Docker host: checking registry'))
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker manifest inspect hello-world:latest >NUL'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist at all'))
    assertJobStatusSuccess()
  }
}
