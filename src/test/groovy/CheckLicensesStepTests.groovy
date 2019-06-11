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

class CheckLicensesStepTests extends BasePipelineTest {
  String scriptName = "vars/checkLicenses.groovy"
  Map env = [:]

  /**
   * Mock Docker class from docker-workflow plugin.
   */
  class Docker implements Serializable {

      public Image image(String id) {
          new Image(this, id)
      }

      public static class Image implements Serializable {
          private Image(Docker docker, String id) {}
          public <V> V inside(String args = '', Closure<V> body) {}
      }
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = '/tmp'
    env.BASE_DIR = '/base'

    binding.setVariable('env', env)
    binding.setProperty('docker', new Docker())

    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod('sh', [Map.class], { m -> println m.script })
  }

  @Test
  void testSuccess() throws Exception {
    def script = loadScript(scriptName)
    script.call(ext: '.foo')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testMissingExtArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('checkLicenses: Missing ext param.')
    })
    assertJobStatusFailure()
  }
}
