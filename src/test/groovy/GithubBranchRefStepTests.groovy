import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GithubBranchRefStepTests extends BasePipelineTest {
  Map env = [:]
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.WORKSPACE = 'WS'
    env.ORG_NAME = 'org'
    env.REPO_NAME = 'repo'
    env.CHANGE_ID = '1'
    env.BRANCH_NAME = 'master'
    binding.setVariable('env', env)
    
    helper.registerAllowedMethod('getGithubToken', [], {return 'dummy'})
    helper.registerAllowedMethod('githubPrInfo', [Map.class], {
      return [
        head: [
          ref: 'master',
          repo: [
            owner: [
              login: 'username'
            ]
          ]
        ],
        title: 'dummy PR', 
        user: [login: 'username'], 
        author_association: 'NONE'
        ]
      })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/githubBranchRef.groovy")
    def ret = script.call()
    printCallStack()
    assertTrue(ret == 'username/master')
    assertJobStatusSuccess()
  }
  
  @Test
  void testNoPR() throws Exception {
    env.CHANGE_ID = null
    def script = loadScript("vars/githubBranchRef.groovy")
    def ret = script.call()
    printCallStack()
    assertTrue(ret == 'master')
    assertJobStatusSuccess()
  }
  
  @Test
  void testEnvError() throws Exception {
    env.ORG_NAME = null
    env.REPO_NAME = null
    def script = loadScript("vars/githubBranchRef.groovy")
    def ret = script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubBranchRef: Environment not initialized, try to call githubEnv step before')
    })
  }
}