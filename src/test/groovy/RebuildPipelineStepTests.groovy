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
  void testWithUnsupportedJob() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_NAME = 'foo'
    script.call()
    assertTrue(assertMethodCallContainsPattern('log', 'unsupported'))
    assertFalse(assertMethodCallContainsPattern('build', 'job'))
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testWithSupportedJob() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_NAME = 'apm-agent-python-mbp'
    script.call()
    assertFalse(assertMethodCallContainsPattern('log', 'unsupported'))
    assertTrue(assertMethodCallContainsPattern('build', 'job=apm-agent-python-mbp'))
    printCallStack()
    assertJobStatusSuccess()
  }
}
