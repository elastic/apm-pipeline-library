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

class Tap2JunitStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/tap2Junit.groovy')
  }

  @Test
  void test() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'node:12-alpine'))
    assertTrue(assertMethodCallContainsPattern('sh', '-junit-report.xml'))
    assertTrue(assertMethodCallContainsPattern('sh', '--package="co.elastic"'))
    assertTrue(assertMethodCallContainsPattern('junit', 'junit-report.xml'))
    assertTrue(assertMethodCallContainsPattern('sh', 'for i in "*.tap"'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_suffix() throws Exception {
    script.call(suffix: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('junit', 'foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_nodeVersion() throws Exception {
    script.call(nodeVersion: 'foo:latest')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'foo:latest'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_package() throws Exception {
    script.call(package: 'foo.bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '--package="foo.bar"'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_pattern() throws Exception {
    script.call(pattern: 'foo.*')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'for i in "foo.*"'))
    assertJobStatusSuccess()
  }
}
