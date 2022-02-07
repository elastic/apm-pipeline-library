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

class WithKindEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withKindEnv.groovy')
  }

  @Test
  void test_default() throws Exception {
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'K8S_VERSION=v1.23.0, KIND_VERSION=v0.11.1'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_versions() throws Exception {
    def isOK = false
    script.call(k8sVersion: 'v1.0.0', kindVersion: 'v2.0.0') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnv', 'K8S_VERSION=v1.0.0, KIND_VERSION=v2.0.0'))
    assertTrue(assertMethodCallContainsPattern('sh', 'delete cluster'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_body_error() throws Exception {
    def ret = false
    try {
      script.call(retries: 3) {
        throw new Exception('Force failure')
        ret = true
      }
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('sh', 'delete cluster'))
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call() { }
    }
  }
}
