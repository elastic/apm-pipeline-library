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
import static org.junit.Assert.assertFalse

class SendBenchmarksStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/sendBenchmarks.groovy')

    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'

    helper.registerAllowedMethod('withGoEnv', [Map.class, Closure.class], { m, b -> b()  })
    helper.registerAllowedMethod('withGoEnv', [Closure.class], { b -> b()  })
    helper.registerAllowedMethod('httpRequest', [Map.class], {
      return "{'errors': false}"
    })
  }

  @Test
  void test() throws Exception {
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    script.call(file: 'bench.out', index: 'index-name', url: 'https://vault.example.com', secret: VaultSecret.SECRET.toString(), archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testSecretNotFound() throws Exception {
    try{
      def ret = script.call(secret: VaultSecret.SECRET_NOT_VALID.toString())
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Benchmarks: was not possible to get authentication info to send benchmarks'))
    assertJobStatusFailure()
  }

  @Test
  void testSecretError() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString())
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Benchmarks: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void testWrongProtocol() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET.toString(), url: 'ht://wrong.example.com')
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Benchmarks: unknow protocol, the url is not http(s).'))
    assertJobStatusFailure()
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void testMissingSecretArgument() throws Exception {
    testMissingArgument('secret') {
      script.prepareAndRun() { }
    }
  }

  @Test
  void testMissingUrlArgument() throws Exception {
    testMissingArgument('url_var') {
      script.prepareAndRun(secret: VaultSecret.SECRET.toString()) { }
    }
  }

  @Test
  void testMissingUserArgument() throws Exception {
    testMissingArgument('user_var') {
      script.prepareAndRun(secret: VaultSecret.SECRET.toString(), url_var: 'URL_') { }
    }
  }

  @Test
  void testMissingPassArgument() throws Exception {
    testMissingArgument('pass_var') {
      script.prepareAndRun(secret: VaultSecret.SECRET.toString(), url_var: 'URL_', user_var: 'USER_') { }
    }
  }

  @Test
  void testPrepareAndRunWithSecretError() throws Exception {
    def isOK = false
    try {
      script.prepareAndRun(secret: VaultSecret.SECRET_ERROR.toString(), url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
        isOK = true
      }
    } catch(e){
      println e.toString()
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertTrue(assertMethodCallContainsPattern('error', 'prepareAndRun: Unable to get credentials from the vault: Error message'))
    assertJobStatusFailure()
  }

  @Test
  void testPrepareAndRunWithSecretNotFound() throws Exception {
    def isOK = false
    try{
      script.prepareAndRun(secret: VaultSecret.SECRET_NOT_VALID.toString(), url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
        isOK = true
      }
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(isOK)
    assertTrue(assertMethodCallContainsPattern('error', 'prepareAndRun: was not possible to get authentication info to send benchmarks'))
    assertJobStatusFailure()
  }

  @Test
  void testPrepareAndRun() throws Exception {
    def isOK = false
    script.prepareAndRun(secret: VaultSecret.SECRET.toString(), url_var: 'URL_', user_var: 'USER_', pass_var: 'PASS_') {
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withEnvMask', "var=URL_, password=${EXAMPLE_URL}"))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', "var=USER_, password=username"))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', "var=PASS_, password=user_password"))
    assertJobStatusSuccess()
  }

  @Test
  void testPrepareAndRunInWindows() throws Exception {
    testWindows() {
      script.prepareAndRun() { }
    }
  }

  @Test
  void test_response_with_errors() throws Exception {
    helper.registerAllowedMethod('httpRequest', [Map.class], {
      return "{'errors': 'true'}"
    })
    try {
      script.call(file: 'bench.out', index: 'index-name', url: 'https://vault.example.com', secret: VaultSecret.SECRET.toString(), archive: true)
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Benchmarks: there was a response with an error. Review response'))
    assertJobStatusFailure()
  }
}
