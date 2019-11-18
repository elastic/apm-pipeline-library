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

class GetBlueoceanDisplayURLStepTests extends ApmBasePipelineTest {
  String scriptName = "vars/getBlueoceanDisplayURL.groovy"

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def redirectURL = "${env.JENKINS_URL}blue/organizations/jenkins/folder%2Fmbp%2F${env.JOB_BASE_NAME}/detail/${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}/"
    def url = script.call()
    printCallStack()
    assertTrue(url.matches(redirectURL))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithADifferentJenkinsURL() throws Exception {
    def script = loadScript(scriptName)
    env.JENKINS_URL = "http://jenkins/"
    def redirectURL = "${env.JENKINS_URL}blue/organizations/jenkins/folder%2Fmbp%2F${env.JOB_BASE_NAME}/detail/${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}/"
    def url = script.call()
    printCallStack()
    assertTrue(url.matches(redirectURL))
    assertJobStatusSuccess()
  }

  @Test
  void testWrongURL() throws Exception {
    def script = loadScript(scriptName)
    def url = script.call()
    printCallStack()
    assertFalse(url.contains("${env.JOB_BASE_NAME}/wrong"))
    assertJobStatusSuccess()
  }
}
