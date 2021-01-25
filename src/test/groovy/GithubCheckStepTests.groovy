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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertSame
import static org.junit.Assert.assertTrue

class GithubCheckStepTests extends ApmBasePipelineTest {
  def script

  def privateKey

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubCheck.groovy')
    privateKey = new File("src/test/resources/github-app-private-key-tests.pem").text
  }

  @Test
  void test_without_name_argument() throws Exception {
    try {
      script.call()
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'missing name argument'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_wrong_vault_details() throws Exception {
    try {
      script.call(name: 'foo', secret: VaultSecret.SECRET_ERROR.toString())
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Unable to get credentials from the vault'))
    assertJobStatusFailure()
  }

  @Test
  void test_getToken_success() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [token: 1]
    })
    script.getToken(jsonWebToken: 'foo', installationId: '123')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'token=foo'))
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'https://api.github.com/app/installations/123/access_tokens'))
    assertJobStatusSuccess()
  }

  @Test
  void test_getToken_failure() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    try {
      script.getToken(jsonWebToken: 'foo', installationId: '123')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getToken: Failed to create a JWT'))
    assertJobStatusFailure()
  }

  @Test
  void test_getRSAPrivateKey_success() throws Exception {
    script.getRSAPrivateKey(privateKey)
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void test_getRSAPrivateKey_failure() throws Exception {
    try {
      script.getRSAPrivateKey('secret')
    } catch(err) {
      // NOOP
      println err
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getRSAPrivateKey: Failed to create a JWT'))
    assertJobStatusFailure()
  }

  @Test
  void test_getJsonWebToken_success() throws Exception {
    script.getJsonWebToken(privateKeyContent: privateKey, appId: '123')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void test_getJsonWebToken_failure() throws Exception {
    try {
      script.getJsonWebToken(privateKeyContent: 'foo', appId: '123')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getJsonWebToken: Failed to create a JWT'))
    assertJobStatusFailure()
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_match() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [ check_runs: [[
                  name: 'bar',
                  id: '1'
                ],[
                  name: 'my-check',
                  id: '2'
                ]
             ]]
    })
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    assertSame(ret, '2')
    assertJobStatusSuccess()
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_no_match() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [ check_runs: [[
                              name: 'bar',
                              id: '1'
                            ]
      ]]
    })
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    assertNull(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_exception() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  // setCheckName


}
