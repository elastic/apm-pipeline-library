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

class IsUserTriggerStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isUserTrigger.groovy')
  }

  @Test
  void test() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UserIdCause',
          shortDescription: 'Started by user admin',
          userId: 'admin',
          userName: 'admin'
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue('admin'.equals(env.BUILD_CAUSE_USER))
    assertJobStatusSuccess()
  }

  @Test
  void testFalse() throws Exception {
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
  void testWithUserIdJSONNull() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UserIdCause',
          shortDescription: 'Started by user admin',
          userId: JSONNull.getInstance(),
          userName: 'admin'
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testWithAnonymousUserId() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UserIdCause',
          shortDescription: 'Started by anonymous user',
          userName: 'anonymous'
        ]
      ]
    }

    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
