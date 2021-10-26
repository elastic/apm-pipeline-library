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
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class GsutilStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('isInstalled', [Map.class], { return true })
    script = loadScript('vars/gsutil.groovy')
  }

  @Test
  void test_missing_command() throws Exception {
    testMissingArgument('command') {
      script.call()
    }
  }

  @Test
  void test_missing_credentialsId_or_secret() throws Exception {
    testMissingArgument('credentialsId or secret', 'parameters are required') {
      script.call(command: 'cp')
    }
  }

  @Test
  void test_command() throws Exception {
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "gsutil cp"))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_failed() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      if (m.label.startsWith('gsutil')) { throw new Exception('unknown command "foo" for "gsutil"') }})
    def result
    try {
      result = script.call(command: 'foo', credentialsId: 'foo')
    } catch(err) {
      println err
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'returnStdout=true'))
    assertNull(result)
  }

  @Test
  void test_windows() throws Exception {
    helper.registerAllowedMethod('isUnix', [], { false })
    script.call(command: 'cp', credentialsId: 'foo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('bat', "gsutil cp"))
    assertJobStatusSuccess()
  }
}
