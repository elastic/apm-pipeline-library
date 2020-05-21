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
import static org.junit.Assert.assertTrue

class PublishToCDNStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/publishToCDN.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'publishToCDN: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_source() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(target: 'bar')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'publishToCDN: Missing source argument.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_target() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(source: 'foo')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'publishToCDN: Missing target argument.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_secret() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(source: 'foo', target: 'bar')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'publishToCDN: Missing secret argument.'))
    assertJobStatusFailure()
  }

  @Test
  void test_secret_error() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_ERROR.toString())
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('writeJSON', 'file=service-account.json'))
    assertFalse(assertMethodCallContainsPattern('sh', 'rm service-account.json'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_install() throws Exception {
    def script = loadScript(scriptName)
    script.call(install: false, source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString())
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'https://sdk.cloud.google.com'))
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString(), header: 'my_header')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'https://sdk.cloud.google.com'))
    assertTrue(assertMethodCallContainsPattern('writeJSON', 'file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', '--key-file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', '-h my_header cp foo gs://bar'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm service-account.json'))
    assertJobStatusSuccess()
  }

  @Test
  void test_error_when_pushing() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m.label.contains('Upload')) {
        updateBuildStatus('FAILURE')
        throw new Exception('Something wrong with the connection')
      }
    })
    try {
      script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString())
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('writeJSON', 'file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', '--key-file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', 'cp foo gs://bar'))
    assertTrue(assertMethodCallContainsPattern('error', 'publishToCDN: error'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm service-account.json'))
    assertJobStatusFailure()
  }
}
