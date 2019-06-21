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
import static org.junit.Assert.assertFalse
import co.elastic.NotificationManager

class NotifyBuildResultStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    env.JOB_NAME = "testJob"
    env.BUILD_NUMBER = '100'
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.JOB_URL = "${env.JENKINS_URL}/job/${env.JOB_NAME}"
    env.NOTIFY_TO = "myName@example.com"

    binding.setVariable('env', env)
    binding.getVariable('currentBuild').result = "FAILURE"
    binding.getVariable('currentBuild').currentResult = "FAILURE"

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("emailext", [Map.class], { println("sending email") })
    helper.registerAllowedMethod("readJSON", [Map.class], { m ->
      return ["field": "value"]
    })
    helper.registerAllowedMethod("error", [String.class], {s ->
      printCallStack()
      throw new Exception(s)
    })
    helper.registerAllowedMethod("toJSON", [String.class], { s ->
      def script = loadScript("vars/toJSON.groovy")
      return script.call(s)
    })
    helper.registerAllowedMethod("toJSON", [Map.class], { s ->
      def script = loadScript("vars/toJSON.groovy")
      return script.call(s)
    })
    helper.registerAllowedMethod("brokenTestsSuspects", { "OK" })
    helper.registerAllowedMethod("brokenBuildSuspects", { "OK" })
    helper.registerAllowedMethod("upstreamDevelopers", { "OK" })
    helper.registerAllowedMethod("getVaultSecret", [Map.class], {
      return [data: [user: "admin", password: "admin123"]]
    })
    helper.registerAllowedMethod("httpRequest", [Map.class], { "OK" })
    helper.registerAllowedMethod("base64encode", [Map.class], { return "dTpwCg==" })
    helper.registerAllowedMethod("writeJSON", [Map.class], { "OK" })
    helper.registerAllowedMethod("readFile", [Map.class], { return '{"field": "value"}' })
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("getBuildInfoJsonFiles", [String.class,String.class], { "OK" })
    helper.registerAllowedMethod("catchError", [Closure.class], { c -> c() })
    helper.registerAllowedMethod("catchError", [Map.class, Closure.class], { m, c ->
      try{
        c()
      } catch(e){
        //NOOP
      }
    })
    helper.registerAllowedMethod("sendDataToElasticsearch", [Map.class], { "OK" })

    co.elastic.NotificationManager.metaClass.notifyEmail{ Map m -> 'OK' }
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call(es: "https://ecs.example.com:9200", secret: "secret")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "getBuildInfoJsonFiles"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "archiveArtifacts"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: Notifying results by email.")
    })
  }

  @Test
  void testPullRequest() throws Exception {
    env.CHANGE_ID = "123"

    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call(es: "https://ecs.example.com:9200", secret: "secret")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "getBuildInfoJsonFiles"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "archiveArtifacts"
    }.size()== 1)
    assertFalse(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: Notifying results by email.")
    })
  }

  @Test
  void testSuccessBuild() throws Exception {
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call(es: "https://ecs.example.com:9200", secret: "secret")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "getBuildInfoJsonFiles"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "archiveArtifacts"
    }.size()== 1)
    assertFalse(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: Notifying results by email.")
    })
  }

  @Test
  void testWithoutParams() throws Exception {
    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "getBuildInfoJsonFiles"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "archiveArtifacts"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: Notifying results by email.")
    })
  }

  @Test
  void testWithoutSecret() throws Exception {
    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call(es: "https://ecs.example.com:9200")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "getBuildInfoJsonFiles"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "archiveArtifacts"
    }.size()== 1)
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("notifyBuildResult: Notifying results by email.")
    })
  }

  @Test
  void testCatchError() throws Exception {
    // When a failure
    helper.registerAllowedMethod("getBuildInfoJsonFiles", [String.class,String.class], { throw new Exception(s) })

    // Then the build is Success
    binding.getVariable('currentBuild').result = "SUCCESS"
    binding.getVariable('currentBuild').currentResult = "SUCCESS"

    def script = loadScript("vars/notifyBuildResult.groovy")
    script.call(es: "https://ecs.example.com:9200", secret: "secret")
    printCallStack()

    // Then no further actions are executed afterwards
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sendDataToElasticsearch"
    }.size()== 0)

    // Then unstable the stage
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "catchError"
    }.any { call ->
        callArgsToString(call).contains('buildResult=SUCCESS, stageResult=UNSTABLE')
    })

    assertJobStatusSuccess()
  }
}
