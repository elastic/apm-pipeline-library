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
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.is

class GetModulesFromCommentTriggerStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/getModulesFromCommentTrigger.groovy')
  }

  @Test
  void testWithoutComment() throws Exception {
    def ret = script.call()
    printCallStack()
    assertTrue(ret.isEmpty());
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentWithoutMatch() throws Exception {
    env.GITHUB_COMMENT = 'something'
    def actual = script.call()
    printCallStack()
    assertTrue(actual.isEmpty());
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentWithAListOfModules() throws Exception {
    env.GITHUB_COMMENT = 'jenkins run the tests for the module mysql,finalhandler,redis'
    def actual = script.call()
    printCallStack()
    def expected = Arrays.asList('finalhandler', 'mysql', 'redis')
    assertThat(actual, is(expected))
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentWithAListOfModulesWithSpaces() throws Exception {
    env.GITHUB_COMMENT = 'jenkins run the tests for the module mysql version2,finalhandler,redis'
    def actual = script.call()
    printCallStack()
    def expected = Arrays.asList('finalhandler', 'mysql version2', 'redis')
    assertThat(actual, is(expected))
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentWithOneModule() throws Exception {
    env.GITHUB_COMMENT = 'jenkins run the tests for the module _ALL_'
    def actual = script.call()
    printCallStack()
    def expected = Arrays.asList('_ALL_')
    assertThat(actual, is(expected))
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentAndRegex() throws Exception {
    env.GITHUB_COMMENT = 'foo,bar'
    def actual = script.call(regex: '(.+)')
    printCallStack()
    def expected = Arrays.asList('bar', 'foo')
    assertThat(actual, is(expected))
    assertJobStatusSuccess()
  }

  @Test
  void testWithCommentRegexAndDelimiter() throws Exception {
    env.GITHUB_COMMENT = 'foo bar'
    def actual = script.call(regex: '(.+)', delimiter: ' ')
    printCallStack()
    def expected = Arrays.asList('bar', 'foo')
    assertThat(actual, is(expected))
    assertJobStatusSuccess()
  }
}
