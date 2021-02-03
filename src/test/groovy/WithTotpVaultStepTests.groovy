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

class WithTotpVaultStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withTotpVault.groovy')
  }

  @Test
  void test_MissingArguments() throws Exception {
    testMissingArgument('code_var_name') {
      script.call(secret: VaultSecret.SECRET.toString()){
        //NOOP
      }
    }
  }

  @Test
  void test_SecretError() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString(), code_var_name: 'bar'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withTotpVault: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void test_SecretNotFound() throws Exception {
    try{
      script.call(secret: 'secretNotExists', code_var_name: 'bar'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withTotpVault: was not possible to get authentication info'))
    assertJobStatusFailure()
  }

  @Test
  void test_variable_is_created() throws Exception {
    def isOK = false
    script.call(secret: VaultSecret.SECRET_TOTP.toString(), code_var_name: 'VAULT_TOTP'){
      isOK = (binding.getVariable('VAULT_TOTP') == '123456')
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=VAULT_TOTP'))
    assertJobStatusSuccess()
  }
}
