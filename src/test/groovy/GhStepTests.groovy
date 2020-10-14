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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GhStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/gh.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gh: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_args() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gh: command argument is required.'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_flags() throws Exception {
    def script = loadScript(scriptName)
    script.call(command: 'issue list', flags: [ label: 'foo'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label=foo"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags_with_list() throws Exception {
    def script = loadScript(scriptName)
    script.call(command: 'issue list', flags: [ label: ['foo', 'bar'] ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', "gh issue list --label=foo --label=bar"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { throw new Exception('unknown command "foo" for "gh issue"') })
    def result
    try {
      result = script.call(command: 'issue foo')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'returnStdout=true'))
    assertNull(result)
  }
}
