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

class IsDarwinStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/isDarwin.groovy')
    helper.registerAllowedMethod('nodeOS', [], {
      def script = loadScript('vars/nodeOS.groovy')
      return script.call()
    })
  }

  @Test
  void test_macos() throws Exception {
    env.NODE_LABELS = 'macos'
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_linux() throws Exception {
    env.NODE_LABELS = 'linux'
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void test_error() throws Exception {
    env.NODE_LABELS = 'foo bar'
    try {
      def ret = script.call()
    } catch (e) {
      assertTrue(e.getMessage() == 'Unhandled OS name in NODE_LABELS: foo bar')
    }
    printCallStack()
    assertJobStatusFailure()
  }
}
