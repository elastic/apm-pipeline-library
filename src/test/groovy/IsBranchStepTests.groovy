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

import co.elastic.mock.RawBuildMock
import hudson.model.Cause
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class IsBranchStepTests extends ApmBasePipelineTest {


  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isBranch.groovy')
  }

  @Test
  void test_isBranch() throws Exception {
    env.BRANCH_NAME = 'master'
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isBranch_in_pr() throws Exception {
    env.BRANCH_NAME = 'PR-1'
    env.CHANGE_ID = '1'
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_isBranch_in_tag() throws Exception {
    env.TAG_NAME = 'my-tag'
    env.remove('BRANCH_NAME')
    env.remove('CHANGE_ID')
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
