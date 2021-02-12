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

class GoTestJUnitStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/goTestJUnit.groovy')
    env.GOPATH = "${env.WORKSPACE}"
    helper.registerAllowedMethod('withGoEnvUnix', [Map.class, Closure.class], { m, c ->
      c.call()
    })
  }

  @Test
  void test() throws Exception {
    def version = "1.15.1"
    helper.registerAllowedMethod('goDefaultVersion', [], { version })
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'gotestsum --format testname --junitfile junit-report.xml --'))
    assertTrue(assertMethodCallContainsPattern('withGoEnvUnix', "version=${version}"))
    assertJobStatusSuccess()
  }

  @Test
  void testArguments() throws Exception {
    script.call(options: '-foo -bar', output: 'foo.xml', version: 'fooGo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '-- -foo -bar'))
    assertTrue(assertMethodCallContainsPattern('sh', '--junitfile foo.xml'))
    assertTrue(assertMethodCallContainsPattern('withGoEnvUnix', 'version=fooGo'))
    assertJobStatusSuccess()
  }
}
