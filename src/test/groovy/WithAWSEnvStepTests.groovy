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

class WithAWSEnvStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script = loadScript('vars/withAWSEnv.groovy')
  }

  @Test
  void test_missing_secret() throws Exception {
    testMissingArgument('secret') {
      script.call() {
        // NOOP
      }
    }
  }

  @Test
  void test_with_secret() throws Exception {
    def ret = false
    try {
    script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString()) {
      ret = true
    }
    } catch (e) {
      println e
    }
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('cmd', 'aws configure import --csv file://'))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'AWS_PROFILE'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m -> throw new Exception('force a failure') })
    def result = false
    try {
      script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString()) {
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
  void test_with_force_installation_and_version_already_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    def ret = false
    script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString(), version: "2.0.0", forceInstallation: true) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_force_installation_and_version_not_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    def ret = false
    script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString(), version: "2.0.0", forceInstallation: true) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_without_force_installation_and_version_not_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') || !m.tool.equals('aws') })
    def ret = false
    script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString(), version: "2.0.0", forceInstallation: false) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_vault_arguments() throws Exception {
    def ret = false
    try {
    script.call(secret: VaultSecret.SECRET_AWS_PROVISIONER.toString(), role_id: 'my-role', secret_id: 'my-secret') {
      ret = true
    }
    } catch (e) {
      println e
    }
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', "secret=${VaultSecret.SECRET_AWS_PROVISIONER.toString()}, role_id=my-role, secret_id=my-secret"))
    assertJobStatusSuccess()
  }

  @Test
  void test_awsURL_in_arm() throws Exception {
    helper.registerAllowedMethod('isArm', [], { true })
    def ret = script.awsURL('2.2.2')
    printCallStack()
    assertTrue(ret.contains("linux-aarch64-2.2.2.zip"))
  }

  @Test
  void test_awsURL_in_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.awsURL('2.2.2')
    } catch(err) {
      println err
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'windows is not supported yet'))
  }

  @Test
  void test_awsURL_in_32_bits() throws Exception {
    helper.registerAllowedMethod('is64', [], { return false })
    def ret = script.awsURL('2.2.2')
    printCallStack()
    assertTrue(ret.contains("linux-x86-2.2.2.zip"))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call() {
        // NOOP
      }
    }
  }
}
