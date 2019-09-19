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
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class TarStepTests extends BaseDeclarativePipelineTest {
  String scriptName = 'vars/tar.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('WORKSPACE', 'WS')
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testError() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertJobStatusUnstable()
  }

  @Test
  void testAllowMissing() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [String.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: true, archive: false)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testIsNotUnix() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod("isUnix", [], {false})
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("tar step is compatible only with unix systems")
    })
    assertJobStatusSuccess()
  }
}
