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

class WithGoEnvUnixStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withGoEnvUnix.groovy')
    env.PATH = '/foo/bin'
    helper.registerAllowedMethod('retryWithSleep', [Map.class, Closure.class], { m, b -> b() })
  }

  @Test
  void test() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('withEnv', 'GOPATH=WS, GOARCH=amd64'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_arm64() throws Exception {
    helper.registerAllowedMethod('isArm', { return true })
    def isOK = false
    script.call(version: "1.12.2"){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.linux.arm64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.linux.arm64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'GOPATH=WS, GOARCH=arm64'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_arm64_with_different_GOARCH() throws Exception {
    addEnvVar('GOARCH', 'amd64')
    helper.registerAllowedMethod('isArm', { return true })
    def isOK = false
    script.call(version: "1.12.2"){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('log', "GOARCH env variable matches 'amd64' but it will be overridden to 'arm64'"))
    assertTrue(assertMethodCallContainsPattern('withEnv', 'GOPATH=WS, GOARCH=arm64'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing go 1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void testGoVersion() throws Exception {
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
  void testOSArg() throws Exception {
    env.GO_VERSION = "1.12.2"
    def isOK = false
    script.call(os: 'custom-os'){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.12.2.custom-os.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.12.2.custom-os.amd64"
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
  void testPkgs_go_1_12() throws Exception {
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
    assertTrue(assertMethodCallContainsPattern('sh', 'go get -u P1'))
    assertTrue(assertMethodCallContainsPattern('sh', 'go get -u P2'))
    assertJobStatusSuccess()
  }

  @Test
  void testPkgs_go_1_16() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def isOK = false
    script.call(version: "1.16.1", pkgs: [ "P1", "P2" ]){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go1.16.1.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go1.16.1.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'go install P1@latest'))
    assertTrue(assertMethodCallContainsPattern('sh', 'go install P2@latest'))
    assertJobStatusSuccess()
  }

  @Test
  void testPkgs_with_version() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def isOK = false
    script.call(version: "1.16.1", pkgs: [ "P1@1", "P2@2" ]){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', 'go install P1@1'))
    assertTrue(assertMethodCallContainsPattern('sh', 'go install P2@2'))
    assertJobStatusSuccess()
  }

  @Test
  void testDefaultGoVersion() throws Exception {
    helper.registerAllowedMethod('nodeOS', [], { "linux" })
    def version = "1.15.1"
    helper.registerAllowedMethod('goDefaultVersion', [], { version })
    def isOK = false
    script.call(){
      if(binding.getVariable("PATH") == "WS/bin:WS/.gvm/versions/go${version}.linux.amd64/bin:/foo/bin"
        && binding.getVariable("GOROOT") == "WS/.gvm/versions/go${version}.linux.amd64"
        && binding.getVariable("GOPATH") == "WS" ){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('sh', "Installing go ${version}"))
    assertJobStatusSuccess()
  }

  @Test
  void test_installPackages_with_env_variable() throws Exception {
    addEnvVar('GOARCH', 'amd64')
    script.installPackages(pkgs: [ "P1", "P2" ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'GOARCH=amd64'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P1'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_installPackages_without_env_variable() throws Exception {
    helper.registerAllowedMethod('isArm', { return true })
    script.installPackages(pkgs: [ "P1", "P2" ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'GOARCH=arm64'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P1'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Installing P2'))
    assertJobStatusSuccess()
  }

  @Test
  void test_goArch_for_arm() throws Exception {
    helper.registerAllowedMethod('isArm', { return true })
    assertTrue(script.goArch().equals('arm64'))
  }

  @Test
  void test_goArch_for_linux() throws Exception {
    helper.registerAllowedMethod('isUnix', { return true })
    assertTrue(script.goArch().equals('amd64'))
  }

  @Test
  void test_goArch_for_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', { return false })
    assertTrue(script.goArch().equals('amd64'))
  }
}
