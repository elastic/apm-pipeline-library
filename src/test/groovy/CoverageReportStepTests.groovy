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

class CoverageReportStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/coverageReport.groovy')
    // mock nested structure in the publishCoverage
    helper.registerAllowedMethod('coberturaAdapter', [String.class], { s -> return s })
    helper.registerAllowedMethod('sourceFiles', [String.class], { s -> return s })
  }

  @Test
  void test() throws Exception {
    script.call(baseDir: "foo", reportFiles: 'file.html', coverageFiles: 'file.xml')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('publishHTML', 'reportDir=foo, reportFiles=file.html'))
    assertTrue(assertMethodCallContainsPattern('cobertura', 'coberturaReportFile=foo/file.xml'))
    assertTrue(assertMethodCallContainsPattern('publishCoverage', 'foo/file.xml'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_string_signature() throws Exception {
    script.call("bar")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('publishHTML', 'reportDir=bar, reportFiles=coverage-*-report.html'))
    assertTrue(assertMethodCallContainsPattern('cobertura', 'coberturaReportFile=bar/coverage-*-report.xml'))
    assertTrue(assertMethodCallContainsPattern('publishCoverage', 'bar/coverage-*-report.xml'))
    assertJobStatusSuccess()
  }

  @Test
  void test_no_baseDir() throws Exception {
    testMissingArgument('baseDir') {
      script.call()
    }
  }

  @Test
  void test_no_reportFiles() throws Exception {
    testMissingArgument('reportFiles') {
      script.call(baseDir: 'foo')
    }
  }

  @Test
  void test_no_coverageFiles() throws Exception {
    testMissingArgument('coverageFiles') {
      script.call(baseDir: 'foo', reportFiles: 'file.html')
    }
  }

}
