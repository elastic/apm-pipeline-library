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

class JunitAndStoreStepTests extends ApmBasePipelineTest {


  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/junitAndStore.groovy')
  }

  @Test
  void test_no_stashedTestReports_parameter() throws Exception {
    try {
      script.call()
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'stashedTestReports param'))
    assertJobStatusFailure()
  }

  @Test
  void test_no_testResults() throws Exception {
    try {
      script.call(stashedTestReports: [:])
    } catch(err) {
      //
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'testResults param'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_id() throws Exception {
    script.call(stashedTestReports: [:], testResults: '*.xml')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=*.xml'))
    assertTrue(assertMethodCallContainsPattern('stash', 'includes=*.xml, allowEmpty=true, name=uncategorized-'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_id() throws Exception {
    script.call(stashedTestReports: [:], testResults: '*.xml', id: 'acme')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=*.xml'))
    assertFalse(assertMethodCallContainsPattern('junit', 'id'))
    assertFalse(assertMethodCallContainsPattern('junit', 'stashedTestReports'))
    assertTrue(assertMethodCallContainsPattern('stash', 'includes=*.xml, allowEmpty=true, name=acme-'))
    assertJobStatusSuccess()
  }
}
