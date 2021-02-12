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
import static org.junit.Assert.assertFalse

class MatrixStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/matrix.groovy'
  def axes = [
    [
      [name: 'foo', value: '1'],
      [name: 'foo', value: '2']
    ],
    [
      [name: 'bar', value: 'a'],
      [name: 'bar', value: 'b']
    ]
  ]
  def excludes = [
    [
      [name: 'foo', value: '1']
    ],
    [
      [name: 'bar', value: 'a']
    ]
  ]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    printCallStack() {
      script.call(
        axes: axes
      ) {
        isOK = true
      }
    }
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=b]'))

    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=b]'))
    assertJobStatusSuccess()
  }

  @Test
  void testExcludes() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    printCallStack() {
      script.call(
        axes: axes,
        excludes: excludes
      ) {
        isOK = true
      }
    }
    assertTrue(isOK)
    assertFalse(assertMethodCallContainsPattern('parallel', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=b]'))

    assertFalse(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=b]'))
    assertJobStatusSuccess()
  }

  @Test
  void testAgent() throws Exception {
    def script = loadScript(scriptName)
    def isOK = false
    printCallStack() {
      script.call(
        agent: 'linux',
        axes: axes
        ) {
          isOK = true
        }
    }
    assertTrue(isOK)
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('parallel', '[foo=2, bar=b]'))

    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=1, bar=b]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=a]'))
    assertTrue(assertMethodCallContainsPattern('withEnv', '[foo=2, bar=b]'))

    assertTrue(assertMethodCallContainsPattern('node', 'linux'))
    assertTrue(assertMethodCallOccurrences('node', 4))
    assertJobStatusSuccess()
  }
}
