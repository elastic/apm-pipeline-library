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

import co.elastic.mock.WithGithubCheckMock
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue

class WithGithubNotifyStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setProperty('withGithubCheck', new WithGithubCheckMock(true))
    script = loadScript('vars/withGithubNotify.groovy')
  }

  @Test
  void test_default_behaviour() throws Exception {
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallOccurrences('withGithubStatus', 1))
    assertTrue(assertMethodCallOccurrences('withGithubCheck', 0))
  }

  @Test
  void test_with_GITHUB_CHECK_false() throws Exception {
    env.GITHUB_CHECK = 'false'
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallOccurrences('withGithubStatus', 1))
    assertTrue(assertMethodCallOccurrences('withGithubCheck', 0))
  }

  @Test
  void test_with_GITHUB_CHECK_true() throws Exception {
    env.GITHUB_CHECK = 'true'
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallOccurrences('withGithubStatus', 0))
    assertTrue(assertMethodCallOccurrences('withGithubCheck', 1))
  }

  @Test
  void test_with_GITHUB_CHECK_true_and_not_available() throws Exception {
    env.GITHUB_CHECK = 'true'
    binding.setProperty('withGithubCheck', new WithGithubCheckMock(false))
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallOccurrences('withGithubStatus', 1))
    assertTrue(assertMethodCallOccurrences('withGithubCheck', 0))
  }
}
