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

  def apiInterceptor = { return toJSON([
      "name": "master.gradle",
      "path": "cd/release/release-manager/master.gradle",
      "size": "1",
      "type": "file"
    ])
  }

  def apiErrorInterceptor = { return toJSON([
      "message": "java.lang.Exception",
      "Error": "Not Found",
      "Code": "404"
    ])
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isBranchUnifiedReleaseAvailable.groovy')
    helper.registerAllowedMethod("githubApiCall", [Map.class], apiInterceptor)
  }

  @Test
  void test_isMainBranch() throws Exception {
    def ret = script.call('main')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isFoo_branch() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], apiErrorInterceptor)
    def ret = script.call('foo')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
