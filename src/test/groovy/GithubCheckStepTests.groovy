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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertSame
import static org.junit.Assert.assertTrue

class GithubCheckStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubCheck.groovy')
  }

  @Test
  void test_without_name_argument() throws Exception {
    testMissingArgument('name') {
      script.call()
    }
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_match() throws Exception {
    // When githubApiCall returns a list
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [ check_runs: [[
                  name: 'bar',
                  id: '1'
                ],[
                  name: 'my-check',
                  id: '2'
                ]
             ]]
    })
    // and calling the getPreviousCheckNameRunIdIfExists with a already created checkName
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    // Then the result matches the expected id.
    assertSame(ret, '2')
    assertJobStatusSuccess()
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_no_match() throws Exception {
    // When githubApiCall returns a list
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [ check_runs: [[
                              name: 'bar',
                              id: '1'
                            ]
      ]]
    })
    // and calling the getPreviousCheckNameRunIdIfExists with an unexisting checkName
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    // Then the result is Null
    assertNull(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_getPreviousCheckNameRunIdIfExists_when_exception() throws Exception {
    // When githubApiCall throws an error
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      throw new Exception('Forced a failure')
    })
    // and calling the getPreviousCheckNameRunIdIfExists with some arguments
    def ret = script.getPreviousCheckNameRunIdIfExists(token: 'token', org: 'acme', repository: 'foo', commitId: 'abcdef', checkName: 'my-check')
    printCallStack()
    // Then the result is FALSE
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_setCheckName_with_default() throws Exception {
    // When calling the setCheckName with the default arguments
    script.setCheckName(token: 'my-token', org: 'acme', repository: 'foo', checkName: 'my-check',
                        commitId: 'abcdef', checkRunId: '1')
    printCallStack()
    // Then API Call is correct and SHA commit is required
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'https://api.github.com/repos/acme/foo/check-runs'))
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'head_sha=abcdef'))
    assertJobStatusSuccess()
  }

  @Test
  void test_setCheckName_with_patch_method_and_default() throws Exception {
    // When calling the setCheckName with the PATCH method
    script.setCheckName(token: 'my-token', org: 'acme', repository: 'foo', checkName: 'my-check', commitId: 'abcdef', checkRunId: '1', method: 'PATCH')
    printCallStack()
    // Then API Call is correct, no details_url is used and no SHA commit is required
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'https://api.github.com/repos/acme/foo/check-runs'))
    assertFalse(assertMethodCallContainsPattern('githubApiCall', 'details_url='))
    assertFalse(assertMethodCallContainsPattern('githubApiCall', 'head_sha=abcdef'))
    assertJobStatusSuccess()
  }

  @Test
  void test_setCheckName_with_detailsUrl() throws Exception {
    // When calling the setCheckName with the PATCH method
    script.setCheckName(token: 'my-token', org: 'acme', repository: 'foo', checkName: 'my-check', commitId: 'abcdef', checkRunId: '1', detailsUrl: 'https://acme.co.uk')
    printCallStack()
    // Then API Call is correct and details_url is used
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'details_url=https://acme.co.uk'))
    assertJobStatusSuccess()
  }

  @Test
  void test_createCheck() throws Exception {
    // When calling the createCheck method
    script.createCheck(token: 'my-token', org: 'acme', repository: 'foo', checkName: 'my-check', commitId: 'abcdef', checkRunId: '1')
    printCallStack()
    // Then SHA commit is required
    assertTrue(assertMethodCallContainsPattern('githubApiCall', 'head_sha=abcdef'))
    assertJobStatusSuccess()
  }

  @Test
  void test_updateCheck() throws Exception {
    // When calling the updateCheck method
    script.updateCheck(token: 'my-token', org: 'acme', repository: 'foo', checkName: 'my-check', commitId: 'abcdef', checkRunId: '1')
    printCallStack()
    // Then no SHA commit is required
    assertFalse(assertMethodCallContainsPattern('githubApiCall', 'head_sha=abcdef'))
    assertJobStatusSuccess()
  }
}
