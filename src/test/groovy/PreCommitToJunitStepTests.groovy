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
  String scriptName = 'vars/preCommitToJunit.groovy'
  String compareWith = 'src/test/resources/preCommitToJunit/output'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    helper.registerAllowedMethod('readFile', [Map.class], { m ->
      return (new File("src/test/resources/preCommitToJunit/${m.file}")).text
    })
  }

  @Test
  void testMissingInputArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'preCommitToJunit: input parameter is required.'))
    assertJobStatusFailure()
  }

  @Test
  void testMissingOutputArgument() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(input: 'pre-commit.txt')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'preCommitToJunit: output parameter is required.'))
    assertJobStatusFailure()
  }

  @Test
  void testSuccessWithSimpleCommitStages() throws Exception {
    def script = loadScript(scriptName)
    def file = 'simple.xml'
    script.call(input: 'simple.txt', output: file)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithAllPreCommitStages() throws Exception {
    def script = loadScript(scriptName)
    def file = 'pre-commit.xml'
    script.call(input: 'pre-commit.txt', output: file)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithSkippedPreCommitStages() throws Exception {
    def script = loadScript(scriptName)
    def file = 'skipped.xml'
    script.call(input: 'skipped.txt', output: file)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }

  @Test
  void testSuccessWithGherkinDefects() throws Exception {
    def script = loadScript(scriptName)
    def file = 'gherkin.xml'
    script.call(input: 'gherkin.txt', output: file)
    printCallStack()
    assertTrue("The files differ!", FileUtils.contentEqualsIgnoreEOL(
                                      new File("${compareWith}/${file}"),
                                      new File("target/${file}"), 'UTF-8'))
    assertJobStatusSuccess()
  }
}
