import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GitCheckoutStepTests extends BasePipelineTest {
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

    binding.getVariable('currentBuild').getBuildCauses = {
      return null
    }
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/gitCheckout.groovy")
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
    def script = loadScript("vars/gitCheckout.groovy")
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
    def script = loadScript("vars/gitCheckout.groovy")
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
  void testErrorBranchIncomplete() throws Exception {
    def script = loadScript("vars/gitCheckout.groovy")
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
    def script = loadScript("vars/gitCheckout.groovy")
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
    def script = loadScript("vars/gitCheckout.groovy")
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
  void testManuallyTriggered() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UserIdCause',
          shortDescription: 'Started by user admin',
          userId: 'admin',
          userName: 'admin'
        ]
      ]
    }
    def script = loadScript("vars/gitCheckout.groovy")
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
}