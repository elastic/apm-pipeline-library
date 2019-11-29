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

class PipelineManagerStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/pipelineManager.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testEmpty() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testUnknown() throws Exception {
    def script = loadScript(scriptName)
    script.call(unknown: [:])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testFirstTime() throws Exception {
    def script = loadScript(scriptName)
    script.call(firstTimeContributor: [ when: 'ALWAYS' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'firstTimeContributor step is not available yet.'))
    assertJobStatusSuccess()
  }

  @Test
  void testCancelPreviousRunningBuilds() throws Exception {
    def script = loadScript(scriptName)
    script.call(cancelPreviousRunningBuilds: [ when: 'TAG' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'cancelPreviousRunningBuilds step is not enabled'))
    assertJobStatusSuccess()
  }

  @Test
  void testDefaultAndEmptyWhen() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.isWhen('unknwon'))
    assertFalse(script.isWhen(''))
    assertFalse(script.isWhen(null))
  }

  @Test
  void testWhenAlways() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.isWhen('ALWAYS'))
  }

  @Test
  void testWhenBranch() throws Exception {
    def script = loadScript(scriptName)
    assertTrue(script.isWhen('BRANCH'))
    env.CHANGE_ID = ''
    assertTrue(script.isWhen('BRANCH'))
    env.CHANGE_ID = 'PR-1'
    assertFalse(script.isWhen('BRANCH'))
  }

  @Test
  void testWhenPr() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.isWhen('PR'))
    env.CHANGE_ID = ''
    assertFalse(script.isWhen('PR'))
    env.CHANGE_ID = 'PR-1'
    assertTrue(script.isWhen('PR'))
  }

  @Test
  void testWhenTag() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.isWhen('TAG'))
    env.TAG_NAME = ''
    assertFalse(script.isWhen('TAG'))
    env.TAG_NAME = 'v1.0'
    assertTrue(script.isWhen('TAG'))
  }
}
