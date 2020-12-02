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

class GoogleStorageUploadExtStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.JOB_GCS_CREDENTIALS = 'secret'
    script = loadScript('vars/googleStorageUploadExt.groovy')
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call(bucket: 'gs://foo', pattern: 'file.txt')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gsutil: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_bucket() throws Exception {
    try {
      script.call()
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'googleStorageUploadExt: bucket parameter is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_pattern() throws Exception {
    try {
      script.call(bucket: 'gs://foo')
    } catch(err) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'googleStorageUploadExt: pattern parameter is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_gsutil_error() throws Exception {
    helper.registerAllowedMethod('gsutil', [Map.class], { throw new Exception('unknown command "foo" for "gsutil"') })
    try {
      script.call(bucket: 'gs://foo', pattern: 'file.txt')
    } catch(err) {
      // NOOP
    }
    printCallStack()
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('gsutil', [Map.class], { return 'Operation completed over 1 objects.' })
    def ret = script.call(bucket: 'gs://foo', pattern: 'file.txt')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('gsutil', '-a public-read'))
    assertTrue(assertMethodCallContainsPattern('gsutil', 'file.txt gs://foo'))
    assertFalse(ret.isEmpty())
  }

  @Test
  void test_with_shared_publically() throws Exception {
    helper.registerAllowedMethod('gsutil', [Map.class], { return 'Operation completed over 1 objects.' })
    script.call(bucket: 'gs://foo', pattern: 'file.txt', sharedPublicly: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('gsutil', '-a public-read file.txt gs://foo'))
  }
}
