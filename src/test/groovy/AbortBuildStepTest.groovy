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

import co.elastic.mock.RunMock
import co.elastic.mock.RunWrapperMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class AbortBuildStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/abortBuild.groovy')
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
  }

  @Test
  void test_RunningBuild() throws Exception {
    def runBuilding = new RunMock(building: true)
    def build = new RunWrapperMock(rawBuild: runBuilding, number: 1, result: 'FAILURE')
    script.call(build: build, message: "let's stop it")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "let's stop it"))
  }

  @Test
  void test_RunningBuild_default_message() throws Exception {
    def runBuilding = new RunMock(building: true)
    def build = new RunWrapperMock(rawBuild: runBuilding, number: 1, result: 'FAILURE')
    script.call(build: build)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Force to abort the build'))
  }

  @Test
  void test_NotRunningBuild() throws Exception {
    def notRunBuilding = new RunMock(building: false)
    def build = new RunWrapperMock(rawBuild: notRunBuilding, number: 1, result: 'FAILURE')
    script.call(build: build, message: "let's stop it")
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', "let's stop it"))
  }

  @Test
  void test_null_build() throws Exception {
    script.call(build: null)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Build or rawBuild do not have any valid value'))
  }

  @Test
  void test_null_rawbuild() throws Exception {
    def build = new RunWrapperMock(rawBuild: null, number: 1, result: 'FAILURE')
    script.call(build: build)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Build or rawBuild do not have any valid value'))
  }
}
