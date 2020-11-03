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
import co.elastic.mock.beats.GetProjectDependencies

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
  void test_with_description() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(project: 'foo', description: 'bar', content: [:])
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('writeFile', 'Stages for `foo bar`'))
  }

  @Test
  void test_whenBranches_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenBranches()
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenBranches_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'branch'
    def ret = script.whenBranches(content: [:])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenBranches_and_environment_variable_with_data() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'branch'
    def ret = script.whenBranches(content: [ branches: true])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenBranches_and_environment_variable_with_data_and_prs() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'branch'
    env.CHANGE_ID = 'PR-1'
    def ret = script.whenBranches(content: [ branches: true])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_and_no_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenChangeset()
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_and_content() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^.ci']])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_and_content_with_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^Jenkinsfile']])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenChangeset_content_and_macro() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenChangeset(content: [ changeset: ['^.ci', '@oss']],
                                   changeset: [ oss: [ '^oss'] ])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_content_and_macro_with_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'oss'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^.ci', '@oss']],
                                   changeset: [ oss: [ '^oss'] ])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenChangeset_content_and_macro_without_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'oss'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^.ci', '@osss']],
                                   changeset: [ oss: [ '^oss'] ])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_content_and_function_with_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'projectA/Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^Jenkinsfile']],
                                   changesetFunction: new GetProjectDependencies())
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenChangeset_content_with_project_dependency_and_function_with_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'projectA/Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['#generator/common/beatgen']],
                                   changesetFunction: new GetProjectDependencies())
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenChangeset_content_and_function_without_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^Jenkinsfile']],
                                   changesetFunction: new GetProjectDependencies())
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenChangeset_branch_first_build() throws Exception {
    env.remove('GIT_PREVIOUS_COMMIT')
    env.remove('CHANGE_TARGET')
    env.GIT_BASE_COMMIT = 'bar'
    def script = loadScript(scriptName)
    def changeset = 'Jenkinsfile'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def ret = script.whenChangeset(content: [ changeset: ['^Jenkinsfile']])
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallContainsPattern('sh', 'bar...bar'))
  }

  @Test
  void test_whenComments_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenComments()
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = 'branch'
    def ret = script.whenComments(content: [:])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_with_match() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = '/test foo'
    def ret = script.whenComments(content: [ comments: ['/test foo']])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenComments_and_environment_variable_without_match() throws Exception {
    def script = loadScript(scriptName)
    env.GITHUB_COMMENT = '/test foo'
    def ret = script.whenComments(content: [ comments: ['/run bla', '/test bar']])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenEnabled_without_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenEnabled()
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenEnabled_with_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenEnabled(content: [:])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenEnabled_with_disabled() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenEnabled(content: [ disabled: true])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenEnabled_with_no_disabled() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenEnabled(content: [ disabled: false])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenLabels_and_no_data() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenLabels(content: [:])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenLabels_with_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { true })
    def ret = script.whenLabels(content: [ labels: ['foo']])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_whenLabels_without_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { false })
    def ret = script.whenLabels(content: [ labels: ['foo']])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenParameters_and_no_params() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenParameters()
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenParameters_and_params_without_match() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    printCallStack()
    assertFalse(ret)
  }

  void test_whenParameters_and_params_with_match() throws Exception {
    def script = loadScript(scriptName)
    params.bar = true
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    printCallStack()
    assertTrue(ret)
  }

  void test_whenParameters_and_params_with_match_but_disabled() throws Exception {
    def script = loadScript(scriptName)
    params.bar = false
    def ret = script.whenParameters(content: [ parameters : [ 'foo', 'bar']])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_no_environment_variable() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.whenTags()
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_environment_variable_but_no_data() throws Exception {
    def script = loadScript(scriptName)
    env.TAG_NAME = 'tag'
    def ret = script.whenTags(content: [:])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_whenTags_and_environment_variable_with_data() throws Exception {
    def script = loadScript(scriptName)
    env.TAG_NAME = 'tag'
    def ret = script.whenTags(content: [ tags: true])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_isSkipCiBuildLabel_without_content() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { false })
    def ret = script.isSkipCiBuildLabel(content: [:])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_isSkipCiBuildLabel_with_label_enabled_and_pr_without_label_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { false })
    def ret = script.isSkipCiBuildLabel(content: [ 'skip-ci-build-label': true ])
    printCallStack()
    assertFalse(ret)
  }

  @Test
  void test_isSkipCiBuildLabel_with_label_enabled_and_pr_with_label_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('matchesPrLabel', [Map.class], { true })
    def ret = script.isSkipCiBuildLabel(content: [ 'skip-ci-build-label': true ])
    printCallStack()
    assertTrue(ret)
  }

  @Test
  void test_isSkipCiBuildLabel_with_label_disabled() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.isSkipCiBuildLabel(content: [ 'skip-ci-build-label': false ])
    printCallStack()
    assertFalse(ret)
  }
}
