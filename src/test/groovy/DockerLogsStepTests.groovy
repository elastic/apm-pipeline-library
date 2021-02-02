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

class DockerLogsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/dockerLogs.groovy')
  }

  @Test
  void test() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/docker-logs.sh'))
    assertTrue(assertMethodCallContainsPattern('sh', 'docker-logs.sh "" "docker-compose.yml" || true'))
    assertTrue(assertMethodCallContainsPattern('sh', 'chmod 755'))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'docker-info/**'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_fail_never_false() throws Exception {
    script.call(failNever: false)
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'docker-logs.sh "" "docker-compose.yml" || true'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_parameters() throws Exception {
    script.call(step: 'foo', dockerCompose: 'bar/docker-compose.yml', failNever: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'docker-logs.sh "foo" "bar/docker-compose.yml" || true'))
    assertJobStatusSuccess()
  }

  @Test
  void test_failure_with_failNever_false() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception('Failed')
    })
    try {
      script.call(failNever: false)
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('archiveArtifacts', 'docker-info/**'))
    assertJobStatusFailure()
  }

  @Test
  void test_normalise_step() throws Exception {
    assertTrue(script.normalise('foo').equals('foo'))
    assertTrue(script.normalise('foo;bar').equals('foo/bar'))
    assertTrue(script.normalise('--f.o o--').equals('_f_o_o_'))
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }
}
