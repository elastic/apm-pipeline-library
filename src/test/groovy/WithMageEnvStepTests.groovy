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

class WithMageEnvStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/withMageEnv.groovy')
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('withGoEnv', [Map.class, Closure.class], { m, b -> b()  })
    def isOK = false
    script.call(){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withGoEnv', 'pkgs=['))
    assertJobStatusSuccess()
  }

  @Test
  void testVersion() throws Exception {
    helper.registerAllowedMethod('withGoEnv', [Map.class, Closure.class], { m, b -> b()  })
    def isOK = false
    script.call(version: "1.12.2"){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withGoEnv', 'version=1.12.2'))
    assertJobStatusSuccess()
  }

  @Test
  void testPkgs() throws Exception {
    helper.registerAllowedMethod('withGoEnv', [Map.class, Closure.class], { m, b -> b()  })
    def isOK = false
    script.call(pkgs: ["foo"]){
      isOK = true
    }
    printCallStack()
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('withGoEnv', 'foo'))
    assertJobStatusSuccess()
  }
}
