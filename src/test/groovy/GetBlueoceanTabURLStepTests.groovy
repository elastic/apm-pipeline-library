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

class GetBlueoceanTabURLStepTests extends BasePipelineTest {
  String scriptName = "vars/getBlueoceanTabURL.groovy"
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.BUILD_ID = "4"
    env.BRANCH_NAME = "PR-60"
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.BUILD_URL = "${env.JENKINS_URL}/job/folder/job/mpb/job/${env.BRANCH_NAME}/${env.BUILD_ID}/"

    binding.setVariable('env', env)

    def redirectURL = "${env.JENKINS_URL}/blue/organizations/jenkins/folder%2Fmbp/detail/${env.BRANCH_NAME}/${env.BUILD_ID}/"

    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("getBlueoceanDisplayURL", [], { redirectURL })
  }

  @Test
  void testMissingArguments() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('getBlueoceanTabURL: Unsupported type')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWrongTypeArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call('unknown')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == "error"
    }.any { call ->
      callArgsToString(call).contains('getBlueoceanTabURL: Unsupported type')
    })
    assertJobStatusFailure()
  }

  @Test
  void testSuccessWithTestsTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('tests')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/tests"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithChangesTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('changes')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/changes"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithPipelineTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('pipeline')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/pipeline"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithArtifactsTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('artifacts')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/artifacts"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithCoberturaTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('cobertura')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/cobertura"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithStorageTab() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call('gcs')
    printCallStack()
    assertTrue(ret.contains("${env.BRANCH_NAME}/${env.BUILD_ID}/gcsObjects"))
    assertJobStatusSuccess()
  }
}
