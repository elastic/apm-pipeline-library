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

class SuperLinterStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/superLinter.groovy')
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      return 0
    })
  }

  @Test
  void test_with_failNever_false() throws Exception {
    script.call(failNever: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-e RUN_LOCAL=true -e DISABLE_ERRORS=false'))
    assertTrue(assertMethodCallOccurrences('tap2Junit', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failNever_true() throws Exception {
    script.call(failNever: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-e RUN_LOCAL=true -e DISABLE_ERRORS=true'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_envs() throws Exception {
    script.call(envs: [ 'foo=bar', 'var=value'])
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-e foo=bar -e var=value'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_junit_true() throws Exception {
    script.call(junit: true)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('tap2Junit', 1))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_junit_false() throws Exception {
    script.call(junit: false)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('tap2Junit', 0))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_installation_error_with_not_failNever() throws Exception {
    env.GIT_BASE_COMMIT = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m?.label?.contains('Install super-linter')){
        throw new Exception('Timeout when reaching github')
      }
    })
    try {
      script.call(failNever: false)
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('sh', 2))
    assertTrue(assertMethodCallContainsPattern('sh', "label=Install super-linter"))
    assertJobStatusFailure()
  }

  @Test
  void test_with_superlinter_error_and_not_failNever() throws Exception {
    env.GIT_BASE_COMMIT = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m?.label?.contains('Run super-linter')){
        return 1
      }
    })
    try {
      script.call(failNever: false)
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Super linter failed'))
    assertJobStatusFailure()
  }

  @Test
  void test_with_superlinter_error_and_failNever() throws Exception {
    env.GIT_BASE_COMMIT = 'bar'
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if(m?.label?.contains('Run super-linter')){
        return 1
      }
    })
    try {
      script.call(failNever: true)
    } catch(e) {
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallOccurrences('error', 0))
    assertJobStatusSuccess()
  }
}
