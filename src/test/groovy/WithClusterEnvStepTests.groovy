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

class WithClusterEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withClusterEnv.groovy')
  }

  @Test
  void test() throws Exception {
    def isOK = false
    script.call(cluster: 'foo') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=ELASTICSEARCH_URL'))
    assertFalse(assertMethodCallContainsPattern('withEnvMask', 'var=KIBANA_URL'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_elasticsearch() throws Exception {
    def isOK = false
    script.call(cluster: 'foo', elasticsearch: false) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
    assertFalse(assertMethodCallContainsPattern('withElasticsearchDeploymentEnv', 'foo'))
    assertFalse(assertMethodCallContainsPattern('withEnvMask', 'var=ELASTICSEARCH_URL'))
  }

  @Test
  void test_with_kibana() throws Exception {
    def isOK = false
    script.call(cluster: 'foo', kibana: true) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
    assertTrue(assertMethodCallContainsPattern('withKibanaDeploymentEnv', 'foo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=KIBANA_URL'))
  }

  @Test
  void testWithBodyError() throws Exception {
    try {
      script.call(cluster: 'foo') {
        throw new Exception('Mock an error')
      }
    } catch(e){
      //NOOP
      assertTrue(e instanceof Exception)
      assertTrue(e.toString().contains('Mock an error'))
    }
    printCallStack()
  }

  @Test
  void test_secret_error() throws Exception {
    try {
      script.call(cluster: 'error'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Unable to get credentials from the vault: Error message'))
  }

  @Test
  void test_secret_missing_data_in_secret() throws Exception {
    try {
      script.call(cluster: 'missing'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'was not possible to get the authentication info for the url field.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_cluster() throws Exception {
    testMissingArgument('cluster') {
      script.call() {
        // NOOP
      }
    }
  }
}
