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
import static org.junit.Assert.assertFalse

class RandomStringStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/randomString.groovy')
  }

  @Test
  void test_default() throws Exception {
    def ret = script.call()
    printCallStack()
    println ret
    assertTrue(ret.length() == 128)
  }

  @Test
  void test_with_size() throws Exception {
    def ret = script.call(size: 8)
    printCallStack()
    println ret
    assertTrue(ret.length() == 8)
  }

  @Test
  void test_manipulateString() throws Exception {
    def ret = script.manipulateString('f-o', 2)
    printCallStack()
    println ret
    assertTrue(ret == 'fa')
  }

  @Test
  void test_manipulateString_with_ending_dash() throws Exception {
    def ret = script.manipulateString('a-b-c-d-e-', 6)
    printCallStack()
    println ret
    assertTrue(ret == 'a-b-ca')
  }

  @Test
  void test_manipulateString_with_starting_dash() throws Exception {
    def ret = script.manipulateString('-a-b-c-d-e', 6)
    printCallStack()
    println ret
    assertTrue(ret == 'aa-b-c')
  }
}
