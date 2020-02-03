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
  }
  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call("http://jenkins.example.com/job/myJob", "1")
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testFailedToDownload() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('fileExists', [String.class], { return false })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m.label == 'Get Build info details'){
        return 1
      }
      return 0
    })
    script.call("http://jenkins.example.com/job/myJob", "1")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('writeJSON', 'file=job-info.json'))
    assertJobStatusSuccess()
  }

  @Test
  void testWindows() throws Exception {
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

  @Test
  void test_bulkDownload_with_empty_map() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.bulkDownload([])
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'bulkDownload cannot be executed with empty arguments'))
    assertJobStatusFailure()
  }

  @Test
  void test_bulkDownload_with_some_entries_and_failures() throws Exception {
    def script = loadScript(scriptName)
    // force to create an empty file for bar
    helper.registerAllowedMethod('fileExists', [String.class], { return it.equals('file') })
    script.bulkDownload([ 'foo': 'bar', 'url': 'file' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-o bar foo'))
    assertTrue(assertMethodCallContainsPattern('sh', '-o file url'))
    assertFalse(assertMethodCallContainsPattern('writeJSON', 'file=file'))
    assertTrue(assertMethodCallContainsPattern('writeJSON', 'file=bar'))
    assertJobStatusSuccess()
  }
}
