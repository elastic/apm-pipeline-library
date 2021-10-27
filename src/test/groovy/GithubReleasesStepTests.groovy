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

class GithubReleasesStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubReleases.groovy')
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_with_no_data() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], { return '' })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_gh_error() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], { throw new Exception('unknown command "foo" for "gh release"') })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty())
  }

  @Test
  void test_with_single_row() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return 'v1.1.256  Latest  (v1.1.256)  about 21 hours ago' })
    def ret = script.call()
    printCallStack()
    assertTrue(ret.size() == 1)
  }

  @Test
  void test_with_multiple_data() throws Exception {
    helper.registerAllowedMethod('gh', [Map.class], {
      return '''v1.1.256  Latest  (v1.1.256)  about 21 hours ago
v1.1.255          (v1.1.255)  about 5 days ago
v1.1.254          (v1.1.254)  about 6 days ago'''})
    def ret = script.call()
    printCallStack()
    assertTrue(ret.size() == 3)
  }
}
