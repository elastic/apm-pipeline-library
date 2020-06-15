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

class TarStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/tar.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('WORKSPACE', 'WS')
  }

  @Test
  void test_with_all_the_parameters() throws Exception {
    def script = loadScript(scriptName)
    script.call(file:'archive.tgz', dir: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=archive.tgz'))
    assertTrue(assertMethodCallContainsPattern('sh', 'tar --exclude=archive.tgz -czf archive.tgz folder'))
    assertTrue(assertMethodCallOccurrences('bat', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_an_error_and_without_allowMissing() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertJobStatusUnstable()
  }

  @Test
  void test_with_allowMissing() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [String.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', allowMissing: true, archive: false)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void test_with_an_error_without_failNever() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { throw new Exception('Error') })
    try {
      script.call(file:'archive.tgz', dir: 'folder', failNever: false, archive: true)
    } catch(err) {
      //NOOP
      println err
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
    assertJobStatusFailure()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod("isUnix", [], {false})
    script.call(file:'archive.tgz', dir: 'folder', allowMissing: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'C:\\Windows\\System32'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=archive.tgz'))
    assertTrue(assertMethodCallContainsPattern('bat', 'tar --exclude=archive.tgz -czf archive.tgz folder'))
    assertTrue(assertMethodCallOccurrences('sh', 0))
    assertJobStatusSuccess()
  }
}
