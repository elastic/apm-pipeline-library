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
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GetVaultSecretStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/getVaultSecret.groovy')

    helper.registerAllowedMethod('httpRequest', [Map.class], { m ->
      if(m?.url?.contains("v1/secret/apm-team/ci/secret")){
        return "{plaintext: '12345', encrypted: 'SECRET'}"
      }
      if(m?.url?.contains("v1/auth/approle/login")){
        return "{auth: {client_token: 'TOKEN'}}"
      }
    })
  }

  @Test
  void test() throws Exception {
    def jsonValue = script.call("secret")
    assertTrue(jsonValue.plaintext == '12345')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testMap() throws Exception {
    def jsonValue = script.call(secret: "secret/apm-team/ci/secret")
    assertTrue(jsonValue.plaintext == '12345')
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testNoSecret() throws Exception {
    try {
      def jsonValue = script.call()
    } catch(e) {
      //NOOP
    }
    assertTrue(assertMethodCallContainsPattern('error', 'getVaultSecret: No valid secret to looking for'))
  }

  @Test
  void testGetTokenError() throws Exception {
    helper.registerAllowedMethod('httpRequest', [Map.class], { m ->
      if(m?.url?.contains("v1/auth/approle/login")){
        return "{auth: ''}"
      }
    })

    try {
      def jsonValue = script.call("secret")
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getVaultSecret: Unable to get the token.'))
  }

  @Test
  void testGetSecretError() throws Exception {
    helper.registerAllowedMethod('httpRequest', [Map.class], { m ->
      if(m?.url?.contains("v1/secret/apm-team/ci/secret")){
        return ""
      }
      if(m?.url?.contains("v1/auth/approle/login")){
        return "{auth: {client_token: 'TOKEN'}}"
      }
    })

    try {
      def jsonValue = script.call("secret")
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'getVaultSecret: Unable to get the secret.'))
  }

  @Test
  void testReadSecretWrapper() throws Exception {
    script.readSecretWrapper {
      // TODO
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', '[{credentialsId=vault-addr, variable=VAULT_ADDR}, {credentialsId=vault-role-id, variable=VAULT_ROLE_ID}, {credentialsId=vault-secret-id, variable=VAULT_SECRET_ID}]'))
    assertJobStatusSuccess()
  }
}
