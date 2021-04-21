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

class ArtifactsApiStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/artifactsApi.groovy')
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_action() throws Exception {
    testMissingArgument('action') {
      script.call()
    }
  }

  @Test
  void test_unsupported_action() throws Exception {
    try {
      script.call(action: 'unknown')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', "artifactsApi: unsupported action."))
    assertJobStatusFailure()
  }

  @Test
  void test_latest_versions() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class],{'''
    {
      "6.8":
        {
          "release_branch": "6.8",
          "branch": "6.8",
          "version": "6.8.16-SNAPSHOT",
          "build_id": "6.8.16-ac54b152"
      },
      "7.x":
        {
          "release_branch": "7.x",
          "branch": "7.x",
          "version": "7.13.0-SNAPSHOT",
          "build_id": "7.13.0-a6f186fd"
      }
    }'''})
    def obj = script.call(action: 'latest-versions')
    assertTrue(obj.get('6.8').branch.equals('6.8'))
    printCallStack()
  }
}
