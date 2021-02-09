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
import org.junit.Ignore
import org.junit.Test
import static org.junit.Assert.assertTrue

class DetailsURLStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/detailsURL.groovy')
  }

  @Test
  void test_default_with_blueocean() throws Exception {
    def url = script.call(isBlueOcean: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getBlueoceanTabURL', 'pipeline'))
    assertJobStatusSuccess()
  }

  @Test
  void test_default_without_blueocean() throws Exception {
    def url = script.call(isBlueOcean: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getTraditionalPageURL', 'pipeline'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_http() throws Exception {
    def url = script.call(tab: 'http://foo')
    printCallStack()
    assertTrue(url.equals('http://foo'))
    assertJobStatusSuccess()
  }

  @Test
  @Ignore("StageId call is not working with parallel. See https://github.com/elastic/apm-pipeline-library/issues/961")
  void test_with_stage() throws Exception {
    addEnvVar('BUILD_NUMBER', '1')
    helper.registerAllowedMethod('getStageId', [], { 2 })
    helper.registerAllowedMethod('getBlueoceanRestURLJob', [Map.class], { m -> 'http://jenkins.com:8080/blue/rest/organizations/jenkins/pipelines/acme/foo/' })
    def url = script.call()
    printCallStack()
    assertTrue(url.trim().equals('http://jenkins.com:8080/blue/rest/organizations/jenkins/pipelines/acme/foo/runs/1/nodes/2/log/?start=0'))
    assertJobStatusSuccess()
  }
}
