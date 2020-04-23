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

class GetBuildInfoJsonFilesStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/getBuildInfoJsonFiles.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.JENKINS_URL = 'http://jenkins.example.com/'
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('fileExists', [String.class], { return false })
    script.call('http://jenkins.example.com/job/myJob', '1')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('writeFile', 'generate-build-data.sh'))
    assertTrue(assertMethodCallContainsPattern('sh', 'generate-build-data'))
    assertTrue(assertMethodCallContainsPattern('sh', 'http://jenkins.example.com/blue/rest/organizations/jenkins/pipelines/myJob/'))
    assertTrue(assertMethodCallContainsPattern('sh', 'http://jenkins.example.com/blue/rest/organizations/jenkins/pipelines/myJob/runs/1'))
    assertJobStatusSuccess()
  }

  @Test
  void test_failed_script() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('fileExists', [String.class], { return true })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m.label == 'generate-build-data'){
        return 1
      }
      return 0
    })
    script.call('http://jenkins.example.com/job/myJob', '1')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('writeFile', 'generate-build-data'))
    assertTrue(assertMethodCallContainsPattern('sh', 'generate-build-data.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call('', '')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getBuildInfoJsonFiles: windows is not supported yet.'))
    assertJobStatusFailure()
  }
}
