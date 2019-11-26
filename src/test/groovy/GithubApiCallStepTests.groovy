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

class GithubApiCallStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/githubApiCall.groovy'

  def shInterceptor = {
    return """[{
      "id": 186086539,
      "node_id": "MDE3OlB1bGxSZXF1ZXN0UmV2aWV3MTg2MDg2NTM5",
      "user": {
        "login": "githubusername",
        "type": "User",
        "site_admin": false
      },
      "body": "",
      "state": "APPROVED",
      "pull_request_url": "https://api.github.com/repos/org/repo/pulls/1",
      "author_association": "MEMBER",
      "submitted_at": "2018-12-18T14:13:16Z",
      "commit_id": "4457d4e98f91501bb7914cbb29e440a857972fee"
    }]"""
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], shInterceptor)
    def script = loadScript(scriptName)
    def ret = script.call(url: "dummy", token: "dummy")
    printCallStack()
    assertTrue(ret[0].user.login == "githubusername")
    assertJobStatusSuccess()
  }

  @Test
  void testErrorNoToken() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], shInterceptor)
    def script = loadScript(scriptName)
    try {
      script.call(url: "dummy")
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: no valid Github token.')
    })
  }

  @Test
  void testErrorNoUrl() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], shInterceptor)
    def script = loadScript(scriptName)
    try {
      script.call(token: 'dummy')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: no valid Github REST API URL.')
    })
  }

  @Test
  void testRequestError() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], {
      return """{
        "Code": "404",
        "message": "Not Found",
        "documentation_url": "https://developer.github.com/v3"
      }"""
    })
    def script = loadScript(scriptName)
    try {
      script.call(token: "dummy", url: "http://error")
    } catch(e) {
      println e.toString()
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: The REST API call http://error return the message : Not Found')
    })
  }

  @Test
  void testRequestFailure() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], {
      throw new Exception('Failure')
    })
    def script = loadScript(scriptName)
    try{
      script.call(token: "dummy", url: "http://error")
    } catch(e) {
      println e.toString()
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: The REST API call http://error return the message : java.lang.Exception: Failure')
    })
  }

  @Test
  void testCache() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], shInterceptor)
    def script = loadScript(scriptName)
    def ret0 = script.call(url: "dummy", token: "dummy")
    def ret1 = script.call(url: "dummy", token: "dummy")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("githubApiCall: get the JSON from GitHub.")
    })
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("githubApiCall: get the JSON from cache.")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testResponseNull() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], {
      return null
    })
    def script = loadScript(scriptName)
    try {
      script.call(token: "dummy", url: "http://error")
    } catch(e) {
      println e.toString()
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: somethign happened with the toJson')
    })
  }

  @Test
  void testToJSONFailed() throws Exception {
    helper.registerAllowedMethod("httpRequest", [Map.class], {
      throw new Exception('Failure')
    })
    helper.registerAllowedMethod('toJSON', [String.class], { s ->
      return null
    })
    def script = loadScript(scriptName)
    try {
      script.call(token: "dummy", url: "http://error")
    } catch(e) {
      println e.toString()
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubApiCall: somethign happened with the toJson')
    })
  }
}
