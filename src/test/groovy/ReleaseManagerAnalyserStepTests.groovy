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

class ReleaseManagerAnalyserStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/releaseManagerAnalyser.groovy')
    helper.registerAllowedMethod('readFile', [Map.class], { '''
There were some errors while running the release manager, let's analyse them.
* Environmental issue with Vault. Try again.
'''})
  }

  @Test
  void test() throws Exception {
    def ret = script.call(file: 'report.txt')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'RAW_OUTPUT=report.txt, REPORT=folder/release-manager-report.out'))
    assertTrue(ret.contains('Try again'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_missing_file() throws Exception {
    testMissingArgument('file') {
      script.call()
    }
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

}
