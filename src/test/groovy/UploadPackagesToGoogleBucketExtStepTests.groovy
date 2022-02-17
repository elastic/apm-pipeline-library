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

class UploadPackagesToGoogleBucketStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.GIT_BASE_COMMIT = 'abdefg'
    env.CHANGE_ID = '1'
    script = loadScript('vars/uploadPackagesToGoogleBucket.groovy')
  }

  @Test
  void test_without_credentials() throws Exception {
    env.remove('JOB_GCS_CREDENTIALS')
    testMissingArgument('credentialsId') {
      script.call()
    }
  }

  @Test
  void test_without_bucket() throws Exception {
    env.remove('JOB_GCS_BUCKET')
    testMissingArgument('bucket') {
      script.call(credentialsId: 'secret', repo: 'my-repo')
    }
  }

  @Test
  void test_without_repo() throws Exception {
    env.remove('REPO')
    testMissingArgument('repo') {
      script.call(bucket: 'foo', credentialsId: 'secret')
    }
  }

  @Test
  void test_branch() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    script.call(bucket: 'foo', pattern: 'file.txt', repo: 'my-repo', credentialsId: 'secret')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'credentialsId=secret, pattern=file.txt'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://foo/my-repo/snapshots'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://foo/my-repo/commits/abdefg'))
    assertFalse(assertMethodCallContainsPattern('googleStorageUploadExt', 'pr-'))
  }

  @Test
  void test_pr() throws Exception {
    helper.registerAllowedMethod('isPR', { return true })
    script.call(bucket: 'foo', pattern: 'file.txt', repo: 'my-repo', credentialsId: 'secret')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'credentialsId=secret, pattern=file.txt'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://foo/my-repo/pull-requests/pr-1'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://foo/my-repo/commits/abdefg'))
    assertFalse(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://foo/my-repo/snapshots'))
  }

  @Test
  void test_defaults() throws Exception {
    addEnvVar('JOB_GCS_CREDENTIALS', 'my-secret')
    addEnvVar('REPO', 'my-repository')
    addEnvVar('JOB_GCS_BUCKET', 'my-bucket')
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'credentialsId=my-secret'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'pattern=build/distributions/**/*'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://my-bucket/my-repository'))
  }

  @Test
  void test_defaults_with_folder() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    addEnvVar('JOB_GCS_CREDENTIALS', 'my-secret')
    addEnvVar('REPO', 'my-repository')
    addEnvVar('JOB_GCS_BUCKET', 'my-bucket')
    script.call(folder: 'new-folder')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://my-bucket/my-repository/snapshots/new-folder'))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', 'bucket=gs://my-bucket/my-repository/commits/abdefg/new-folder'))
  }
}
