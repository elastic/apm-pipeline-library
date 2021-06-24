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

class WithAzureCredentialsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withAzureCredentials.groovy')
    env.HOME = '/foo'
  }

  @Test
  void test_default_parameters() throws Exception {
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('dir', '/foo'))
    assertTrue(assertMethodCallContainsPattern('writeFile', '"clientId": "client_id_1"'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm .credentials.json'))
    assertJobStatusSuccess()
  }

  @Test
  void test_default_parameters_on_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [ ], { false })
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'del .credentials.json'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_parameters() throws Exception {
    def isOK = false
    script.call(path: '/bar', credentialsFile: 'mytoken', secret: VaultSecret.SECRET_AZURE.toString()) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('dir', '/bar'))
    assertTrue(assertMethodCallContainsPattern('writeFile', '"clientId": "client_id_1"'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm mytoken'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_body_error() throws Exception {
    try {
      script.call {
        throw new Exception('Mock an error')
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withAzureCredentials: error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm .credentials.json'))
    assertJobStatusFailure()
  }

  @Test
  void test_secret_error() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString()){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withAzureCredentials: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void test_secret_not_found() throws Exception {
    try{
      script.call(secret: 'secretNotExists'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withAzureCredentials: was not possible to get authentication info'))
    assertJobStatusFailure()
  }
}
