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

class IsBranchUnifiedReleaseAvailableStepTests extends ApmBasePipelineTest {

  def apiInterceptor = [
      "name": "build.gradle",
      "path": "cd/release/release-manager/project-configs/master/build.gradle",
      "size": "1",
      "type": "file",
      "content": ""
    ]

  def apiInterceptorCurrentReleases = [
      "name": "current-release-branches-main.yml.inc",
      "path": "ci/jjb/shared/current-release-branches-main.yml.inc",
      "size": "1",
      "type": "file",
      "content": "bWFpbgo="
    ]

  def apiInterceptorAsyncReleases = [
      "name": "current-async-release-branches.yml.inc",
      "path": "ci/jjb/shared/current-async-release-branches.yml.inc",
      "size": "1",
      "type": "file",
      "content": "bWFpbgo="
    ]

  def apiErrorInterceptor = [
      "message": "java.lang.Exception",
      "Error": "Not Found",
      "Code": "404"
    ]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isBranchUnifiedReleaseAvailable.groovy')
    helper.registerAllowedMethod("githubApiCall", [Map.class], { m  ->
      if (m.url.contains('current-release-branches-main.yml.inc')) {
        return toJSON(apiInterceptorCurrentReleases)
      }
      if (m.url.contains('current-async-release-branches.yml.inc')) {
        return toJSON(apiInterceptorAsyncReleases)
      } else {
        return toJSON(apiInterceptor)
      }
    })
    helper.registerAllowedMethod('base64encode', [Map.class], { return 'main' })
  }

  @Test
  void test_isMainBranch() throws Exception {
    def ret = script.call('main')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isMainBranch_with_fallback() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], { m  ->
      if (m.url.contains('current-async-release-branches.yml.inc')) {
        return toJSON(apiInterceptorAsyncReleases)
      }
      return toJSON(apiErrorInterceptor)
    })
    def ret = script.call('main')
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallOccurrences('githubApiCall', 3))
    assertJobStatusSuccess()
  }

  @Test
  void test_isFoo_branch() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], { return toJSON(apiErrorInterceptor)})
    def ret = script.call('foo')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_fallback_with_main_branch() throws Exception {
    def ret = script.fallback('current-release-branches-main.yml.inc', 'main', 'my-token')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_fallback_with_foo_branch() throws Exception {
    def ret = script.fallback('current-release-branches-main.yml.inc', 'foo', 'my-token')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
