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

class GoVersionStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/goVersion.groovy')
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_action() throws Exception {
    testMissingArgument('action') {
      script.call()
    }
  }

  @Test
  void test_unsupported_action() throws Exception {
    try {
      script.call(action: 'unknown')
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', "goVersion: unsupported action."))
    assertJobStatusFailure()
  }

  @Test
  void test_latest_unstable() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class],{'1.17beta1'})
    def obj = script.call(action: 'latest', unstable: 'true')
    assertTrue(obj.equals('1.17beta1'))
    assertTrue(assertMethodCallContainsPattern('sh', '--refs git://github.com/golang/go'))
    assertTrue(assertMethodCallContainsPattern('sh', 'grep "go*" | sed "s#^go##g" | sort --version-sort -r | head -n1'))
    printCallStack()
  }

  @Test
  void test_latest() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class],{'1.16.5'})
    def obj = script.call(action: 'latest')
    assertTrue(obj.equals('1.16.5'))
    assertTrue(assertMethodCallContainsPattern('sh', 'grep "go*" | grep -v "[beta|rc]" | sed "s#^go##g" | sort --version-sort -r | head -n1'))
    printCallStack()
  }

  @Test
  void test_latest_with_glob() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class],{'1.15.13'})
    def obj = script.call(action: 'latest', glob: '1.15')
    assertTrue(obj.equals('1.15.13'))
    assertTrue(assertMethodCallContainsPattern('sh', 'grep "go*" | grep -v "[beta|rc]" | grep "1.15" | sed "s#^go##g" | sort --version-sort -r | head -n1'))
    printCallStack()
  }

  @Test
  void test_versions() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class],{'''1.16.5
1.16.4
1.16
1.15.13
1.15.12
    '''})
    def obj = script.call(action: 'versions')
    assertTrue(obj.split(System.getProperty("line.separator")).length == 5)
    assertTrue(assertMethodCallContainsPattern('sh', 'grep "go*" | grep -v "[beta|rc]" | sed "s#^go##g" | sort --version-sort -r'))
    assertFalse(assertMethodCallContainsPattern('sh', 'head -n1'))
    printCallStack()
  }
}
