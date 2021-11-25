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

class Tap2JunitExtStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/tap2JunitExt.groovy')
  }

  @Test
  void testWindows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "tap-to-junit-ext.sh '*.tap'"))
    assertTrue(assertMethodCallContainsPattern('tap2Junit', '*.tap.ext'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_pattern() throws Exception {
    script.call(pattern: 'foo.*')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "tap-to-junit-ext.sh 'foo.*'"))
    assertJobStatusSuccess()
  }
}
