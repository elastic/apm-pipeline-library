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

class GetBlueoceanDisplayURLStepTests extends BasePipelineTest {
  String scriptName = "vars/getBlueoceanDisplayURL.groovy"
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.BUILD_ID = "4"
    env.BRANCH_NAME = "PR-60"
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.RUN_DISPLAY_URL = "${env.JENKINS_URL}/job/folder/job/mbp/job/${env.BRANCH_NAME}/${env.BUILD_ID}/display/redirect"

    binding.setVariable('env', env)

    def redirectURL = "${env.JENKINS_URL}/blue/organizations/jenkins/folder%2Fmbp/detail/${env.BRANCH_NAME}/${env.BUILD_ID}/"

    helper.registerAllowedMethod("sh", [Map.class], { redirectURL })
    helper.registerAllowedMethod("powershell", [Map.class], { redirectURL })
    helper.registerAllowedMethod("isUnix", [], { "OK" })
  }

  @Test
  void testSuccessLinux() throws Exception {
    def script = loadScript(scriptName)
    def url = script.call()
    printCallStack()
    assertTrue(url.contains("${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWindows() throws Exception {
    helper.registerAllowedMethod("isUnix", [], { false })
    def script = loadScript(scriptName)
    def url = script.call()
    printCallStack()
    assertTrue(url.contains("${env.BRANCH_NAME}/${env.BUILD_ID}"))
    assertJobStatusSuccess()
  }
}
