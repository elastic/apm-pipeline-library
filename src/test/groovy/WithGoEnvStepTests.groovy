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

class WithGoEnvStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/withGoEnv.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.PATH = '/foo/bin'
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def isOK = false
    script.call(version: "1.12.2"){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void testGoVersion() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    env.GO_VERSION = "1.12.2"
    def isOK = false
    script.call(){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void testOS() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('nodeOS', [], { "HAL9000" })
    env.GO_VERSION = "1.12.2"
    def isOK = false
    script.call(){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.HAL9000.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.HAL9000.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }


  @Test
  void testPkgs() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def isOK = false
    script.call(version: "1.12.2", pkgs: [ "P1", "P2" ]){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P1'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P2'))
    assertJobStatusSuccess()
  }


  @Test
  void testDefaultGoVersion() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def isOK = false
    script.call(){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.14.2.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.14.2.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.14.2'))
    assertJobStatusSuccess()
  }

}
