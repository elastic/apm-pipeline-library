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

class WithGoEnvWindowsStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withGoEnvWindows.groovy')
    env.PATH = '\\foo\\bin'
    helper.registerAllowedMethod('is32', {return false})
    helper.registerAllowedMethod('isUnix', [Closure.class], { return false })
    helper.registerAllowedMethod('nodeOS', [], { "windows" })
    helper.registerAllowedMethod('retryWithSleep', [Map.class, Closure.class], { m, b -> b() })
  }

  def definedVariables(version, suffix='windows') {
    return (binding.getVariable("PATH") == "WS\\bin;WS\\.gvm\\versions\\go${version}.${suffix}.amd64\\bin;C:\\ProgramData\\chocolatey\\bin;C:\\tools\\mingw64\\bin;\\foo\\bin"
      && binding.getVariable("GOROOT") == "WS\\.gvm\\versions\\go${version}.${suffix}.amd64"
      && binding.getVariable("GOPATH") == "WS")
  }

  @Test
  void test() throws Exception {
    def isOK = false
    script.call(version: '1.12.2'){
      isOK = definedVariables('1.12.2', 'windows')
      println isOK
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'Installing Go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_GoVersion_env_variable() throws Exception {
    env.GO_VERSION = '1.12.2'
    def isOK = false
    script.call(){
      isOK = definedVariables('1.12.2', 'windows')
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'Installing Go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void testOS() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { "HAL9000" })
    env.GO_VERSION = '1.12.2'
    def isOK = false
    script.call(){
      isOK = definedVariables('1.12.2', 'HAL9000')
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'Installing Go 1.12.2'))
    assertJobStatusSuccess()
  }

@Test
void testOSArg() throws Exception {
  env.GO_VERSION = '1.12.2'
  def isOK = false
  script.call(os: 'custom-os'){
    isOK = definedVariables('1.12.2', 'custom-os')
  }
  printCallStack()
  assertTrue(isOK)
  assertTrue(assertMethodCallContainsPattern('bat', 'Installing Go 1.12.2'))
  assertJobStatusSuccess()
}

  @Test
  void testPkgs() throws Exception {
    def isOK = false
    script.call(version: '1.12.2', pkgs: [ "P1", "P2" ]){
      isOK = definedVariables('1.12.2', 'windows')
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'Installing P1'))
    assertTrue(assertMethodCallContainsPattern('bat', 'Installing P2'))
    assertJobStatusSuccess()
  }

  @Test
  void testDefaultGoVersion() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { "windows" })
    def scriptDV = loadScript('vars/goDefaultVersion.groovy')
    def version = scriptDV.defaultVersion()
    def isOK = false
    script.call(){
      isOK = definedVariables("${version}", 'windows')
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', "Installing Go ${version}"))
    assertJobStatusSuccess()
  }
}
