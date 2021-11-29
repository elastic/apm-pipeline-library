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

import net.sf.json.JSONNull
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class IsUpstreamTriggerStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isUpstreamTrigger.groovy')
  }

  @Test
  void test_with_upstream_cause() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UpstreamCause',
          shortDescription: 'Started by upstream project "apm-integration-tests/PR-695" build number 5',
          upstreamBuild: 5,
          upstreamProject: 'apm-integration-tests/PR-695',
          upstreamUrl: 'job/apm-integration-tests/job/PR-695/'
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('log', "isUpstreamTrigger: apm-integration-tests/PR-695, filter: 'all'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_upstream_cause_and_filter() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UpstreamCause',
          shortDescription: 'Started by upstream project "apm-integration-tests/PR-695" build number 5',
          upstreamBuild: 5,
          upstreamProject: 'apm-integration-tests/PR-695',
          upstreamUrl: 'job/apm-integration-tests/job/PR-695/'
        ]
      ]
    }

    def ret = script.call(filter: 'PR-')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('log', "isUpstreamTrigger: apm-integration-tests/PR-695, filter: 'PR-'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_upstream_cause_and_lowercase_filter() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UpstreamCause',
          shortDescription: 'Started by upstream project "apm-integration-tests/PR-695" build number 5',
          upstreamBuild: 5,
          upstreamProject: 'apm-integration-tests/PR-695',
          upstreamUrl: 'job/apm-integration-tests/job/PR-695/'
        ]
      ]
    }

    def ret = script.call(filter: 'pr-')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('log', "isUpstreamTrigger: apm-integration-tests/PR-695, filter: 'pr-'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_trigger_cause() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.triggers.TimerTrigger$TimerTriggerCause',
          shortDescription: 'Started by a timmer',
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_trigger_cause_and_filter() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.triggers.TimerTrigger$TimerTriggerCause',
          shortDescription: 'Started by a timmer',
        ]
      ]
    }

    def ret = script.call(filter: 'PR-')
    printCallStack()
    assertFalse(ret)
    assertFalse(assertMethodCallContainsPattern('log', "filter: 'PR-'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_upstream_cause_without_upstreamProject() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UpstreamCause',
          shortDescription: 'Started by upstream project "apm-integration-tests/PR-695" build number 5',
          upstreamBuild: 5
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_upstream_cause_and_filter_without_upstreamProject() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UpstreamCause',
          shortDescription: 'Started by upstream project "apm-integration-tests/PR-695" build number 5',
          upstreamBuild: 5
        ]
      ]
    }

    def ret = script.call(filter: 'PR-')
    printCallStack()
    assertFalse(ret)
    assertFalse(assertMethodCallContainsPattern('log', "isUpstreamTrigger: apm-integration-tests/PR-695, filter: 'PR-'"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_build_upstream_cause() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamCause',
          shortDescription: 'Started by upstream project "Beats/beats/PR-28919" build number 2',
          upstreamBuild: 2,
          upstreamProject: 'Beats/beats/PR-28919',
          upstreamUrl: 'job/Beats/job/beats/job/PR-28919/'
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('log', "isUpstreamTrigger: Beats/beats/PR-28919, filter: 'all'"))
    assertJobStatusSuccess()
  }
}
