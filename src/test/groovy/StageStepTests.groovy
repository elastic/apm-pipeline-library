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

class StageStepTests extends BasePipelineTest {
  String scriptName = "vars/stage.groovy"
  Map env = [:]

  class Steps {
    def call(String name, Closure body) {
      body()
    }
  }

  void stage(String name, Closure body) {
    body()
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    env.JOB_NAME = "testJob"
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.JOB_URL = "${env.JENKINS_URL}/job/${env.JOB_NAME}"

    binding.setVariable('env', env)
    binding.setVariable('steps', new Steps())

    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })

    helper.registerAllowedMethod("stage", [String.class, Closure.class], { body -> body() })
    helper.registerAllowedMethod("stage", [String.class, String.class, Closure.class], { body -> body() })
    helper.registerAllowedMethod('githubNotify', [Map.class], { m ->
      if(m?.context.equals('failed')){
        updateBuildStatus('FAILURE')
        throw new Exception('Failed')
      } else {
        "OK"
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
    assertJobStatusFailure()
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    script.call('name', 'foo') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testFailure() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    try{
      script.call('name', 'failed') {
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
