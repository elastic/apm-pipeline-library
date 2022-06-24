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

class GenerateGoBenchmarkDiffStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/generateGoBenchmarkDiff.groovy')
    env.CHANGE_TARGET = 'main'
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_file_param() throws Exception {
    testMissingArgument('file') {
      script.call()
    }
  }

  @Test
  void test_non_existing_file() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    try {
      script.call(file: 'unknown')
    } catch (e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
    assertJobStatusFailure()
  }

  @Test
  void test_no_pull_request() throws Exception {
    env.remove('CHANGE_TARGET')
    script.call(file: 'bench.out')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'generateGoBenchmarkDiff'))
    assertTrue(assertMethodCallOccurrences('copyArtifacts', 0))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 0))
    assertTrue(assertMethodCallContainsPattern('stash', 'name=bench.diff, includes=bench.diff'))
    assertJobStatusSuccess()
  }

  @Test
  void test_pull_request() throws Exception {
    script.call(file: 'bench.out')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'benchstat bench.out build/main/bench.out  | tee bench.diff'))
    assertTrue(assertMethodCallOccurrences('copyArtifacts', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('stash', 'name=bench.diff, includes=bench.diff'))
    assertJobStatusSuccess()
  }

  @Test
  void test_pull_request_with_exclude() throws Exception {
    script.call(file: 'bench.out', filter: 'exclude')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "benchstat bench.out build/main/bench.out | grep -v 'all equal' | grep -v '~' | tee bench.diff"))
    assertTrue(assertMethodCallOccurrences('copyArtifacts', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('stash', 'name=bench.diff, includes=bench.diff'))
    assertJobStatusSuccess()
  }
}
