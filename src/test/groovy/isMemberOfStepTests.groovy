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

import hudson.model.Cause
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class IsMemberOfStepTests extends ApmBasePipelineTest {

  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isMemberOf.groovy')
  }

  @Test
  void test_without_user_parameter() throws Exception {
    try {
      script.call()
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'user param is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_team_parameter() throws Exception {
    try {
      script.call(user: 'foo')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'team param is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_active_membership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{ "message": {"state":"active","role":"maintainer","url":"https://api.github.com/organizations/6764390/team/2448411/memberships/foo"} }') })
    def ret = script.call(user: 'foo', team: 'apm-ui')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_pending_membership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{ "message": {"state":"pending","role":"member","url":"https://api.github.com/organizations/6764390/team/2448411/memberships/foo"} }') })
    def ret = script.call(user: 'foo', team: 'apm-ui')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_no_membership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return net.sf.json.JSONSerializer.toJSON( """{
        "Code": "404",
        "message": "Not Found",
        "documentation_url": "https://developer.github.com/v3"
      }""")
    })
    def ret = script.call(user: 'foo', team: 'bar')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_no_membership_with_error() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    def ret = script.call(user: 'foo', team: 'bar')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
