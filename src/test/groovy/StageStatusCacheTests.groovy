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
import static org.junit.Assert.assertFalse

class StageStatusCacheTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/stageStatusCache.groovy')
    env.GIT_BASE_COMMIT = "29480a51"
    env.STAGE_ID = 'fooo'
    env.FILE_NAME_BASE64 = 'Zm9vbzI5NDgwYTUxCg'
    env.BUILD_ID = "10"
    helper.registerAllowedMethod('googleStorageUploadExt', [Map.class], { "OK" })
    helper.registerAllowedMethod('isUserTrigger', { false })
    helper.registerAllowedMethod('base64encode', [Map.class], { env.FILE_NAME_BASE64 })
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    def isOK = false
    script.call(id: env.STAGE_ID){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertTrue(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertTrue(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "pattern=${env.FILE_NAME_BASE64}"))
    assertJobStatusSuccess()
  }

  @Test
  void testCache() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    def isOK = true
    script.call(id: env.STAGE_ID){
      isOK = false
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertTrue(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertTrue(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('log', "The stage 'fooo' is skipped because it is in the execution cache."))
    assertJobStatusSuccess()
  }

  @Test
  void testCheckNoCacheOnFirstBuild() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    env.BUILD_ID = "1"
    def isOK = false
    script.call(id: env.STAGE_ID){
      isOK = true
    }
    printCallStack()
    //assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertTrue(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertTrue(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "pattern=${env.FILE_NAME_BASE64}"))
    assertJobStatusSuccess()
  }

  @Test
  void testCheckCacheOnFirstBuildIfRetryOption() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    env.BUILD_ID = "1"
    def isOK = true
    script.call(id: env.STAGE_ID){
      isOK = false
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertTrue(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertTrue(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('log', "The stage 'fooo' is skipped because it is in the execution cache."))
    assertJobStatusSuccess()
  }

  @Test
  void testNoCheckCacheOnRunAlways() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    def isOK = false
    script.call(id: env.STAGE_ID, runAlways: true){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertFalse(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertFalse(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "pattern=${env.FILE_NAME_BASE64}"))
    assertJobStatusSuccess()
  }

  @Test
  void testNoCheckCacheOnUserTrigger() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    helper.registerAllowedMethod('isUserTrigger', { true })
    def isOK = false
    script.call(id: env.STAGE_ID){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "${env.STAGE_ID}${env.GIT_BASE_COMMIT}"))
    assertFalse(assertMethodCallContainsPattern('cmd', 'Download Stage Status'))
    assertFalse(assertMethodCallContainsPattern('fileExists', "${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "pattern=${env.FILE_NAME_BASE64}"))
    assertJobStatusSuccess()
  }


  @Test
  void testParams() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    helper.registerAllowedMethod('isUserTrigger', { true })
    env.FILE_NAME_BASE64 = 'Zm9vb2Zvb1NIQQo'
    def isOK = false
    script.call(id: env.STAGE_ID,
      bucket: 'bucketFoo',
      credentialsId: 'fooCredentials',
      sha: 'fooSHA'){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('base64encode', "fooofooSHA"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "pattern=${env.FILE_NAME_BASE64}"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "bucket=gs://bucketFoo/ci/cache/"))
    assertTrue(assertMethodCallContainsPattern('googleStorageUploadExt', "credentialsId=fooCredentials"))
    assertJobStatusSuccess()
  }
}
