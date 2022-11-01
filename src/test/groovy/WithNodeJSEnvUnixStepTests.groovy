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

class WithNodeJSEnvUnixStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withNodeJSEnvUnix.groovy')
    env.PATH = '/foo/bin'
    helper.registerAllowedMethod('retryWithSleep', [Map.class, Closure.class], { m, b -> b() })
  }

  @Test
  void test_with_version() throws Exception {
    def version = "1.12.2"
    helper.registerAllowedMethod('readFile', [Map.class], { version })
    def isOK = false
    script.call(version: version){
      if(binding.getVariable("PATH+NVM") == "WS/.nvm/versions/node/${version}/bin"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PATH+NVM'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing nvm'))
    assertTrue(assertMethodCallContainsPattern('sh', "Installing Node.js ${version}"))
    assertJobStatusSuccess()
  }

  @Test
  void test_default_nodeVersion() throws Exception {
    def version = "1.15.1"
    helper.registerAllowedMethod('readFile', [Map.class], { version })
    helper.registerAllowedMethod('nodeJSDefaultVersion', [], { version })
    def isOK = false
    script.call(){
      if(binding.getVariable("PATH+NVM") == "WS/.nvm/versions/node/${version}/bin"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing nvm'))
    assertTrue(assertMethodCallContainsPattern('sh', "Installing Node.js ${version}"))
    assertJobStatusSuccess()
  }

  @Test
  void test_if_nvm_path_does_not_exist() throws Exception {
    def version = "1.15.1"
    helper.registerAllowedMethod('readFile', [Map.class], { version })
    helper.registerAllowedMethod('nodeJSDefaultVersion', [], { version })
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    def isOK = false
    try {
      script.call(){
        if (binding.getVariable("PATH+NVM") == "WS/.nvm/versions/node/${version}/bin"){
          isOK = true
        }
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertTrue(assertMethodCallContainsPattern('error', 'does not exist'))
  }
}
