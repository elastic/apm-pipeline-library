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

class WithNpmrcStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withNpmrc.groovy')
    env.HOME = '/foo'
  }

  @Test
  void testDefaultParameters() throws Exception {
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('dir', '/foo'))
    assertTrue(assertMethodCallContainsPattern('writeFile', '//registry.npmjs.org'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm .npmrc'))
    assertJobStatusSuccess()
  }

  @Test
  void testDefaultParametersAndWindows() throws Exception {
    helper.registerAllowedMethod('isUnix', [ ], { false })
    def isOK = false
    script.call {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('bat', 'del .npmrc'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithAllParameters() throws Exception {
    def isOK = false
    script.call(path: '/bar', npmrcFile: 'mytoken', registry: 'foo-bar', secret: VaultSecret.SECRET_NPMRC.toString()) {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('dir', '/bar'))
    assertTrue(assertMethodCallContainsPattern('writeFile', '//foo-bar'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'authToken=mytoken'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm mytoken'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithBodyError() throws Exception {
    try {
      script.call {
        throw new Exception('Mock an error')
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withNpmrc: error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm .npmrc'))
    assertJobStatusFailure()
  }

  @Test
  void test_SecretError() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString()){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withNpmrc: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void test_SecretNotFound() throws Exception {
    try{
      script.call(secret: 'secretNotExists'){
        //NOOP
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'withNpmrc: was not possible to get authentication info'))
    assertJobStatusFailure()
  }
}
