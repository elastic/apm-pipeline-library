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

class WithTerraformEnvStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    helper.registerAllowedMethod('fileExists', [Map.class], { return false })
    helper.registerAllowedMethod('nodeOS', { 'linux' })
    script = loadScript('vars/withTerraformEnv.groovy')
  }

  @Test
  void test() throws Exception {
    def ret = false
    script.call() {
      ret = true
    }
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallOccurrences('download', 0))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+TERRAFORM'))
    assertJobStatusSuccess()
  }


  @Test
  void test_with_force_installation_and_version_already_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    def ret = false
    script.call(version: "2.0.0", forceInstallation: true) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('download', '2.0.0'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_force_installation_and_version_not_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    def ret = false
    script.call(version: "2.0.0", forceInstallation: true) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('download', '2.0.0'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_without_force_installation_and_version_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    def ret = false
    script.call(version: "2.0.0", forceInstallation: false) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('download', 0))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> throw new Exception('force a failure') })
    def result = false
    try {
      script.call() {
        result = true
      }
    } catch(err) {
      println err
      // NOOP
    }
    printCallStack()
    assertFalse(result)
  }

  @Test
  void test_without_terraform_installed_by_default() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    def result = false
    script.call() {
      result = true
    }
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+TERRAFORM'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(assertMethodCallContainsPattern('sh', 'terraform_1.1.9_linux'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('nodeOS', { 'windows' })
    def result = false
    script.call() {
      result = true
    }
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallOccurrences('download', 0))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+TERRAFORM'))
    assertFalse(assertMethodCallContainsPattern('download', "terraform_1.1.9_windows"))
    assertJobStatusSuccess()
  }

  @Test
  void test_terraformURL_in_windows() throws Exception {
    helper.registerAllowedMethod('nodeOS', { 'windows' })
    def ret = script.terraformURL('1.1.9')
    printCallStack()
    println ret
    assertTrue(ret.contains("terraform_1.1.9_windows_amd64.zip"))
    assertJobStatusSuccess()
  }

  @Test
  void test_terraformURL_in_32_bits() throws Exception {
    helper.registerAllowedMethod('is64', [], { return false })
    def ret = script.terraformURL('1.1.9')
    printCallStack()
    assertTrue(ret.contains("terraform_1.1.9_linux_386.zip"))
    assertJobStatusSuccess()
  }
}
