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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GsutilStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script = loadScript('vars/gsutil.groovy')
  }

  @Test
  void test_without_command() throws Exception {
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gsutil: command argument is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_credentials() throws Exception {
    try {
      script.call(command: 'cp')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gsutil: credentialsId argument is required.'))
    assertJobStatusFailure()
  }

  @Test
  void test_command() throws Exception {
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', ''))
    assertTrue(assertMethodCallContainsPattern('sh', "gsutil cp"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertFalse(assertMethodCallContainsPattern('sh', "wget -q -O"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.label.startsWith('gsutil')) { throw new Exception('unknown command "foo" for "gsutil"') }})
    def result
    try {
      result = script.call(command: 'foo', credentialsId: 'foo')
    } catch(err) {
      println err
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'returnStdout=true'))
    assertNull(result)
  }

  @Test
  void test_without_gh_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O gsutil.tar.gz https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-319.0.0-linux-x86_64.tar.gz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_gh_installed_by_default_no_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    try {
      script.call(command: 'cp', credentialsId: 'foo')
      script.call(command: 'cp', credentialsId: 'foo')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache_without_gsutil_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'cp', credentialsId: 'foo')
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertTrue(assertMethodCallContainsPattern('log', 'gsutil: get the gsutilLocation from cache.'))
    assertTrue(assertMethodCallContainsPattern('log', 'gsutil: set the gsutilLocation.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', ''))
    assertTrue(assertMethodCallContainsPattern('bat', "gsutil cp"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertFalse(assertMethodCallContainsPattern('bat', "wget -q -O"))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_gh_installed_by_default_with_wget_in_windows() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GSUTIL'))
    assertTrue(assertMethodCallContainsPattern('bat', 'wget -q -O gsutil.zip https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-319.0.0-windows-x86_64.zip'))
    assertJobStatusSuccess()
  }
}
