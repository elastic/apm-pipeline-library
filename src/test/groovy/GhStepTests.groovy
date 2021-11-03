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

class GhStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/gh.groovy')
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
  }

  @Test
  void test_without_args() throws Exception {
    testMissingArgument('command') {
      script.call()
    }
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(command: 'issue list', flags: [ label: 'foo'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('cmd', "gh issue list --label='foo'"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertFalse(assertMethodCallContainsPattern('sh', "wget -q -O"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags() throws Exception {
    script.call(command: 'issue list', flags: [ label: 'foo'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label='foo'"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertFalse(assertMethodCallContainsPattern('sh', "wget -q -O"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags_with_list() throws Exception {
    script.call(command: 'issue list', flags: [ label: ['foo', 'bar'] ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label='foo' --label='bar'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.label.startsWith('gh')) { throw new Exception('unknown command "foo" for "gh issue"') }})
    def result
    try {
      result = script.call(command: 'issue foo')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'returnStdout=true'))
    assertNull(result)
  }

  @Test
  void test_without_gh_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'issue list')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertTrue(assertMethodCallContainsPattern('sh', 'linux_amd64.tar.gz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_gh_installed_by_default_no_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    script.call(command: 'issue list')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags_and_spaces() throws Exception {
    script.call(command: 'issue list', flags: [ label: "bug,help wanted"])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label='bug,help wanted'"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertFalse(assertMethodCallContainsPattern('sh', "wget -q -O"))
    assertJobStatusSuccess()
  }

  @Test
  void test_outside_of_a_repo_without_variables() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m?.returnStatus) { return 1 }})
    script.call(command: 'issue list')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', '--repo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_outside_of_a_repo_with_variables() throws Exception {
    env.REPO_NAME = 'foo'
    env.ORG_NAME = 'org'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m?.returnStatus) { return 1 }})
    try {
    script.call(command: 'issue list')
    } catch(err) { println err}
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "--repo='org/foo'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_within_a_repo() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { return 0 })
    script.call(command: 'issue list')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', '--repo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'issue list')
    script.call(command: 'issue list')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertJobStatusSuccess()
  }

  @Test
  void test_cache_without_gh_installed_by_default_with_wget() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'issue list')
    script.call(command: 'issue list')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+GH'))
    assertTrue(assertMethodCallContainsPattern('sh', 'wget -q -O'))
    assertTrue(assertMethodCallContainsPattern('log', 'gh: get the ghLocation from cache.'))
    assertTrue(assertMethodCallContainsPattern('log', 'gh: set the ghLocation.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_normalisation() throws Exception {
    script.call(command: 'issue list', flags: [ label: "foo-'" ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label='foo-\"'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_spaces() throws Exception {
    script.call(command: 'pr list', flags: [ search: '"[automation] foo bar" in:title'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', """--search='"[automation] foo bar" in:title'"""))
  }

  @Test
  void test_with_multiple_flag_entries() throws Exception {
    script.call(command: 'workflow run build_and_test.yml', flags: [field: ['id=1', 'runner=ubuntu']])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "gh workflow run build_and_test.yml --field='id=1' --field='runner=ubuntu'"))
  }

  @Test
  void test_with_version() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'issue list', version: "2.0.0")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
  }

  @Test
  void test_with_force_installation() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    script.call(command: 'issue list', version: "2.0.0", forceInstallation: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '2.0.0'))
    assertJobStatusSuccess()
  }

  @Test
  void test_in_darwin() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    helper.registerAllowedMethod('nodeOS', [], { return 'darwin' })
    script.call(command: 'issue list')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'macOS_amd64.tar.gz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_in_arm() throws Exception {
    helper.registerAllowedMethod('isArm', [], { return true })
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
    helper.registerAllowedMethod('nodeOS', [], { return 'linux' })
    script.call(command: 'issue list')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'linux_arm64.tar.gz'))
    assertJobStatusSuccess()
  }
}
