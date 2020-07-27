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
import org.junit.After
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class BeatsStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/beats.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_whenBranches_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenBranches()
    assertFalse(ret)
  }

  @Test
  void test_whenBranches_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'branch'
    def ret = script.whenBranches(content: [:])
    assertFalse(ret)
  }

  @Test
  void test_whenBranches_and_environment_variable_with_data() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'branch'
    def ret = script.whenBranches(content: [ branches: true])
    assertTrue(ret)
  }
}
