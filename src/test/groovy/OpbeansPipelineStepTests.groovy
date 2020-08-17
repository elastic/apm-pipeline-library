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
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class OpbeansPipelineStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/opbeansPipeline.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    binding.setProperty('BASE_DIR', '/')
    binding.setProperty('DOCKERHUB_SECRET', 'secret')
    env.GIT_BASE_COMMIT = '1'
    env.REPO_NAME = 'opbeans-xyz'
    super.setUp()
  }

  @Test
  void test_when_master_branch() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('stage', 'Build'))
    assertTrue(assertMethodCallContainsPattern('stage', 'Test'))
    assertTrue(assertMethodCallContainsPattern('stage', 'Release'))
    assertTrue(assertMethodCallContainsPattern('build', 'job=apm-integration-tests-selector-mbp/master'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_master_branch_and_empty_downstreamJobs() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.call(downstreamJobs: [])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('build', 'job=apm-integration-tests-selector-mbp/master'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_master_branch_and_downstreamJobs() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.call(downstreamJobs: [ 'folder/foo', 'folder/bar'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('stage', 'Downstream'))
    assertTrue(assertMethodCallContainsPattern('build', 'folder/foo'))
    assertTrue(assertMethodCallContainsPattern('sh', 'VERSION=latest make publish'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_no_release() throws Exception {
    def script = loadScript(scriptName)
    // When the branch doesn't match
    env.BRANCH_NAME = 'foo'
    script.call()
    printCallStack()
    // Then no publish shell step
    assertFalse(assertMethodCallContainsPattern('sh', 'make publish'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_tag_release() throws Exception {
    def script = loadScript(scriptName)
    // When the tag release does match
    env.BRANCH_NAME = 'v1.0'
    script.call()
    printCallStack()
    // Then publish shell step
    assertTrue(assertMethodCallContainsPattern('sh', 'VERSION=agent-v1.0 make publish'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_tag_release_for_opbeans_frontend() throws Exception {
    def script = loadScript(scriptName)
    // When the tag release does match
    env.BRANCH_NAME = '@elastic/apm-rum@1.0'
    script.call()
    printCallStack()
    // Then publish shell step
    assertTrue(assertMethodCallContainsPattern('sh', 'VERSION=agent-1.0 make publish'))
    assertJobStatusSuccess()
  }

  @Test
  void test_getForkedRepoOrElasticRepo() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_FORK = 'user/forked_repo'
    def result = script.getForkedRepoOrElasticRepo('foo')
    assertEquals(result, 'user/forked_repo')
    assertJobStatusSuccess()
  }

  @Test
  void test_getForkedRepoOrElasticRepo_without_change_fork() throws Exception {
    def script = loadScript(scriptName)
    def result = script.getForkedRepoOrElasticRepo('repo')
    assertEquals(result, 'elastic/repo')
    assertJobStatusSuccess()
  }

  @Test
  void test_getForkedRepoOrElasticRepo_with_change_fork() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_FORK = 'user'
    def result = script.getForkedRepoOrElasticRepo('repo')
    assertEquals(result, 'user/repo')
    assertJobStatusSuccess()
  }

  @Test
  void test_generateBuildOpts_without_known_repo() throws Exception {
    def script = loadScript(scriptName)
    def result = script.generateBuildOpts('unknown', '')
    assertEquals(result, '')
    assertJobStatusSuccess()
  }

  @Test
  void test_generateBuildOpts_with_go() throws Exception {
    def script = loadScript(scriptName)
    def result = script.generateBuildOpts('opbeans-go', '')
    assertEquals(result, '--with-opbeans-go --opbeans-go-branch 1 --opbeans-go-repo elastic/opbeans-go')
    assertJobStatusSuccess()
  }

  @Test
  void test_generateBuildOpts_with_go_and_forked_repo() throws Exception {
    def script = loadScript(scriptName)
    env.CHANGE_FORK = 'user'
    def result = script.generateBuildOpts('opbeans-go', '')
    assertEquals(result, '--with-opbeans-go --opbeans-go-branch 1 --opbeans-go-repo user/opbeans-go')
    assertJobStatusSuccess()
  }

  @Test
  void test_generateBuildOpts_with_java() throws Exception {
    def script = loadScript(scriptName)
    def result = script.generateBuildOpts('opbeans-java', 'foo')
    assertEquals(result, '--with-opbeans-java --opbeans-java-image foo --opbeans-java-version 1')
    assertJobStatusSuccess()
  }

  @Test
  void test_waitIfNotPR() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.waitIfNotPR())
    env.CHANGE_ID = 'PR-1'
    assertFalse(script.waitIfNotPR())
    assertJobStatusSuccess()
  }
}
