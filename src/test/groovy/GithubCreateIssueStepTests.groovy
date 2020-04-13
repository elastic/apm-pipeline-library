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

class GithubCreateIssueStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubCreateIssue.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubCreateIssue: windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_params() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubCreateIssue: title argument is required.'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_title() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=2a9602aa-ab9f-4e52-baf3-b71ca88469c7, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', 'hub issue create -m foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_title_with_credentials() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', credentialsId: 'bar')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('withCredentials', 'credentialsId=bar, variable=GITHUB_TOKEN'))
    assertTrue(assertMethodCallContainsPattern('sh', 'hub issue create -m foo'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_all_the_arguments() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', description: 'bar', assign: 'v1v', milestone: 'm1', labels: 'automation')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "hub issue create -m foo -m 'bar' -a v1v -l automation -M m1"))
    assertJobStatusSuccess()
  }

  @Test
  void test_some_cornercases() throws Exception {
    def script = loadScript(scriptName)
    script.call(title: 'foo', description: 'bar with some chars \n something else', assign: 'u1,u2,u3', labels: 'l1,l2,l3')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '''hub issue create -m foo -m 'bar with some chars \n something else' -a u1,u2,u3 -l l1,l2,l3'''))
    assertJobStatusSuccess()
  }
}
