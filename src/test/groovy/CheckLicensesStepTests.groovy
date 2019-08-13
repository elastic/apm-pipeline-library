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

import co.elastic.TestUtils
import co.elastic.mock.DockerMock
import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class CheckLicensesStepTests extends BasePipelineTest {
  static final String scriptName = 'vars/checkLicenses.groovy'
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = '/tmp'
    env.BASE_DIR = 'base'

    binding.setVariable('env', env)
    binding.setProperty('docker', new DockerMock())

    helper.registerAllowedMethod('archive', [String.class], { 'OK' })
    helper.registerAllowedMethod('catchError', [Closure.class], { s -> s() })
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod('isUnix', [ ], { true })
    helper.registerAllowedMethod('junit', [Map.class], { 'OK' })
    helper.registerAllowedMethod('readFile', [Map.class], { '' })
    helper.registerAllowedMethod('sh', [Map.class], { 'OK' })
    helper.registerAllowedMethod('withEnv', [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod('writeFile', [Map.class], { 'OK' })
  }

  @Test
  void testSuccessWithoutArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithExtArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(ext: '.foo')
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('-ext .foo')
    })
  }

  @Test
  void testWithEnvAndAllTheEnvironmentVariables() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'withEnv'
    }.any { call ->
      callArgsToString(call).contains("[HOME=${env.WORKSPACE}/${env.BASE_DIR}]")
    })
  }

  @Test
  void testWithEnvAndNoBaseDirVariable() throws Exception {
    env.BASE_DIR = ''
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'withEnv'
    }.any { call ->
      callArgsToString(call).contains("[HOME=${env.WORKSPACE}/]")
    })
  }

  @Test
  void testSuccessWithExcludeArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(exclude: './bar')
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('-exclude ./bar')
    })
  }

  @Test
  void testSuccessWithLicenseArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(license: 'Elastic')
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('-license Elastic')
    })
  }

  @Test
  void testSuccessWithLicensorArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(licensor: 'Foo S.A.')
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('-licensor "Foo S.A."')
    })
  }

  @Test
  void testSuccessWithSkipArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(skip: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('-d')
    })
  }

  @Test
  void testSuccessWithJunitArgument() throws Exception {
    def script = loadScript(scriptName)
    script.call(skip: true, junit: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'writeFile'
    }.any { call ->
      callArgsToString(call).contains('<testcase/>')
    })
  }

  @Test
  void testWarningsWithJunitArgument() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('readFile', [Map.class], { 'foo/bar/file.java: is missing the license header' })
    script.call(skip: true, junit: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'writeFile'
    }.any { call ->
      callArgsToString(call).contains('<testcase name="file.java" classname="foo.bar.file.java"')
    })
  }

  @Test
  void testWarningsWithJunitArgumentAndHiddenFolders() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('readFile', [Map.class], { '.foo/bar/file.java: is missing the license header' })
    script.call(skip: true, junit: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'writeFile'
    }.any { call ->
      callArgsToString(call).contains('<testcase name="file.java" classname="foo.bar.file.java"')
    })
  }

  @Test
  void testMissingSkipWhenJunitArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(junit: true)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('checkLicenses: skip should be enabled when using the junit flag.')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWindows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'error'
    }.any { call ->
      callArgsToString(call).contains('checkLicenses: windows is not supported yet.')
    })
    assertJobStatusFailure()
  }
}
