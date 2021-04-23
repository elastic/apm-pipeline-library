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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class ReleaseNotificationStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/releaseNotification.groovy')
  }

  @Test
  void test_without_arguments() throws Exception {
    script.call()
    printCallStack()
    assertTrue(assertMethodCallOccurrences('emailext', 1))
    assertTrue(assertMethodCallOccurrences('slackSend', 0))
  }

  @Test
  void test_without_arguments_and_env_variables() throws Exception {
    addEnvVar('SLACK_CHANNEL', '#foo')
    addEnvVar('NOTIFY_TO', 'to@acme.com')
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('emailext', 'to@acme.com'))
    assertTrue(assertMethodCallContainsPattern('slackSend', '#foo'))
  }

  @Test
  void test_without_arguments_and_env_variables_all_parameters() throws Exception {
    addEnvVar('SLACK_CHANNEL', '#foo')
    addEnvVar('NOTIFY_TO', 'to@acme.com')
    script.call(slackColor: 'good',
                slackCredentialsId: 'credentials',
                slackChannel: '#channel',
                to: "to@foo.com",
                subject: "[example] Release tag *0.1.1* has been created", 
                body: "Build: (<http://foo.bar|here>) for further details.")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('emailext', 'to@foo.com'))
    assertTrue(assertMethodCallContainsPattern('slackSend', '#channel'))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'good'))
  }

  @Test
  void test_transformSlackURLFormatToEmailFormat_null() throws Exception {
    def ret = script.transformSlackURLFormatToEmailFormat(null)
    printCallStack()
    assertNull(ret)
  }

  @Test
  void test_transformSlackURLFormatToEmailFormat_empty() throws Exception {
    def ret = script.transformSlackURLFormatToEmailFormat('')
    printCallStack()
    assertTrue(ret.equals(''))
  }

  @Test
  void test_transformSlackURLFormatToEmailFormat_foo() throws Exception {
    def ret = script.transformSlackURLFormatToEmailFormat('foo')
    printCallStack()
    assertTrue(ret.equals('foo'))
  }

  @Test
  void test_transformSlackURLFormatToEmailFormat_url() throws Exception {
    def ret = script.transformSlackURLFormatToEmailFormat('(<URL|description>)')
    printCallStack()
    assertTrue(ret.equals('URL'))
  }
}
