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
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/pipelineManager.groovy')
  }

  @Test
  void testEmpty() throws Exception {
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testUnknown() throws Exception {
    script.call(unknown: [:])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testFirstTime() throws Exception {
    script.call(firstTimeContributor: [ when: 'ALWAYS' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'firstTimeContributor step is not available yet.'))
    assertJobStatusSuccess()
  }

  @Test
  void testCancelPreviousRunningBuilds() throws Exception {
    script.call(cancelPreviousRunningBuilds: [ when: 'TAG' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'cancelPreviousRunningBuilds step is not enabled'))
    assertJobStatusSuccess()
  }

  @Test
  void testCancelPreviousRunningBuildsWhenAlways() throws Exception {
    script.call(cancelPreviousRunningBuilds: [ when: 'ALWAYS' ])
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testApmTracesWhenAlways() throws Exception {
    helper.registerAllowedMethod('apmCli', [Map.class], {'OK'})
    script.call(apmTraces: [ when: 'ALWAYS' ])
    assertTrue(assertMethodCallContainsPattern('log', 'apmTraces is enabled.'))
    assertTrue(env.APM_CLI_SERVICE_NAME == env.JOB_NAME)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testDefaultAndEmptyWhen() throws Exception {
    assertFalse(script.isEnabled('unknwon'))
    assertFalse(script.isEnabled(''))
    assertFalse(script.isEnabled(null))
  }

  @Test
  void testWhenAlways() throws Exception {
    assertTrue(script.isEnabled('ALWAYS'))
  }

  @Test
  void testWhenBranch() throws Exception {
    assertTrue(script.isEnabled('BRANCH'))
    env.CHANGE_ID = ''
    assertTrue(script.isEnabled('BRANCH'))
    env.CHANGE_ID = 'PR-1'
    assertFalse(script.isEnabled('BRANCH'))
  }

  @Test
  void testWhenPr() throws Exception {
    assertFalse(script.isEnabled('PR'))
    env.CHANGE_ID = ''
    assertFalse(script.isEnabled('PR'))
    env.CHANGE_ID = 'PR-1'
    assertTrue(script.isEnabled('PR'))
  }

  @Test
  void testWhenTag() throws Exception {
    assertFalse(script.isEnabled('TAG'))
    env.TAG_NAME = ''
    assertFalse(script.isEnabled('TAG'))
    env.TAG_NAME = 'v1.0'
    assertTrue(script.isEnabled('TAG'))
  }
}
