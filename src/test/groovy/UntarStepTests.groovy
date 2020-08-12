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

class UntarStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/untar.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
  }

  @Test
  void test_with_all_the_parameters() throws Exception {
    def script = loadScript(scriptName)
    script.call(file:'archive.tgz', dir: 'folder', failNever: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('bat', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_an_error_and_with_failNever() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (!m.script.contains('rm')) {
        throw new Exception('Error')
      }
    })
    script.call(file:'archive.tgz', dir: 'folder', failNever: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'rm archive.tgz'))
    assertJobStatusUnstable()
  }

  @Test
  void test_with_an_error_without_failNever() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { throw new Exception('Error') })
    try {
      script.call(file:'archive.tgz', dir: 'folder', failNever: false)
    } catch(err) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm archive.tgz'))
    assertJobStatusFailure()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    script.call(file:'archive.tgz', dir: 'folder')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('sh', 0))
    assertTrue(assertMethodCallContainsPattern('bat', 'del archive.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWithTar() throws Exception {
    def script = loadScript(scriptName)
    script.extractWithTar(file:'archive.tgz', dir: 'folder')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'mkdir -p folder &&'))
    assertTrue(assertMethodCallContainsPattern('sh', 'tar -C folder -xpf archive.tgz'))
    assertTrue(assertMethodCallOccurrences('bat', 0))
    assertFalse(assertMethodCallContainsPattern('withEnv', 'C:\\Windows\\System32'))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWithTar_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    script.extractWithTar(file:'archive.tgz', dir: 'folder')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'C:\\Windows\\System32'))
    assertTrue(assertMethodCallContainsPattern('bat', 'mkdir  folder &&'))
    assertTrue(assertMethodCallContainsPattern('bat', 'tar -C folder -xpf archive.tgz'))
    assertTrue(assertMethodCallOccurrences('sh', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWithTar_without_dir() throws Exception {
    def script = loadScript(scriptName)
    script.extractWithTar(file:'archive.tgz')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-xpf archive.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWith7z_linux() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.extractWith7z(file:'archive.tgz', dir: 'folder')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('sh', 0))
    assertTrue(assertMethodCallContainsPattern('error', '7z is not supported yet'))
    assertJobStatusFailure()
  }

  @Test
  void test_extractWith7z_windows_without_7z_installed() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return false })
    script.extractWith7z(file:'archive.tgz', dir: 'folder')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('installTools', 1))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'C:\\ProgramData\\chocolatey\\bin'))
    assertTrue(assertMethodCallContainsPattern('bat', '7z x -tgzip -so archive.tgz'))
    assertTrue(assertMethodCallContainsPattern('bat', "7z x -si -ttar -ofolder"))
    assertFalse(assertMethodCallContainsPattern('bat', '-xpf'))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWith7z_windows_with_7z_installed() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return true })
    script.extractWith7z(file:'archive.tgz', dir: 'folder')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('installTools', 0))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'C:\\ProgramData\\chocolatey\\bin'))
    assertTrue(assertMethodCallContainsPattern('bat', '7z x -tgzip -so archive.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_extractWith7z_windows_transformation() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    script.extractWith7z(file:'archive.tgz', dir: '.')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('bat', "7z x -si -ttar -o"))
    assertJobStatusSuccess()
  }

  @Test
  void test_tarCommand_in_windows_transformation() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], {false})
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
    def ret = script.tarCommand(file:'archive.tgz', dir: '.')
    printCallStack()
    assertFalse(ret.contains('mkdir'))
    assertFalse(ret.contains('-C'))
    assertJobStatusSuccess()
  }

}
