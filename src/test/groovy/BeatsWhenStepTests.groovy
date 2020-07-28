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

class BeatsWhenStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/beatsWhen.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_with_no_data() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'project param is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_no_project() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(project: 'foo')
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'content param is required'))
    assertJobStatusFailure()
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

  @Test
  void test_whenComments_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenComments()
    assertFalse(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = 'branch'
    def ret = script.whenComments(content: [:])
    assertFalse(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_with_match() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = '/test foo'
    def ret = script.whenComments(content: [ comments: ['/test foo']])
    assertTrue(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_without_match() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = '/test foo'
    def ret = script.whenComments(content: [ comments: ['/run bla', '/test bar']])
    assertFalse(ret)
  }

  @Test
  void test_whenLabels_and_no_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenLabels(content: [:])
    assertFalse(ret)
  }

  @Test
  void test_whenLabels_with_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { true })
    def ret = script.whenLabels(content: [ labels: ['foo']])
    assertTrue(ret)
  }

  @Test
  void test_whenLabels_without_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { false })
    def ret = script.whenLabels(content: [ labels: ['foo']])
    assertFalse(ret)
  }

  @Test
  void test_whenParameters_and_no_params() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenParameters()
    assertFalse(ret)
  }

  @Test
  void test_whenParameters_and_params_without_match() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    assertFalse(ret)
  }

  void test_whenParameters_and_params_with_match() throws Exception {
    def script = loadScript(scriptName)
    params.bar = true
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    assertTrue(ret)
  }

  void test_whenParameters_and_params_with_match_but_disabled() throws Exception {
    def script = loadScript(scriptName)
    params.bar = false
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenTags()
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.TAG_NAME = 'tag'
    def ret = script.whenTags(content: [:])
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_environment_variable_with_data() throws Exception {
    def script = loadScript(scriptName)
    env.TAG_NAME = 'tag'
    def ret = script.whenTags(content: [ tags: true])
    assertTrue(ret)
  }
}
