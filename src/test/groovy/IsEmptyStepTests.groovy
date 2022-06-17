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

class IsEmptyStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isEmpty.groovy')
  }

  @Test
  void test_empty() throws Exception {
    def result = script.call('')
    printCallStack()
    assertTrue(result)
  }

  @Test
  void test_null() throws Exception {
    def result = script.call(null)
    printCallStack()
    assertTrue(result)
  }

  @Test
  void test_value() throws Exception {
    def result = script.call("bar")
    printCallStack()
    assertFalse(result)
  }
}
