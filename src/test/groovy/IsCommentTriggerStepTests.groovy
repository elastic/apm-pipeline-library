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
import hudson.model.Cause

class IsCommentTriggerStepTests extends ApmBasePipelineTest {
  class IssueCommentCause extends Cause {
    private final String userLogin
    private final String comment

    public IssueCommentCause(final String userLogin, final String comment) {
      this.userLogin = userLogin
      this.comment = comment
    }

    public String getUserLogin() {
      return userLogin
    }

    public String getComment() {
      return comment
    }

    public String getShortDescription(){
      return String.format("%s commented: %s", userLogin, comment);
    }
  }

  class RawBuild {
    private final Cause cause

    public RawBuild(Cause cause){
      this.cause = cause
    }

    public Cause getCause(String clazz) {
      return cause
    }

    public List<Cause> getCauses(){
      List<Cause> list = new ArrayList()
      list.add(cause)
      return list
    }
  }

  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    Cause cause = new IssueCommentCause('admin', 'Started by a comment')
    binding.getVariable('currentBuild').rawBuild = new RawBuild(cause)
    script = loadScript('vars/isCommentTrigger.groovy')
  }

  @Test
  void testMembership() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], { return net.sf.json.JSONSerializer.toJSON('{}') })
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue('admin'.equals(env.BUILD_CAUSE_USER))
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
  void testNoCommentTriggered() throws Exception {
    binding.getVariable('currentBuild').rawBuild = new RawBuild(null)
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  def buildIssueCommentCause(String key) {
    List<Cause> result = new ArrayList()
    Cause cause = new IssueCommentCause('admin', 'Started by a comment')
    if(key.contains('IssueCommentCause')){
      result.add(cause)
    }
    return result
  }
}
