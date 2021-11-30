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
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class NotifyStalledBeatsBumpsStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/notifyStalledBeatsBumps.groovy')
  }

  @Test
  void test_without_arguments() throws Exception {
    testMissingArgument('branch') {
      script.call()
    }
  }

  @Test
  void test_with_defaults() throws Exception {
    script.call(branch: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "'https://github.com/elastic/beats.git' foo 7"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_params_and_nothing_to_be_reported() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return false })
    script.call(branch: 'foo', days: 1, sendEmail: true, to: 'me@acme.com')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "'https://github.com/elastic/beats.git' foo 1"))
    assertTrue(assertMethodCallOccurrences('mail', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_email() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true })
    script.call(branch: 'foo', sendEmail: true, to: 'me@acme.com', subject: 'foo: bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "'https://github.com/elastic/beats.git' foo 7"))
    assertTrue(assertMethodCallContainsPattern('mail', "to=me@acme.com"))
    assertTrue(assertMethodCallContainsPattern('mail', "subject=foo: bar"))
  }

  @Test
  void test_with_email_and_no_to() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { return true })
    script.call(branch: 'foo', sendEmail: true)
    printCallStack()
    assertJobStatusSuccess()
    assertTrue(assertMethodCallContainsPattern('log', "to' param is empty"))
    assertTrue(assertMethodCallOccurrences('mail', 0))
  }
}
