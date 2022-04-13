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
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class IsMemberOfOrgStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isMemberOfOrg.groovy')
  }

  @Test
  void test_without_user_parameter() throws Exception {
    testMissingArgument('user') {
      ret = script.call()
    }
  }

  @Test
  void test_active_user() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{}') })
    def ret = script.call(user: 'foo')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'orgs/elastic/members/foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_active_user_with_explicit_org() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{}') })
    def ret = script.call(user: 'foo', org: 'acme')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'orgs/acme/members/foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_membership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return notFound() })
    def ret = script.call(user: 'foo')
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'orgs/elastic/members/foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_membership_with_error() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    def ret = script.call(user: 'foo')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  def notFound() {
    return net.sf.json.JSONSerializer.toJSON( """{
        "Code": "404",
        "message": "Not Found",
        "documentation_url": "https://developer.github.com/v3"
      }""")
  }
}
