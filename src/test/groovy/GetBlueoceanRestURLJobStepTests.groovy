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

class GetBlueoceanRestURLJobStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/getBlueoceanRestURLJob.groovy')
  }

  @Test
  void test_missing_jobURL() throws Exception {
    testMissingArgument('jobURL') {
      script.call()
    }
  }

  @Test
  void test_success() throws Exception {
    addEnvVar('JENKINS_URL', 'http://jenkins.example.com:8080/')
    def ret = script.call(jobURL: 'http://jenkins.example.com:8080/job/acme/job/foo')
    printCallStack()
    assertTrue(ret.contains('http://jenkins.example.com:8080/blue/rest/organizations/jenkins/pipelines/acme/foo/'))
    assertJobStatusSuccess()
  }

  @Test
  void test_success_with_jenkins_url_without_ending_in_slash() throws Exception {
    addEnvVar('JENKINS_URL', 'http://jenkins.example.com:8080')
    def ret = script.call(jobURL: 'http://jenkins.example.com:8080/job/acme/job/foo')
    printCallStack()
    assertTrue(ret.contains('http://jenkins.example.com:8080/blue/rest/organizations/jenkins/pipelines/acme/foo/'))
    assertJobStatusSuccess()
  }
}
