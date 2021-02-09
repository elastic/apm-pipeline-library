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

class WithEsEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withEsEnv.groovy')

    env.BRANCH_NAME = 'branch'
    env.CHANGE_ID = '29480a51'
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
    env.GITHUB_TOKEN = 'TOKEN'
  }

  @Test
  void test() throws Exception {
    def isOK = false
    script.call(secret: VaultSecret.SECRET.toString()){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def isOK = false
    script.call(url: 'https://es.example.com', secret: VaultSecret.SECRET.toString()){
      if(binding.getVariable("CLOUD_URL") == "https://username:user_password@es.example.com"
      && binding.getVariable("CLOUD_ADDR") == "https://es.example.com"
      && binding.getVariable("CLOUD_USERNAME") == "username"
      && binding.getVariable("CLOUD_PASSWORD") == "user_password"){
        isOK = true
      }
    }
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }

  @Test
  void testSecretNotFound() throws Exception {
    try{
      script.call(secret: 'secretNotExists'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withEsEnv: was not possible to get authentication info'))
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString()){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withEsEnv: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void testWrongProtocol() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET.toString(), url: 'ht://wrong.example.com'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withEsEnv: unknow protocol, the url is not http(s).'))
    assertJobStatusFailure()
  }
}
