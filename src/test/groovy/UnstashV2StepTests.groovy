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
import static org.junit.Assert.assertTrue

class UnstashV2StepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/unstashV2.groovy')
    helper.registerAllowedMethod('cmd', [Map.class], { m -> 0 })
  }

  @Test
  void test_without_name_param() throws Exception {
    testMissingArgument('name') {
      script.call()
    }
  }

  @Test
  void test_without_bucket_param_without_env_variable() throws Exception {
    testMissingArgument('bucket', 'parameter is required or JOB_GCS_BUCKET env variable') {
      script.call(name: 'source')
    }
  }

  @Test
  void test_without_credentialsId_param_without_env_variable() throws Exception {
    testMissingArgument('credentialsId', 'parameter is required or JOB_GCS_CREDENTIALS env variable') {
      script.call(name: 'source', bucket: 'foo')
    }
  }

  @Test
  void test_bucket_precedence() throws Exception {
    env.JOB_GCS_BUCKET = 'bar'
    script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'unstashV2: JOB_GCS_BUCKET is set. bucket param got precedency instead.'))
    assertTrue(assertMethodCallOccurrences('untar', 1))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'bucketUri=gs://foo'))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'credentialsId=secret'))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'source/source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_credentialsId_precedence() throws Exception {
    env.JOB_GCS_CREDENTIALS = 'bar'
    script.call(name: 'source', bucket: 'foo', credentialsId: 'my-super-credentials')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'unstashV2: JOB_GCS_CREDENTIALS is set. credentialsId param got precedency instead.'))
    assertTrue(assertMethodCallOccurrences('untar', 1))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'bucketUri=gs://foo'))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'credentialsId=my-super-credentials'))
    assertTrue(assertMethodCallContainsPattern('googleStorageDownload', 'source/source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_linux_with_parameters() throws Exception {
    script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'got precedency instead.'))
    assertTrue(assertMethodCallOccurrences('untar', 1))
    assertNull(assertMethodCall('bat'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows_with_parameters() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'got precedency instead.'))
    assertTrue(assertMethodCallOccurrences('untar', 1))
    assertNull(assertMethodCall('sh'))
    assertJobStatusSuccess()
  }

  @Test
  void test_missing_build_context() throws Exception {
    env.remove('BUILD_ID')
    try {
      script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'BUILD_ID and JOB_NAME environment variables are required.'))
    assertJobStatusFailure()
  }
}
