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

class CmdStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/cmd.groovy')
  }

  @Test
  void test() throws Exception {
    script.call(script: 'echo hi', returnStdout: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'script=echo hi, returnStdout=false'))
    assertTrue(assertMethodCallOccurrences('sh', 1))
    assertTrue(assertMethodCallOccurrences('bat', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(script: 'echo hi')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'script=echo hi'))
    assertTrue(assertMethodCallOccurrences('sh', 0))
    assertTrue(assertMethodCallOccurrences('bat', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows_with_returnStdout() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(script: 'echo hi', returnStdout: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', 'script=@echo off'))
    assertTrue(assertMethodCallContainsPattern('bat', 'echo hi'))
    assertTrue(assertMethodCallOccurrences('bat', 1))
    assertJobStatusSuccess()
  }
}
