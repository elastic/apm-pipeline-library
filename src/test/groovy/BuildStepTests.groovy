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
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

public class BuildStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/build.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def result = script.call(job: 'foo')
    printCallStack()
    assertTrue(result != null)
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("${env.JENKINS_URL}job/foo/1/display/redirect")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testNestedJob() throws Exception {
    def script = loadScript(scriptName)
    def result = script.call(job: 'nested/foo')
    printCallStack()
    assertTrue(result != null)
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'log'
    }.any { call ->
      callArgsToString(call).contains("${env.JENKINS_URL}job/nested/job/foo/1/display/redirect")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testException() throws Exception {
    def script = loadScript(scriptName)
    def result = script.getRedirectLink(new Exception('nested » foo #1'), 'nested/foo')
    assertTrue(result.contains("${env.JENKINS_URL}job/nested/job/foo/1/display/redirect"))
  }

  @Test
  void testAnotherObject() throws Exception {
    def script = loadScript(scriptName)
    def result = script.getRedirectLink('AnotherObject', 'foo')
    assertTrue(result.contains("Can not determine redirect link"))
  }
}
