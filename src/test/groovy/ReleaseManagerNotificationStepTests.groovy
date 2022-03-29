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

class ReleaseManagerNotificationStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/releaseManagerNotification.groovy')
    addEnvVar('SLACK_CHANNEL', '#foo')
    addEnvVar('NOTIFY_TO', 'to@acme.com')
  }

  @Test
  void test() throws Exception {
    def ret = script.call(file: 'report.txt', body: 'my-body', subject: 'my-subject')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=#foo, color=null, message=my-subject. my-body'))
    assertTrue(assertMethodCallContainsPattern('emailext', 'subject=my-subject, to=to@acme.com, body=my-body'))
    assertTrue(assertMethodCallOccurrences('releaseManagerAnalyser', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_analyse_and_failures() throws Exception {
    helper.registerAllowedMethod('releaseManagerAnalyser', [Map.class], { 'There were some errors while running the release manager.'})
    script.call(file: 'report.txt', body: 'my-body', subject: 'my-subject', analyse: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('releaseManagerAnalyser', 1))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=#foo, color=null, message=my-subject. my-body'))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'There were some errors while running the release manager'))
    assertTrue(assertMethodCallContainsPattern('emailext', 'subject=my-subject, to=to@acme.com, body=my-body'))
    assertTrue(assertMethodCallContainsPattern('emailext', 'There were some errors while running the release manager'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_analyse_and_without_failures() throws Exception {
    helper.registerAllowedMethod('releaseManagerAnalyser', [Map.class], { '' })
    script.call(file: 'report.txt', body: 'my-body', subject: 'my-subject', analyse: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('releaseManagerAnalyser', 1))
    assertTrue(assertMethodCallContainsPattern('slackSend', 'channel=#foo, color=null, message=my-subject. my-body, tokenCredentialId='))
    assertTrue(assertMethodCallContainsPattern('emailext', '{subject=my-subject, to=to@acme.com, body=my-body}'))
    assertJobStatusSuccess()
  }
}
