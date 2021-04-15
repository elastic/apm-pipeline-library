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

class CheckDockerImageStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/checkDockerImage.groovy')
  }

  @Test
  void testLinux_ImageDoesNotExists() throws Exception {
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>/dev/null'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist: pulling'))
    assertJobStatusSuccess()
  }

  @Test
  void testLinux_ImageExists() throws Exception {
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>/dev/null'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker host'))
    assertJobStatusSuccess()
  }

  @Test
  void testLinux_PullError() throws Exception {
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 1 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>/dev/null'))
    assertTrue(assertMethodCallContainsPattern('log', 'Docker pull for hello-world:latest failed'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_ImageDoesNotExists() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>NUL'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest does not exist: pulling'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_ImageExists() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>NUL'))
    assertTrue(assertMethodCallContainsPattern('log', 'hello-world:latest exists in the Docker host'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows_PullError() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 1 })
    def ret = script.call(image: 'hello-world:latest')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'docker images -q hello-world:latest'))
    assertTrue(assertMethodCallContainsPattern('cmd', '2>NUL'))
    assertTrue(assertMethodCallContainsPattern('log', 'Docker pull for hello-world:latest failed'))
    assertJobStatusSuccess()
  }
}
