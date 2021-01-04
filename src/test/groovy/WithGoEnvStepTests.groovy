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

class WithGoEnvStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withGoEnv.groovy')
  }

  @Test
  void test_is_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { return false })
    script.call(){}
    printCallStack()
    assertTrue(assertMethodCallOccurrences('withGoEnvWindows', 1))
    assertTrue(assertMethodCallOccurrences('withGoEnvUnix', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_is_linux() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { return true })
    script.call(){}
    printCallStack()
    assertTrue(assertMethodCallOccurrences('withGoEnvWindows', 0))
    assertTrue(assertMethodCallOccurrences('withGoEnvUnix', 1))
    assertJobStatusSuccess()
  }
}
