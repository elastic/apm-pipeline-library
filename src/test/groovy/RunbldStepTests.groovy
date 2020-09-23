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

class RunbldStepTests extends ApmBasePipelineTest {

  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/runbld.groovy')
    env.BASE_DIR = 'src'
    helper.registerAllowedMethod('isPR', [], { true })
  }

  @Test
  void test_no_project() throws Exception {
    try {
      script.call()
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'project param'))
    assertJobStatusFailure()
  }

  @Test
  void test_no_stashedTestReports_parameter() throws Exception {
    try {
      script.call(project: 'acme')
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'stashedTestReports param'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_empty_stashed_reports() throws Exception {
    script.call(stashedTestReports: [:], project: 'acme')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('sh', '--job-name'))
    assertTrue(assertMethodCallContainsPattern('log', 'stashedTestReports is empty'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_pr() throws Exception {
    script.call(stashedTestReports: ['foo': 'bar'], project: 'acme')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '--job-name elastic+acme+pull-request'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_branch() throws Exception {
    helper.registerAllowedMethod('isPR', [], { false })
    script.call(stashedTestReports: ['foo': 'bar'], project: 'acme')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '--job-name elastic+acme'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'runbld: windows is not supported yet'))
    assertJobStatusFailure()
  }
}
