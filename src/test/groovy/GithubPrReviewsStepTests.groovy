import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GithubPrReviewsStepTests extends BasePipelineTest {
  Map env = [:]
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    helper.registerAllowedMethod("githubApiCall", [Map.class], { return [:]})
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/githubPrReviews.groovy")
    def pr = script.call(token: 'token', repo: 'org/repo', pr: 1)
    printCallStack()
    assertTrue(pr instanceof Map)
    assertJobStatusSuccess()
  }
  
  @Test
  void testErrorNoRepo() throws Exception {
    def script = loadScript("vars/githubPrReviews.groovy")
    def pr = script.call(token: 'token', pr: 1)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubPrReviews: no valid repository.')
    })
  }
  
  @Test
  void testErrorNoPR() throws Exception {
    def script = loadScript("vars/githubPrReviews.groovy")
    def pr = script.call(token: 'token', repo: 'org/repo')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('githubPrReviews: no valid PR ID.')
    })
  }
}