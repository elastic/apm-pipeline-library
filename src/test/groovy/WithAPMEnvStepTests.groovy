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

class WithAPMEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withAPMEnv.groovy')
  }

  @Test
  void test_SecretError() throws Exception {
    def isOK = false
    script.call(secret: VaultSecret.SECRET_ERROR.toString()){
      isOK = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'withAPMEnv: is disabled. Unable to get credentials from the vault: Error message'))
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void test_SecretNotFound() throws Exception {
    def isOK = false
    script.call(secret: 'secretNotExists'){
      isOK = true
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'withAPMEnv: is disabled. Missing fields in the vault secret'))
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_default() throws Exception {
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'ELASTIC_APM_SERVER_URL'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_the_parameters() throws Exception {
    def isOK = false
    script.call(secret: VaultSecret.SECRET_APM_CUSTOMISED.toString(), tokenFieldName: 'token', urlFieldName: 'url'){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'ELASTIC_APM_SERVER_URL'))
    assertJobStatusSuccess()
  }
}
