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

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class WithGithubNotifyStepTests extends BasePipelineTest {
  String scriptName = "vars/withGithubNotify.groovy"
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    env.BUILD_ID = "4"
    env.BRANCH_NAME = "PR-60"
    env.JOB_NAME = "mbp/${env.BRANCH_NAME}"
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.JOB_URL = "${env.JENKINS_URL}/job/mbp/job/${env.BRANCH_NAME}"
    env.BUILD_URL = "${env.JENKINS_URL}/job/mbp/job/${env.BRANCH_NAME}/${env.BUILD_ID}/"
    env.RUN_DISPLAY_URL = "${env.JENKINS_URL}/job/mbp/job/${env.BRANCH_NAME}/${env.BUILD_ID}/display/redirect"

    binding.setVariable('env', env)

    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })

    helper.registerAllowedMethod('githubNotify', [Map.class], { m ->
      if(m.context.equalsIgnoreCase('failed')){
        updateBuildStatus('FAILURE')
        throw new Exception('Failed')
      }
    })
  }

  @Test
  void testMissingArguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('withGithubNotify: Missing arguments')
    })
    assertJobStatusFailure()
  }

  @Test
  void testMissingDescriptionArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(description: 'foo'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('withGithubNotify: Missing arguments')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWrongTypeArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(context: 'foo', type: 'unknown'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('withGithubNotify: Unsupported type')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithTestLink() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call(context: 'foo', description: 'bar', type: 'test') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "githubNotify"
    }.any { call ->
      callArgsToString(call).contains("${env.BRANCH_NAME}/${env.BUILD_ID}/test")
    })
  }

  @Test
  void testFailure() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    try{
      script.call(context: 'failed') {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertJobStatusFailure()
  }
}
