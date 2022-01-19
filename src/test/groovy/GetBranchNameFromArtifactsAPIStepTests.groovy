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

class GetBranchNameFromArtifactsAPIStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/getBranchNameFromArtifactsAPI.groovy')
    helper.registerAllowedMethod('artifactsApi', [Map.class], { f ->
      return [
        "7.14": ["version": "7.16.3-SNAPSHOT"],
        "7.15": ["version": "7.16.3-SNAPSHOT"],
        "7.16": ["version": "7.16.3-SNAPSHOT"],
        "7.17": ["version": "7.16.3-SNAPSHOT"],
        "7.18": ["version": "7.16.3-SNAPSHOT"],
        "8.0": ["version": "7.16.3-SNAPSHOT"]
      ]
    })
  }

  @Test
  void test_missing_argument() throws Exception {
    testMissingArgument('branch') {
      script.call()
    }
  }

  @Test
  void test_branch() throws Exception {
    def ret = script.call(branch: "8.0")

    printCallStack()
    assert ret.equals('8.0')
  }

  @Test
  void test_minor() throws Exception {
    def ret = script.call(branch: "7.<minor>")

    printCallStack()
    assert ret.equals('7.18')
  }

  @Test
  void test_minor_minus_NaN_retrieves_last() throws Exception {
    def ret = script.call(branch: "7.<minor-asdfg>")

    printCallStack()
    assert ret.equals('7.18')
  }

  @Test
  void test_minor_minus_0_retrieves_last() throws Exception {
    def ret = script.call(branch: "7.<minor-0>")

    printCallStack()
    assert ret.equals('7.18')
  }

  @Test
  void test_minor_minus_1() throws Exception {
    def ret = script.call(branch: "7.<minor-1>")

    printCallStack()
    assert ret.equals('7.17')
  }

  @Test
  void test_minor_minus_2() throws Exception {
    def ret = script.call(branch: "7.<minor-2>")

    printCallStack()
    assert ret.equals('7.16')
  }

  @Test
  void test_minor_minus_more_than_size_retrieves_first() throws Exception {
    def ret = script.call(branch: "7.<minor-500>")

    printCallStack()
    assert ret.equals('7.14')
  }
}
