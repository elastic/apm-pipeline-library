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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GenerateReportStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/generateReport.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.CHANGE_TARGET = 'master'
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_id_param() throws Exception {
    def script = loadScript(scriptName)
    testMissingArgument('id') {
      script.call()
    }
  }

  @Test
  void test_missing_input_param() throws Exception {
    def script = loadScript(scriptName)
    testMissingArgument('input') {
      script.call(id: 'bundlesize')
    }
  }

  @Test
  void test_no_template() throws Exception {
    def script = loadScript(scriptName)
    env.remove('CHANGE_TARGET')
    script.call(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', template: false)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('stash', 0))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', 'bundlesize'))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_compare() throws Exception {
    def script = loadScript(scriptName)
    env.remove('CHANGE_TARGET')
    script.call(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', compare: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'bundlesize.sh "bundlesize" "build" "packages/rum/reports/apm-*-report.html" ""'))
    assertTrue(assertMethodCallOccurrences('createFileFromTemplate', 1))
    assertTrue(assertMethodCallOccurrences('stash', 1))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_pull_request() throws Exception {
    def script = loadScript(scriptName)
    env.remove('CHANGE_TARGET')
    script.call(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', template: true, compare: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('copyArtifacts', 0))
    assertTrue(assertMethodCallContainsPattern('sh', '"bundlesize" "build" "packages/rum/reports/apm-*-report.html" ""'))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_pull_request() throws Exception {
    def script = loadScript(scriptName)
    script.call(id: 'bundlesize', input: 'packages/rum/reports/apm-*-report.html', template: true, compare: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('copyArtifacts', 1))
    assertTrue(assertMethodCallContainsPattern('sh', 'bundlesize.sh "bundlesize" "build" "packages/rum/reports/apm-*-report.html" "build/master/bundlesize.json"'))
    assertTrue(assertMethodCallOccurrences('archiveArtifacts', 1))    
    assertJobStatusSuccess()
  }
}
