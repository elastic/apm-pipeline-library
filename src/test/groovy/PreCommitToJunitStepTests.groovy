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
import org.apache.commons.io.FileUtils

class PreCommitToJunitStepTests extends ApmBasePipelineTest {
  String compareWith = 'src/test/resources/preCommitToJunit/output'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/preCommitToJunit.groovy')

    helper.registerAllowedMethod('readFile', [Map.class], { m ->
      return (new File("src/test/resources/preCommitToJunit/${m.file}")).text
    })
  }

  @Test
  void testMissingInputArgument() throws Exception {
    testMissingArgument('input') {
      script.call()
    }
  }

  @Test
  void testMissingOutputArgument() throws Exception {
    testMissingArgument('output') {
      script.call(input: 'pre-commit.txt')
    }
  }

  @Test
  void testSuccessWithSimpleCommitStages() throws Exception {
    def file = 'simple.xml'
    script.call(input: 'simple.txt', output: file, reportSkipped: true)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithAllPreCommitStages() throws Exception {
    def file = 'pre-commit.xml'
    script.call(input: 'pre-commit.txt', output: file, reportSkipped: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithSkippedPreCommitStages() throws Exception {
    def file = 'skipped.xml'
    script.call(input: 'skipped.txt', output: file, reportSkipped: true)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithGherkinDefects() throws Exception {
    def file = 'gherkin.xml'
    script.call(input: 'gherkin.txt', output: file, reportSkipped: true)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void test_null() throws Exception {
    def ret = script.toJunit('foo', null, 'bar')
    printCallStack()
    assertTrue(ret.contains('name="foo" />'))
  }

  @Test
  void test_reportSkipped_enabled() throws Exception {
    def ret = script.toJunit('foo', 'skipped', 'isortno files to check', true)
    printCallStack()
    assertTrue(ret.contains('<testcase classname="pre_commit.lint" name="foo"><skipped message="skipped"/><system-out>'))
  }

  @Test
  void test_reportSkipped_disabled() throws Exception {
    def ret = script.toJunit('foo', 'skipped', 'isortno files to check', false)
    printCallStack()
    println ret
    assertTrue(ret.equals('<testcase classname="pre_commit.lint" name="foo" />'))
  }
}
