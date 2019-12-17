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

class RebuildPipelineStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/rebuildPipeline.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testWithoutParams() throws Exception {
    def script = loadScript(scriptName)
    binding.setVariable('params', null)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "rebuildPipeline: params doesn't exist"))
    assertFalse(assertMethodCallContainsPattern('build', 'job'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithEmptyParams() throws Exception {
    def script = loadScript(scriptName)
    binding.setVariable('params', [:])
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "rebuildPipeline: params doesn't exist"))
    assertFalse(assertMethodCallContainsPattern('build', 'job'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithUnsupportedJob() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_NAME = 'foo'
    binding.setVariable('params', [ a: 'foo' ])
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'unsupported'))
    assertFalse(assertMethodCallContainsPattern('build', 'job'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithSupportedJob() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_NAME = 'apm-agent-python-mbp'
    binding.setVariable('params', [ a: 'foo' ])
    script.call()
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'unsupported'))
    assertTrue(assertMethodCallContainsPattern('build', 'job=apm-agent-python-mbp'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithPrevious2BuildSuccess() throws Exception {
    def script = loadScript(scriptName)
    def previousBuild = [ previousBuild: [ currentResult: 'SUCCESS' ] ]
    binding.getVariable('currentBuild').previousBuild = previousBuild
    env.JOB_NAME = 'apm-agent-python-mbp'
    binding.setVariable('params', [ a: 'foo' ])
    script.call()
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'there are more than 2 previous build failures.'))
    assertTrue(assertMethodCallContainsPattern('build', 'job=apm-agent-python-mbp'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithPrevious2BuildFailure() throws Exception {
    def script = loadScript(scriptName)
    def previousBuild = [ previousBuild: [ currentResult: 'FAILURE' ] ]
    binding.getVariable('currentBuild').previousBuild = previousBuild
    env.JOB_NAME = 'apm-agent-python-mbp'
    binding.setVariable('params', [ a: 'foo' ])
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'there are more than 2 previous build failures.'))
    assertFalse(assertMethodCallContainsPattern('build', 'job=apm-agent-python-mbp'))
    assertJobStatusSuccess()
  }

}
