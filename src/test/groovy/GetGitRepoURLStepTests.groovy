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
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class GetGitRepoURLStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/getGitRepoURL.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    String url = 'http://github.com/org/repo.git'
    def script = loadScript(scriptName)
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git config --get remote.origin.url'.equals(map.script)) {
          return url
      }
      return ""
    })
    String ret = script.call()
    printCallStack()
    assertTrue(url.equals(ret))
  }

  @Test
  void test_no_git_repo() throws Exception {
    String url = 'http://github.com/org/repo.git'
    def script = loadScript(scriptName)
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      throw new Exception('hudson.AbortException: script returned exit code 1')
    })
    try {
      script.call()
    } catch(e) {
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getGitRepoURL: could not fetch the URL details'))
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
    assertTrue(assertMethodCallContainsPattern('error', 'getGitRepoURL: windows is not supported yet.'))
    assertJobStatusFailure()
  }
}
