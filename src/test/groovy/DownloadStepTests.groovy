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

class DownloadStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/download.groovy')
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('wget') })
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
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertTrue(assertMethodCallOccurrences('downloadWithWget', 1))
    assertTrue(assertMethodCallOccurrences('downloadWithCurl', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_wget() throws Exception {
    script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('downloadWithWget', 'gsutil.tar.gz'))
    assertTrue(assertMethodCallOccurrences('downloadWithWget', 1))
    assertTrue(assertMethodCallOccurrences('downloadWithCurl', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_curl() throws Exception {
    helper.registerAllowedMethod('downloadWithWget', [Map.class], { return false })
    script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'wget'))
    assertTrue(assertMethodCallOccurrences('downloadWithCurl', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_wget_and_flags() throws Exception {
    script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz', curlFlags: '--foo', wgetFlags: '--bar')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'curl'))
    assertFalse(assertMethodCallContainsPattern('sh', '--foo'))
    assertTrue(assertMethodCallContainsPattern('sh', '--bar'))
    assertTrue(assertMethodCallOccurrences('downloadWithWget', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_curl_and_flags() throws Exception {
    helper.registerAllowedMethod('downloadWithWget', [Map.class], { return false })
    helper.registerAllowedMethod('isInstalled', [Map.class], { m -> return m.tool.equals('curl') })
    script.call(url: 'https://example.acme.org', output: 'gsutil.tar.gz', curlFlags: '--foo', wgetFlags: '--bar')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', 'wget'))
    assertFalse(assertMethodCallContainsPattern('sh', '--bar'))
    assertTrue(assertMethodCallContainsPattern('sh', '--foo'))
    assertTrue(assertMethodCallOccurrences('downloadWithCurl', 1))
    assertJobStatusSuccess()
  }
}
