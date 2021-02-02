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
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/publishToCDN.groovy')
    env.HOME = '/home'
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_without_source() throws Exception {
    testMissingArgument('source') {
      script.call(target: 'bar')
    }
  }

  @Test
  void test_without_target() throws Exception {
    testMissingArgument('target') {
      script.call(source: 'foo')
    }
  }

  @Test
  void test_without_secret() throws Exception {
    testMissingArgument('secret') {
      script.call(source: 'foo', target: 'bar')
    }
  }

  @Test
  void test_secret_error() throws Exception {
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
    script.call(install: false, source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString())
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'https://sdk.cloud.google.com'))
    assertFalse(assertMethodCallContainsPattern('sh', 'PATH=/home/google-cloud-sdk/bin:'))
    assertFalse(assertMethodCallContainsPattern('sh', '-h'))
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString(), headers: ['my_header'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'rm -rf /home/google-cloud-sdk'))
    assertTrue(assertMethodCallContainsPattern('sh', 'https://sdk.cloud.google.com'))
    assertTrue(assertMethodCallContainsPattern('writeJSON', 'file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', 'PATH=/home/google-cloud-sdk/bin:'))
    assertTrue(assertMethodCallContainsPattern('sh', '--key-file=service-account.json'))
    assertTrue(assertMethodCallContainsPattern('sh', '-h my_header cp foo gs://bar'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm service-account.json'))
    assertJobStatusSuccess()
  }

  @Test
  void test_without_force_install() throws Exception {
    script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString(), forceInstall: false)
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'rm -rf /home/google-cloud-sdk'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_headers() throws Exception {
    script.call(source: 'foo', target: 'gs://bar', secret: VaultSecret.SECRET_GCP.toString(), headers: ['my_header', 'my_second_header'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-h my_header -h my_second_header'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm service-account.json'))
    assertJobStatusSuccess()
  }

  @Test
  void test_error_when_pushing() throws Exception {
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
