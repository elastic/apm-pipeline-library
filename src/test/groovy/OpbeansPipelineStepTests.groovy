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

class OpbeansPipelineStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/opbeansPipeline.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    binding.setProperty('BASE_DIR', '/')
    binding.setProperty('DOCKERHUB_SECRET', 'secret')
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
    assertNull(assertMethodCall('build'))
    assertJobStatusSuccess()
  }

  @Test
  void test_when_master_branch_and_empty_downstreamJobs() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.call(downstreamJobs: [])
    printCallStack()
    assertNull(assertMethodCall('build'))
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

}
