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

class GitCheckoutStepTests extends BasePipelineTest {
  String scriptName = 'vars/gitCheckout.groovy'
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("checkout", [String.class], { "OK" })
    helper.registerAllowedMethod("githubEnv", [], { "OK" })
    helper.registerAllowedMethod("githubPrCheckApproved", [], { return true })
    helper.registerAllowedMethod("withEnvWrapper", [Closure.class], { closure -> closure.call() })
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("isUserTrigger", {return false})
    helper.registerAllowedMethod("isCommentTrigger", {return false})
    binding.getVariable('currentBuild').getBuildCauses = {
      return null
    }
    helper.registerAllowedMethod('githubNotify', [Map.class], { m ->
      if(m.context.equalsIgnoreCase('failed')){
        updateBuildStatus('FAILURE')
        throw new Exception('Failed')
      }
    })
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = "BRANCH"
    script.scm = "SCM"
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Checkout SCM ${env.BRANCH_NAME}")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testBaseDir() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = "BRANCH"
    script.scm = "SCM"
    script.call(basedir: 'sub-folder')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Checkout SCM ${env.BRANCH_NAME}")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testBranch() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Checkout master")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testReferenceRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      reference: "repo")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("Checkout master")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testMergeRemoteRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      mergeRemote: "upstream",
      mergeTarget: "master")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("options:[mergeTarget:master, mergeRemote:upstream]]]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testMergeTargetRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      mergeTarget: "master")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("options:[mergeTarget:master, mergeRemote:origin]]]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testErrorBranchIncomplete() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("No valid SCM config passed.")
    })
  }

  @Test
  void testErrorBranchNoCredentials() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("No valid SCM config passed.")
    })
  }

  @Test
  void testErrorBranchNoRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("No valid SCM config passed.")
    })
  }

  @Test
  void testUserTriggered() throws Exception {
    helper.registerAllowedMethod("isUserTrigger", {return true})
    helper.registerAllowedMethod("isCommentTrigger", {return true})
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("No valid SCM config passed.")
    })
  }

  @Test
  void testCommentTriggered() throws Exception {
    helper.registerAllowedMethod("isCommentTrigger", {return true})
    def script = loadScript(scriptName)
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', branch: 'master',
      credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("No valid SCM config passed.")
    })
  }

  @Test
  void testFirstTimeContributorWithoutNotify() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod("githubPrCheckApproved", [], {
      updateBuildStatus('FAILURE')
      throw new Exception('githubPrCheckApproved: The PR is not approved yet')
    })
    script.scm = "SCM"
    try{
      script.call(basedir: 'sub-folder', branch: 'master',
        repo: 'git@github.com:elastic/apm-pipeline-library.git',
        credentialsId: 'credentials-id',
        reference: "repo")
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void testFirstTimeContributorWithNotify() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod("githubPrCheckApproved", [], {
      updateBuildStatus('FAILURE')
      throw new Exception('githubPrCheckApproved: The PR is not approved yet')
    })
    env.BRANCH_NAME = "BRANCH"
    script.scm = "SCM"
    try{
      script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: true)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "githubNotify"
    }.any { call ->
        callArgsToString(call).contains('context=First time contributor, description=It requires manual inspection, status=FAILURE')
    })
    assertJobStatusFailure()
  }

  @Test
  void testWithoutFirstTimeContributorWithNotify() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = "BRANCH"
    script.scm = "SCM"
    script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "githubNotify"
    }.any { call ->
        callArgsToString(call).contains('context=First time contributor, status=SUCCESS')
    })
    assertJobStatusSuccess()
  }
}
