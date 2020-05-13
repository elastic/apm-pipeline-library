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

class StashV2StepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/stashV2.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_without_name_param() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'stashV2: name param is required.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_bucket_param_without_env_variable() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(name: 'source')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'stashV2: bucket param is required or JOB_GCS_BUCKET env variable.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_credentialsId_param_without_env_variable() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(name: 'source', bucket: 'foo')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'stashV2: credentialsId param is required or JOB_GCS_CREDENTIALS env variable.'))
    assertJobStatusFailure()
  }

  @Test
  void test_bucket_precedence() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_GCS_BUCKET = 'bar'
    script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'stashV2: JOB_GCS_BUCKET is set. bucket param got precedency instead.'))
    assertTrue(assertMethodCallContainsPattern('sh', 'tar -czf .artefacts/source.tgz'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'bucket=gs://foo'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'credentialsId=secret'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'pattern=source.tgz'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_credentialsId_precedence() throws Exception {
    def script = loadScript(scriptName)
    env.JOB_GCS_CREDENTIALS = 'bar'
    script.call(name: 'source', bucket: 'foo', credentialsId: 'my-super-credentials')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'stashV2: JOB_GCS_CREDENTIALS is set. credentialsId param got precedency instead.'))
    assertTrue(assertMethodCallContainsPattern('sh', 'tar -czf .artefacts/source.tgz'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'bucket=gs://foo'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'credentialsId=my-super-credentials'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUpload', 'pattern=source.tgz'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_linux_with_parameters() throws Exception {
    def script = loadScript(scriptName)
    def bucketUri = script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'got precedency instead.'))
    assertNull(assertMethodCall('bat'))
    assertTrue(assertMethodCallContainsPattern('dir', '.artefacts'))
    assertTrue(assertMethodCallContainsPattern('sh', 'tar -czf .artefacts/source.tgz --exclude=.artefacts . ; mv .artefacts/source.tgz source.tgz'))
    assertTrue(assertMethodCallContainsPattern('sh', 'rm source.tgz'))
    assertTrue(bucketUri.contains('source/source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows_with_parameters() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(name: 'source', bucket: 'foo', credentialsId: 'secret')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'got precedency instead.'))
    assertNull(assertMethodCall('sh'))
    assertTrue(assertMethodCallContainsPattern('bat', 'tar -czf .artefacts\\source.tgz --exclude=.artefacts . && move .artefacts\\source.tgz source.tgz'))
    assertTrue(assertMethodCallContainsPattern('bat', 'del source.tgz'))
    assertJobStatusSuccess()
  }

  @Test
  void test_missing_build_context() throws Exception {
    def script = loadScript(scriptName)
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
