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

class DummyDeclarativePipelineStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setProperty('BASE_DIR', '/')
    script = loadScript('vars/dummyDeclarativePipeline.groovy')
  }

  @Test
  void test_with_defaults() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('stage', 'Checkout'))
    assertTrue(assertMethodCallContainsPattern('stage', 'Build'))
    assertTrue(assertMethodCallContainsPattern('echo', "FOO=''"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_foo_params() throws Exception {
    params.FOO = 'BAR'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('stage', 'Checkout'))
    assertTrue(assertMethodCallContainsPattern('stage', 'Build'))
    assertTrue(assertMethodCallContainsPattern('echo', "FOO='BAR'"))
    assertJobStatusSuccess()
  }
}
