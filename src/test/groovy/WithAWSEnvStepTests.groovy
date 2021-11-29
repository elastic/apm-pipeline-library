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
    helper.registerAllowedMethod('fileExists', [Map.class], { return false })
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
    assertFalse(assertMethodCallContainsPattern('withCredentials', ''))
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
  void test_without_aws_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    def result = false
    script.call(credentialsId: 'foo') {
      result = true
    }
    printCallStack()
    assertTrue(result)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+AWS'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O awscli.zip'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_aws_installed_by_default_no_wget_no_curl() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    def result = false
    script.call(credentialsId: 'foo') {
      result = true
    }
    printCallStack()
    assertTrue(result)
    assertFalse(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertFalse(assertMethodCallContainsPattern('sh', 'curl'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_aws_installed_by_default_no_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return !(m.tool.equals('wget') || m.tool.equals('aws'))})
    def result = false
    script.call(credentialsId: 'foo') {
      result = true
    }
    printCallStack()
    assertTrue(result)
    assertFalse(assertMethodCallContainsPattern('sh', 'wget -q -O awscli.zip'))
    assertTrue(assertMethodCallContainsPattern('sh', 'curl -sSLo awscli.zip --retry 3 --retry-delay 2 --max-time 10'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
  }

}
