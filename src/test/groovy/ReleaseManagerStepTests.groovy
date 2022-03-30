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

class ReleaseManagerStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/releaseManager.groovy')
  }

  @Test
  void test_defaults() throws Exception {
    script.call(project: 'apm-server', version: '1.2.3')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PROJECT=apm-server, TYPE=snapshot, VERSION=1.2.3, FOLDER=build/distribution, OUTPUT_FILE=release-manager-report.out'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Release Manager'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_values() throws Exception {
    script.call(project: 'beats', version: '1.2.3', type: 'staging', artifactsFolder: 'build/dist', outputFile: 'foo.txt')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnv', 'PROJECT=beats, TYPE=staging, VERSION=1.2.3, FOLDER=build/dist, OUTPUT_FILE=foo.txt'))
    assertTrue(assertMethodCallContainsPattern('sh', 'Release Manager'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_snapshot() throws Exception {
    try {
      script.call(project: 'apm-server', version: '1.2.3-SNAPSHOT')
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', '-SNAPSHOT'))
  }

  @Test
  void test_with_missing_project() throws Exception {
    testMissingArgument('project') {
      script.call()
    }
  }

  @Test
  void test_with_missing_version() throws Exception {
    testMissingArgument('version') {
      script.call(project: 'foo')
    }
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

}
