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

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class SendDataToElasticsearchStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("readJSON", [Map.class], { m ->
      def jsonSlurper = new groovy.json.JsonSlurper()
      def object = jsonSlurper.parseText(m.text)
      return object
    })
    helper.registerAllowedMethod("error", [String.class], {s ->
      printCallStack()
      throw new Exception(s)
    })
    helper.registerAllowedMethod("toJSON", [String.class], { s ->
      def script = loadScript("vars/toJSON.groovy")
      return script.call(s)
    })
    helper.registerAllowedMethod("getVaultSecret", [Map.class], { m ->
      return [data: [user: "admin", password: "admin123"]]
    })
    helper.registerAllowedMethod("httpRequest", [Map.class], { "OK" })
    helper.registerAllowedMethod("base64encode", [Map.class], { return "YWRtaW46YWRtaW4xMjMK" })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/sendDataToElasticsearch.groovy")
    script.call(es: "https://ecs.example.com:9200", secret: "secret", data: '{"field":"value"}')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "httpRequest"
    }.size() == 1)
    assertJobStatusSuccess()
  }

  @Test
  void testNoEsURL() throws Exception {
    def script = loadScript("vars/sendDataToElasticsearch.groovy")
    try{
      script.call(secret: "secret", data: '{"field":"value"}')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("sendDataToElasticsearch: Elasticsearch URL is not valid.")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testNoSecret() throws Exception {
    def script = loadScript("vars/sendDataToElasticsearch.groovy")
    try{
      script.call(es: "https://ecs.example.com:9200", data: '{"field":"value"}')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("sendDataToElasticsearch: secret is not valid.")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testNoData() throws Exception {
    def script = loadScript("vars/sendDataToElasticsearch.groovy")
    try{
      script.call(es: "https://ecs.example.com:9200", secret: "secret")
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("sendDataToElasticsearch: data is not valid.")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testInvalidSecret() throws Exception {
    def script = loadScript("vars/sendDataToElasticsearch.groovy")
    helper.registerAllowedMethod("getVaultSecret", [Map.class], { m ->
      return [data: [password: "admin123"]]
    })
    try{
      script.call(es: "https://ecs.example.com:9200", secret: "secret", data: '{"field":"value"}')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: was not possible to get authentication info to send data.")
    })
    assertJobStatusSuccess()
  }
}
