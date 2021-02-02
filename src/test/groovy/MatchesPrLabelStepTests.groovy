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

class MatchesPrLabelStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/matchesPrLabel.groovy')
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
    helper.registerAllowedMethod('githubPrLabels', [], { return [ 'bar', 'foo' ] })
  }

  @Test
  void test_missing_label_parameter() throws Exception {
    testMissingArgument('label') {
      script.call()
    }
  }

  @Test
  void test_branch() throws Exception {
    def ret = script.call(label: 'foo')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_pr_without_match() throws Exception {
    env.CHANGE_ID = 1
    def ret = script.call(label: 'unmatch')
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_pr_with_match() throws Exception {
    env.CHANGE_ID = 1
    def ret = script.call(label: 'foo')
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }
}
