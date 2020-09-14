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

class IsCommentTriggerStepTests extends ApmBasePipelineTest {

  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.GITHUB_COMMENT_AUTHOR = 'admin'
    env.GITHUB_COMMENT = 'Started by a comment'
    script = loadScript('vars/isCommentTrigger.groovy')
  }

  @Test
  void testMembership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{}') })
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue('admin'.equals(env.GITHUB_COMMENT_AUTHOR))
    assertTrue('Started by a comment'.equals(env.GITHUB_COMMENT))
    assertJobStatusSuccess()
  }

  @Test
  void testNoMembership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return net.sf.json.JSONSerializer.toJSON( """{
        "Code": "404",
        "message": "Not Found",
        "documentation_url": "https://developer.github.com/v3"
      }""")
    })
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testNoMembershipWithError() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testNoCommentTriggered() throws Exception {
    def ret = script.call()
    env.remove('GITHUB_COMMENT_AUTHOR')
    env.remove('GITHUB_COMMENT')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
