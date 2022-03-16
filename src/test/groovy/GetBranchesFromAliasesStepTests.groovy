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

class GetBranchesFromAliasesStepTests extends ApmBasePipelineTest {

  class BumpUtilsMock {
    String getCurrentMinorReleaseFor8(){ "8.0.0" }
    String getMajorMinor(String value){ "8.0" }
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/getBranchesFromAliases.groovy')
    binding.setProperty('bumpUtils', new BumpUtilsMock())
  }

  @Test
  void test_missing_argument() throws Exception {
    testMissingArgument('aliases') {
      script.call()
    }
  }

  @Test
  void test_alias_without_macro() throws Exception {
    def ret = script.call(aliases:[ 'foo' ])
    printCallStack()
    assert ret.equals(['foo'])
    assertJobStatusSuccess()
  }

  @Test
  void test_alias_with_macro() throws Exception {
    def ret = script.call(aliases:[ 'foo', '8.<minor>' ])
    printCallStack()
    assert ret.equals(['foo', '8.0'])
    assertJobStatusSuccess()
  }

  @Test
  void test_subtract_with_0() throws Exception {
    def ret = script.subtractBranchIfPossible('8.1', '1')
    printCallStack()
    assert ret.equals('8.0')
    assertJobStatusSuccess()
  }

  @Test
  void test_subtract() throws Exception {
    def ret = script.subtractBranchIfPossible('8.2', '1')
    printCallStack()
    assert ret.equals('8.1')
    assertJobStatusSuccess()
  }

  @Test
  void test_subtract_with_major_branch() throws Exception {
    def ret = script.subtractBranchIfPossible('8', '1')
    printCallStack()
    assert ret.equals('8')
    assertJobStatusSuccess()
  }

  @Test
  void test_subtract_with_major_branch_overflow() throws Exception {
    def ret = script.subtractBranchIfPossible('8.2', '5')
    printCallStack()
    assert ret.equals('8.2')
    assertJobStatusSuccess()
  }
}
