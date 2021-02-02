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

class CreateFileFromTemplateStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/createFileFromTemplate.groovy')
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_data_param() throws Exception {
    testMissingArgument('data') {
      script.call()
    }
  }

  @Test
  void test_missing_template_param() throws Exception {
    testMissingArgument('template') {
      script.call(data: 'foo.json')
    }
  }

  @Test
  void test_missing_output_param() throws Exception {
    testMissingArgument('output') {
      script.call(data: 'foo.json', template: 'foo.j2')
    }
  }

  @Test
  void test_no_local() throws Exception {
    try {
      script.call(data: 'foo.json', template: 'foo.j2', output: 'foo.md')
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('writeFile', 2))
    assertTrue(assertMethodCallContainsPattern('sh', '-f foo.json -t foo.j2 -o foo.md'))
    assertJobStatusSuccess()
  }

  @Test
  void test_local() throws Exception {
    try {
      script.call(data: 'foo.json', template: 'foo.j2', output: 'foo.md', localTemplate: true)
    } catch (e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('writeFile', 1))
    assertTrue(assertMethodCallContainsPattern('sh', '-f foo.json -t foo.j2 -o foo.md'))
    assertJobStatusSuccess()
  }
}
