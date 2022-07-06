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

class DownloadWithCurlStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script = loadScript('vars/downloadWithCurl.groovy')
  }

  @Test
  void test_missing_url() throws Exception {
    testMissingArgument('url', 'parameter is required') {
      script.call()
    }
  }

  @Test
  void test_missing_output() throws Exception {
    testMissingArgument('output', 'parameter is required') {
      script.call(url: 'foo')
    }
  }

  @Test
  void test_without_curl_installed() throws Exception {
    helper.registerAllowedMethod('isInstalled', [Map.class], { return false })
    def result = script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'curl -sSLo gsutil.tar.gz'))
    assertFalse(result)
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    def result = script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'curl -sSLo gsutil.tar.gz'))
    assertTrue(assertMethodCallContainsPattern('sh', 'https://example.acme.org'))
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_flags() throws Exception {
    def result = script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz', flags: '--parallel')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'curl -sSLo gsutil.tar.gz'))
    assertTrue(assertMethodCallContainsPattern('sh', '--parallel'))
    assertTrue(assertMethodCallContainsPattern('sh', 'https://example.acme.org'))
    assertTrue(result)
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    def result = script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'curl -sSLo gsutil.tar.gz'))
    assertTrue(result)
    assertJobStatusSuccess()
  }
}
