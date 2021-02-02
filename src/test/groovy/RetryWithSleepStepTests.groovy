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
import static org.junit.Assert.assertTrue

class RetryWithSleepStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/retryWithSleep.groovy')
  }

  @Test
  void test_missing_parameter() throws Exception {
    testMissingArgument('retries') {
      script.call(){ }
    }
  }

  @Test
  void test_retry() throws Exception {
    def ret = false
    script.call(retries: 3) {
      ret = true
    }
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallOccurrences('log', 1))
    assertFalse(assertMethodCallContainsPattern('log', 'sleep 10 seconds'))
  }

  @Test
  void test_retry_with_sleep_first() throws Exception {
    def ret = false
    script.call(retries: 3, sleepFirst: true) {
      ret = true
    }
    printCallStack()
    assertTrue(ret)
    assertTrue(assertMethodCallOccurrences('log', 2))
    assertTrue(assertMethodCallContainsPattern('log', 'sleep 10 seconds'))
  }

  @Test
  void test_retry_with_errors() throws Exception {
    def ret = false
    try {
      script.call(retries: 3) {
        throw new Exception('Force failure')
        ret = true
      }
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('log', '3 of 3 tries'))
    assertFalse(assertMethodCallContainsPattern('log', 'sleep 30 seconds'))
  }

  @Test
  void test_retry_with_errors_with_backoff() throws Exception {
    def ret = false
    try {
      script.call(retries: 3, backoff: true) {
        throw new Exception('Force failure')
        ret = true
      }
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertFalse(ret)
    assertTrue(assertMethodCallContainsPattern('log', '3 of 3 tries'))
    assertTrue(assertMethodCallContainsPattern('log', 'sleep 30 seconds'))
  }
}
