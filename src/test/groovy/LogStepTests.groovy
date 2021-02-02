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
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class LogStepTests extends ApmBasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/log.groovy')
  }

  @Test
  void test() throws Exception {
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    script.call(text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[DEBUG]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testDebug() throws Exception {
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    script.call(level: 'DEBUG', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[DEBUG]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testInfo() throws Exception {
    script.call(level: 'INFO', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[INFO]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWarn() throws Exception {
    env.PIPELINE_LOG_LEVEL = 'WARN'
    script.call(level: 'WARN', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[WARN]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testError() throws Exception {
    env.PIPELINE_LOG_LEVEL = 'ERROR'
    script.call(level: 'ERROR', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[ERROR]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testLevel() throws Exception {
    env.PIPELINE_LOG_LEVEL = 'WARN'
    script.call(level: 'DEBUG', text: "messageDEBUG")
    script.call(level: 'INFO', text: "messageINFO")
    script.call(level: 'WARN', text: "messageWARN")
    script.call(level: 'ERROR', text: "messageERROR")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        !callArgsToString(call).contains("[DEBUG]")
        !callArgsToString(call).contains("[INFO]")
        callArgsToString(call).contains("[WARN]")
        callArgsToString(call).contains("[ERROR]")
    })
    assertJobStatusSuccess()
  }
}
