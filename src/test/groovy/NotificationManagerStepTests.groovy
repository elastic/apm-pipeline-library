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

class NotificationManagerStepTests extends BasePipelineTest {
  Map env = [:]

  def wrapInterceptor = { map, closure ->
    map.each { key, value ->
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", "${it.password}")
        }
      }
    }
    def res = closure.call()
    map.forEach { key, value ->
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", null)
        }
      }
    }
    return res
  }

  def withEnvInterceptor = { list, closure ->
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], fields[1])
    }
    def res = closure.call()
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], null)
    }
    return res
  }

  def withCredentialsInterceptor = { list, closure ->
    list.forEach {
      env[it.variable] = "dummyValue"
    }
    def res = closure.call()
    list.forEach {
      env.remove(it.variable)
    }
    return res
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    env.JOB_NAME = "folder/myJob"
    env.BRANCH_NAME = "master"
    env.BUILD_NUMBER = "123"
    env.JENKINS_URL = "http://jenkins.example.com:8080"
    env.RUN_DISPLAY_URL = "http://jenkins.example.com:8080/job/folder/job/myJob/display/redirect"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("withEnvWrapper", [Closure.class], { closure -> closure.call() })
    helper.registerAllowedMethod("script", [Closure.class], { closure -> closure.call() })
    helper.registerAllowedMethod("pipeline", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("agent", [String.class], { "OK" })
    helper.registerAllowedMethod("agent", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("label", [String.class], { "OK" })
    helper.registerAllowedMethod("stages", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("steps", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("post", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("success", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("aborted", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("failure", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("unstable", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("always", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("dir", [String.class, Closure.class], { path, body -> body() })
    helper.registerAllowedMethod("when", [Closure.class], { "OK" })
    helper.registerAllowedMethod("parallel", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("failFast", [Boolean.class], { "OK" })
    helper.registerAllowedMethod("script", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("options", [Closure.class], { "OK" })
    helper.registerAllowedMethod("environment", [Closure.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("deleteDir", [], { "OK" })
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], withEnvInterceptor)
    helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], withCredentialsInterceptor)
    helper.registerAllowedMethod("log", [Map.class], {m -> println(m.text)})
    helper.registerAllowedMethod("readJSON", [Map.class], { m ->
      return readJSON(m)
    })
    helper.registerAllowedMethod("error", [String.class], {s ->
      printCallStack()
      throw new Exception(s)
    })
    helper.registerAllowedMethod("toJSON", [String.class], { s ->
      def script = loadScript("vars/toJSON.groovy")
      return script.call(s)
    })
    helper.registerAllowedMethod("mail", [Map.class], { m ->
      println("Subject: ${m.subject}")
      println("Body: \n${m.body}")
      def f = new File('mail-out.html')
      f.write(m.body)
    })
  }

  def readJSON(params){
    def jsonSlurper = new groovy.json.JsonSlurper()
    def jsonText = params.text
    if(params.file){
      File f = new File("src/test/resources/${params.file}")
      jsonText = f.getText()
    }
    return jsonSlurper.parseText(jsonText)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("src/co/elastic/NotificationManager.groovy")
    def f = new File("src/test/resources/console-100-lines.log")
    script.notifyEmail(
      build: readJSON(file: "build-info.json"),
      buildStatus: "SUCCESSFUL",
      emailRecipients: ["me@example.com"],
      testsSummary: readJSON(file: "tests-summary.json"),
      changeSet: readJSON(file: "changeSet-info.json"),
      statsUrl: "https://ecs.example.com/app/kibana",
      log: f.getText(),
      testsErrors: readJSON(file: "tests-errors.json"),
      stepsErrors: readJSON(file: "steps-errors.json")
    )
    printCallStack()

    //"NotificationManager.mail" "SUCCESSFUL folder/myJob#master 123"
    //ERROR SENDING EMAIL
    assertJobStatusSuccess()
  }
}
