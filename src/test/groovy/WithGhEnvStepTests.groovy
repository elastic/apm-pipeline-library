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

class WithGhEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withGhEnv.groovy')
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    def result = false
    script.call() {
      result = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallContainsPattern('powershell', "choco"))
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    def result = false
    script.call() {
      result = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.label.startsWith('gh')) { throw new Exception('unknown command "foo" for "gh issue"') }})
    def result = false
    try {
      script.call() {
        sh(label: 'gh', script: 'foo')
        result = true
      }
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertFalse(result)
  }

  @Test
  void test_cache() throws Exception {
    def ret = false
    script.call() {
      ret = true
    }
    script.call() {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallContainsPattern('download', ''))
    assertTrue(assertMethodCallOccurrences('pwd', 1))
    assertTrue(assertMethodCallContainsPattern('log', 'withGhEnv: get the ghLocation from cache.'))
    assertTrue(assertMethodCallContainsPattern('log', 'withGhEnv: set the ghLocation.'))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_version() throws Exception {
    def ret = false
    script.call(version: "2.0.0") {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('isInstalled', 'version=2.0.0'))
    assertTrue(ret)
  }

  @Test
  void test_with_force_installation_and_version_already_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    def ret = false
    script.call(version: "2.0.0", forceInstallation: true) {
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
    script.call(version: "2.0.0", forceInstallation: true) {
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
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') || !m.tool.equals('gh') })
    def ret = false
    script.call(version: "2.0.0", forceInstallation: false) {
      ret = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
    assertTrue(assertMethodCallOccurrences('download', 1))
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_in_darwin() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { return 'darwin' })
    script.downloadInstaller("/var", '1.1.0')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('download', 'gh_1.1.0_macOS_amd64.tar.gz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_in_arm() throws Exception {
    helper.registerAllowedMethod('isArm', [], { return true })
    helper.registerAllowedMethod('nodeOS', [], { return 'linux' })
    script.downloadInstaller("/var", '1.1.0')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('download', 'gh_1.1.0_linux_arm64.tar.gz'))
    assertJobStatusSuccess()
  }
}
