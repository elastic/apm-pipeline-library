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

import co.elastic.TestUtils
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class DependabotStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/dependabot.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(project: 'elastic/foo', package: 'maven', assign: 'bar')
    printCallStack()
    assertTrue(assertMethodCall('dockerLogin'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Run dependabot'))
    assertTrue(assertMethodCallContainsPattern('sh', "GITHUB_ACCESS_TOKEN=${TestUtils.DEFAULT_VALUE}"))
    assertTrue(assertMethodCallContainsPattern('sh', 'PROJECT_PATH=elastic/foo'))
    assertTrue(assertMethodCallContainsPattern('sh', 'PACKAGE_MANAGER=maven'))
    assertTrue(assertMethodCallContainsPattern('sh', 'PULL_REQUESTS_ASSIGNEE=bar'))
    assertTrue(assertMethodCallContainsPattern('sh', 'docker.elastic.co/observability-ci/dependabot'))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_assign() throws Exception {
    def script = loadScript(scriptName)
    script.call(project: 'elastic/foo', package: 'maven')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'PULL_REQUESTS_ASSIGNEE= '))
    assertJobStatusSuccess()
  }

  @Test
  void test_docker_customisation() throws Exception {
    def script = loadScript(scriptName)
    script.call(project: 'elastic/foo', package: 'maven', secretRegistry: '', registry: '', image: 'bar')
    printCallStack()
    assertFalse(assertMethodCall('dockerLogin'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dependabot: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_missing_package() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(project: 'elastic/foo')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dependabot: package argument is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_missing_project() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(package: 'maven')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'dependabot: project argument is required'))
    assertJobStatusFailure()
  }
}
