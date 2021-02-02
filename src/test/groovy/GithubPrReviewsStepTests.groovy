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

class GithubPrReviewsStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubPrReviews.groovy')
  }

  @Test
  void test() throws Exception {
    def pr = script.call(token: 'token', repo: 'org/repo', pr: 1)
    printCallStack()
    assertTrue(pr instanceof java.util.ArrayList)
    assertJobStatusSuccess()
  }

  @Test
  void testErrorNoRepo() throws Exception {
    try {
      script.call(token: 'token', user: 1)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubPrReviews: no valid repository.'))
  }

  @Test
  void testErrorNoPR() throws Exception {
    try {
      script.call(token: 'token', repo: 'org/repo')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'githubPrReviews: no valid PR ID.'))
  }
}
