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

import co.elastic.mock.StepsMock
import co.elastic.BuildException
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.hamcrest.CoreMatchers.is

public class BuildTokenTriggerExtStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/buildTokenTriggerExt.groovy')
  }

  @Test
  void test_default() throws Exception {
    script.call(job: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('buildTokenTrigger', "job"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_wait() throws Exception {
    script.call(job: 'foo', wait: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('buildTokenTrigger', "job"))
    assertTrue(assertMethodCallOccurrences('waitUntil', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_propagate() throws Exception {
    script.call(job: 'foo', propagate: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('buildTokenTrigger', "job"))
    assertTrue(assertMethodCallOccurrences('waitUntil', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_plugin_not_installed() throws Exception {
    helper.registerAllowedMethod('isPluginInstalled', [Map.class], { return false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'plugin is not available'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_propagate_error() throws Exception {
    try {
      script.call(propagate: true, job: 'mock-test-error-for-unit-testing')
    } catch(e){
      //NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('buildTokenTrigger', "job"))
    // then the propagate will report an error
    assertTrue(assertMethodCallContainsPattern('error', 'buildTokenTriggerExt'))
  }

  @Test
  void test_with_wait_error() throws Exception {
    try {
      script.call(wait: true, job: 'mock-test-error-for-unit-testing')
    } catch(e){
      //NOOP
      println e
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('buildTokenTrigger', "job"))
    // then the propagate won't report an error
    assertFalse(assertMethodCallContainsPattern('error', 'buildTokenTriggerExt'))
  }

}
