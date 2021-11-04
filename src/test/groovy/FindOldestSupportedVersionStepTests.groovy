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

class FindOldestSupportedVersionStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/findOldestSupportedVersion.groovy')
    helper.registerAllowedMethod('httpRequest', [Map.class], { f ->
      return """{
        "versions": [
          "7.14.0-SNAPSHOT",
          "7.14.0",
          "7.14.1-SNAPSHOT",
          "7.15.0-SNAPSHOT",
          "7.15.0",
          "8.0.0-SNAPSHOT"
        ],
        "aliases": [
          "7.x-SNAPSHOT",
          "7.14-SNAPSHOT",
          "7.14",
          "7.15-SNAPSHOT",
          "7.15",
          "8.0-SNAPSHOT",
          "8.0"
        ]
      }"""
    })
  }

  @Test
  void test_missing_argument() throws Exception {
    testMissingArgument('versionCondition') {
      script.call()
    }
  }

  @Test
  void test_match() throws Exception {
    def ret = script.call(versionCondition: "^7.14.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_match_ge() throws Exception {
    def ret = script.call(versionCondition: ">=7.14.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_match_minor() throws Exception {
    def ret = script.call(versionCondition: "~7.14.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_snapshot() throws Exception {
    def ret = script.call(versionCondition: "^7.14.1")
    printCallStack()
    assert ret.equals('7.14.1-SNAPSHOT')
  }

  @Test
  void test_no_match() throws Exception {
    def ret = script.call(versionCondition: "^7.13.0")
    printCallStack()
    assert ret.equals('7.13.0')
  }

  @Test
  void test_unsupported_versionCondition() throws Exception {
    try {
      script.call(versionCondition: "<7.13.0")
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assert assertMethodCallContainsPattern('error', 'versionCondition supports only')
  }

  @Test
  void test_release_in_process() throws Exception {
    // There are already 7.15.0 artifacts, but this release hasn't happened yet, and there are only snapshots for the docker images.
    def ret = script.call(versionCondition: "^7.15.0")
    printCallStack()
    assert ret.equals('7.15.0-SNAPSHOT')
  }

  @Test
  void test_without_patch() throws Exception {
    def ret = script.call(versionCondition: "^7.14")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_next_minor() throws Exception {
    def ret = script.call(versionCondition: "^7.16.0")
    printCallStack()
    assert ret.equals('7.x-SNAPSHOT')
  }

  @Test
  void test_or_condition() throws Exception {
    def ret = script.call(versionCondition: "^7.14.0 || ^8.0.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_or_condition_for_minor() throws Exception {
    def ret = script.call(versionCondition: "^7.14.0 || ~8.0.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }

  @Test
  void test_or_condition_reversed() throws Exception {
    def ret = script.call(versionCondition: "^8.0.0 || ^7.14.0")
    printCallStack()
    assert ret.equals('7.14.0')
  }
}
