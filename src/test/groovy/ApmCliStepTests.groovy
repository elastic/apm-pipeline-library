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

class ApmCliStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/apmCli.groovy')
    env.STAGE_NAME = "fooStage"
  }

  @Test
  void test() throws Exception {
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'text=apmCli: [url:https://apm.example.com:8200, token:password, serviceName:serviceFoo'))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/apm-cli/requirements.txt'))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/apm-cli/apm-cli.py'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVER_URL, password=https://apm.example.com:8200'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TOKEN, password=password'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVICE_NAME, password=serviceFoo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TRANSACTION_NAME, password=fooStage-BEGIN'))
    assertJobStatusSuccess()
  }

  @Test
  void testFull() throws Exception {
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo",
      transactionName: "foo",
      spanName: "spanFoo",
      spanCommand: "cmdFoo",
      spanLabel: '{"foo": "valueFoo"}',
      result: "restulFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'text=apmCli: [url:https://apm.example.com:8200, token:password, serviceName:serviceFoo'))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/apm-cli/requirements.txt'))
    assertTrue(assertMethodCallContainsPattern('libraryResource', 'scripts/apm-cli/apm-cli.py'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVER_URL, password=https://apm.example.com:8200'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TOKEN, password=password'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVICE_NAME, password=serviceFoo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TRANSACTION_NAME, password=foo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SPAN_NAME, password=spanFoo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SPAN_COMMAND, password=cmdFoo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SPAN_LABELS, password={"foo": "valueFoo"}'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TRANSACTION_RESULT, password=restulFoo'))
    assertJobStatusSuccess()
  }

  @Test
  void testSpanCmd() throws Exception {
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo", spanCommand: "cmdFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SPAN_NAME, password=cmdFoo'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SPAN_COMMAND, password=cmdFoo'))
    assertJobStatusSuccess()
  }

  @Test
  void testNOOP() throws Exception {
    script.call(url: "https://apm.example.com:8200", token: "password")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'apmCli: not executed.'))
    assertJobStatusSuccess()
  }

  @Test
  void testVaultConfig() throws Exception {
    helper.registerAllowedMethod('getVaultSecret', [Map.class],
      { return [
          "data": [
            "value": '{"url": "https://vaultapm.example.com:8200", "token": "vaultApmPassword"}'
            ]
          ]
    })

    script.call(apmCliConfig: "secret/oblt/apm", serviceName: "serviceFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVER_URL, password=https://vaultapm.example.com:8200'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TOKEN, password=vaultApmPassword'))
    assertJobStatusSuccess()
  }

  @Test
  void testServiceNameEnv() throws Exception {
    env.APM_CLI_SERVICE_NAME = "fooEnv"
    script.call(url: "https://apm.example.com:8200", token: "password")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_SERVICE_NAME, password=fooEnv'))
    assertJobStatusSuccess()
  }

  @Test
  void testParentTransaction() throws Exception {
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo", parentTransaction: "parentFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_PARENT_TRANSACTION, password=parentFoo'))
    assertJobStatusSuccess()
  }

  @Test
  void testParentTransactionEnv() throws Exception {
    env.APM_CLI_PARENT_TRANSACTION = "parentFooEnv"
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_PARENT_TRANSACTION, password=parentFooEnv'))
    assertJobStatusSuccess()
  }

  @Test
  void testBeginEnd() throws Exception {
    script.transactions['fooStage'] = true;
    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'var=APM_CLI_TRANSACTION_NAME, password=fooStage-END'))
    assertJobStatusSuccess()
  }

  @Test
  void testSavedParentTransaction() throws Exception {
    helper.registerAllowedMethod('readFile', [Map.class],{'fooID'})

    script.call(url: "https://apm.example.com:8200", token: "password", serviceName: "serviceFoo", saveTsID: true)
    printCallStack()
    assertTrue(env.APM_CLI_PARENT_TRANSACTION == 'fooID')
    assertTrue(assertMethodCallContainsPattern('log', 'apmCli: Persistent transaction ID on APM_CLI_PARENT_TRANSACTION'))
    assertJobStatusSuccess()
  }
}
