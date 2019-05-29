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

class CodecovStepTests extends BasePipelineTest {
  Map env = [:]
  String url = 'http://github.com/org/repo.git'

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

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.BRANCH_NAME = "branch"
    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("deleteDir", [], { "OK" })
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], withEnvInterceptor)
    helper.registerAllowedMethod("githubBranchRef", [], {return "master"})
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("readJSON", [Map.class], {return [
      head: [
        repo: [
          owner: [
            login: 'user'
          ]
        ],
        ref: 'refs/'
      ]]})
    helper.registerAllowedMethod('getGitRepoURL', [], {return url})
    helper.registerAllowedMethod("getVaultSecret", [Map.class], { m ->
      if("secret-codecov".startsWith(m.secret)){
        return [data: [ value: 'codecov-token']]
      }
      return null
    })
    helper.registerAllowedMethod("getVaultSecret", [String.class], { s ->
      if("repo-codecov".startsWith(s)){
        return [data: [ value: 'codecov-token']]
      }
      return null
    })
    helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], { list, closure ->
      list.each{ map ->
        map.each{ key, value ->
          if("variable".equals(key)){
            binding.setVariable("${value}", "defined")
          }
        }
      }
      def res = closure.call()
      list.each{ map ->
        map.each{ key, value ->
          if("variable".equals(key)){
            binding.setVariable("${value}", null)
          }
        }
      }
      return res
    })
  }

  @Test
  void testNoRepo() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Codecov: No repository specified.")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testNoToken() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call(repo: "noToken", secret: "secret-bad")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Codecov: Repository not found: noToken")
    })
    assertJobStatusSuccess()
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call(repo: "repo", basedir: "ws", secret: "secret-codecov")
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testCache() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call(repo: "repo", basedir: "ws", secret: "secret-codecov")
    script.call(repo: "repo", basedir: "ws", secret: "secret-codecov")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Codecov: get the token from Vault.")
    })
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Codecov: get the token from cache.")
    })
    assertJobStatusSuccess()
  }
}
