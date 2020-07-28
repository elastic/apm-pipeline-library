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

class BeatsStagesStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/beatsStages.groovy'

  def runCommand(Map args = [:]) {
    echo "${args.label}"
  }

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
  void test_with_no_platform() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(project: 'foo', content: [:], function: this.&runCommand)
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'platform entry in the content is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_no_function() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call(project: 'foo', content: [:])
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'function param is required'))
    assertJobStatusFailure()
  }

  @Test
  void test_simple() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(project: 'foo', content: [
      "platform" : [ "linux && ubuntu-16" ],
      "stages": [
        "simple" : [
          "mage" : [ "foo" ]
        ]
      ]
    ], function: this.&runCommand)
    printCallStack()
    assertTrue(ret.size() == 1)
    assertTrue(assertMethodCallContainsPattern('log', 'stage: foo-simple'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple() throws Exception {
    def script = loadScript(scriptName)
    def ret = script.call(project: 'foo', content: [
      "platform" : [ "linux && ubuntu-16" ],
      "stages": [
        "simple" : [
          "make" : [ "foo" ]
        ],
        "multi" : [
          "mage" : [ "foo" ],
          "platforms" : [ 'windows-2019', 'windows-2016' ]
        ]
      ]
    ], function: this.&runCommand)
    printCallStack()
    assertTrue(ret.size() == 3)
    assertTrue(assertMethodCallContainsPattern('log', 'stage: foo-simple'))
    assertTrue(assertMethodCallContainsPattern('log', 'stage: foo-multi-windows-2019'))
    assertTrue(assertMethodCallContainsPattern('log', 'stage: foo-multi-windows-2016'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_when_without_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('beatsWhen', [Map.class], {return false})
    def ret = script.call(project: 'foo', content: [
      "platform" : [ "linux && ubuntu-16" ],
      "stages": [
        "simple" : [
          "make" : [ "foo" ]
        ],
        "multi" : [
          "make" : [ "foo" ],
          "platforms" : [ 'windows-2019' ]
        ],
        "multi-when" : [
          "mage" : [ "foo" ],
          "platforms" : [ 'windows-2016' ],
          "when" : [ 
            "comments" : [ "/test auditbeat for windows" ]
          ]
        ]
      ]
    ], function: this.&runCommand)
    printCallStack()
    assertTrue(ret.size() == 2)
    assertFalse(assertMethodCallContainsPattern('log', 'stage: foo-multi-when'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_when_with_match() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('beatsWhen', [Map.class], { return true })
    def ret = script.call(project: 'foo', content: [
      "platform" : [ "linux && ubuntu-16" ],
      "stages": [
        "simple" : [
          "make" : [ "foo" ]
        ],
        "multi" : [
          "mage" : [ "foo" ],
          "platforms" : [ 'windows-2019' ]
        ],
        "multi-when" : [
          "mage" : [ "foo" ],
          "platforms" : [ 'windows-2016' ],
          "when" : [ 
            "comments" : [ "/test auditbeat for windows" ]
          ]
        ]
      ]
    ], function: this.&runCommand)
    printCallStack()
    assertTrue(ret.size() == 3)
    assertTrue(assertMethodCallContainsPattern('log', 'stage: foo-multi-when'))
    assertJobStatusSuccess()
  }
}
