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

class GithubAppTokenStepTests extends ApmBasePipelineTest {

  def privateKey

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubAppToken.groovy')
    privateKey = new File("src/test/resources/github-app-private-key-tests.pem").text
  }

  @Test
  void test_with_wrong_vault_details() throws Exception {
    try {
      script.call(secret: VaultSecret.SECRET_ERROR.toString())
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Unable to get credentials from the vault'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_default() throws Exception {
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [token: 1]
    })
    try {
    script.call()
    } catch (e) {
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('wrap', 'MaskPasswordsBuildWrapper'))
    assertJobStatusSuccess()
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
}
