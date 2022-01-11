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
import static org.junit.Assert.assertEquals

class BumpUtilsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/bumpUtils.groovy')
  }

  @Test
  void test_isVersionAvailable_release() throws Exception {
    def result = script.isVersionAvailable('8.0.0')
    printCallStack()
    assertTrue(result == result)
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '8.0.0-SNAPSHOT'))
  }

  @Test
  void test_isVersionAvailable_snapshot() throws Exception {
    def result = script.isVersionAvailable('8.0.0-SNAPSHOT')
    printCallStack()
    assertTrue(result == result)
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '8.0.0-SNAPSHOT'))
  }

  @Test
  void test_areStackVersionsAvailable() throws Exception {
    script.areStackVersionsAvailable([ current_7 : '7.15.1', current_6: '6.8.20', next_minor_7: '7.16.0', next_patch_7: '7.15.2' ])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '6.8.20-SNAPSHOT'))
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '7.15.1-SNAPSHOT'))
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '7.15.2-SNAPSHOT'))
    assertTrue(assertMethodCallContainsPattern('dockerImageExists', '7.16.0-SNAPSHOT'))
  }

  @Test
  void test_createBranch() throws Exception {
    def result = script.createBranch(prefix: 'foo', suffix: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-b "foo-'))
    assertTrue(assertMethodCallContainsPattern('sh', '-bar"'))
  }

  @Test
  void test_areChangesToBePushed() throws Exception {
    def result = script.areChangesToBePushed('my-branch')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'HEAD..my-branch'))
  }

  @Test
  void test_prepareContext() throws Exception {
    def result = script.prepareContext(org: 'my-org', repo: 'my-repo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('git', 'my-org/my-repo'))
  }

  @Test
  void test_prepareContext_with_credentialsId() throws Exception {
    def result = script.prepareContext(org: 'my-org', repo: 'my-repo', credentialsId: 'my-creds')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('git', 'my-org/my-repo'))
    assertTrue(assertMethodCallContainsPattern('git', 'my-creds'))
  }

  @Test
  void test_getCurrentMinorReleaseFor7() throws Exception {
    helper.registerAllowedMethod('readProperties', [Map.class], { [ current_7 : 'value' ] })
    def result = script.getCurrentMinorReleaseFor7()
    printCallStack()
    assertTrue(result.equals('value'))
  }

  @Test
  void test_getCurrentMinorReleaseFor6() throws Exception {
    helper.registerAllowedMethod('readProperties', [Map.class], { [ current_6 : 'value' ] })
    def result = script.getCurrentMinorReleaseFor6()
    printCallStack()
    assertTrue(result.equals('value'))
  }

  @Test
  void test_getNextMinorReleaseFor7() throws Exception {
    helper.registerAllowedMethod('readProperties', [Map.class], { [ next_minor_7 : 'value' ] })
    def result = script.getNextMinorReleaseFor7()
    printCallStack()
    assertTrue(result.equals('value'))
  }

  @Test
  void test_getNextPatchReleaseFor7() throws Exception {
    helper.registerAllowedMethod('readProperties', [Map.class], { [ next_patch_7 : 'value' ] })
    def result = script.getNextPatchReleaseFor7()
    printCallStack()
    assertTrue(result.equals('value'))
  }

  @Test
  void test_getValueForPropertyKey() throws Exception {
    def result
    try {
      result = script.getValueForPropertyKey('key')
    } catch(e) {
      // NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
  }

  @Test
  void test_parseArguments() throws Exception {
    def result = script.parseArguments([title: "my-title", labels: "my-label"])
    printCallStack()
    assertTrue(result.containsKey('title'))
    assertFalse(result.containsKey('assign'))
    result = script.parseArguments([title: "my-title", labels: "my-label", assign: 'my-assign'])
    printCallStack()
    assertTrue(result.containsKey('assign'))
  }

  @Test
  void test_getMajorMinorFor7() throws Exception {
    def result = script.getMajorMinor('7.15.0')
    printCallStack()
    println result
    assertEquals(result, "7.15")
  }

  @Test
  void test_getMajorFor7() throws Exception {
    def result = script.getMajor('7.15.0')
    printCallStack()
    assertEquals(result, "7")
  }

  @Test
  void test_getMajorMinorFor7_with_wrong_format() throws Exception {
    try {
      result = script.getMajorMinor('7.15')
    } catch(e) {
      // NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
  }

  @Test
  void test_getMajorMinor_with_empty_value() throws Exception {
    try {
      result = script.getMajorMinor('')
    } catch(e) {
      // NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 1))
  }

}
