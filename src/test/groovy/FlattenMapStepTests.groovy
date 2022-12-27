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

class FlattenMapStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/flattenMap.groovy')
  }

  @Test
  void test_empty() throws Exception {
    def map = [:]
    def expected = [:]
    def result = script.call(map: map)
    printCallStack()
    assertTrue(expected.equals(result))
    assertJobStatusSuccess()
  }

  @Test
  void test_default_separator() throws Exception {
    def map = ["a": ["b": 1, "c": ["e": [2,3,4], "f": "key"]], "d": "value"]
    def expected = ["a.b": 1, "a.c.e": [2,3,4], "a.c.f": "key", "d": "value"]
    def result = script.call(map: map)
    printCallStack()
    assertTrue(expected.equals(result))
    assertJobStatusSuccess()
  }

  @Test
  void test_custom_separator() throws Exception {
    def map = ["a": ["b": 1, "c": ["e": [2,3,4], "f": "key"]], "d": "value"]
    def expected = ["a-b": 1, "a-c-e": [2,3,4], "a-c-f": "key", "d": "value"]
    def result = script.call(map: map, separator: "-")
    printCallStack()
    assertTrue(expected.equals(result))
    assertJobStatusSuccess()
  }
}
