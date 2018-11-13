import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class OnPullRequestStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.GIT_BUILD_CAUSE = "pr"
    env.BRANCH_NAME = "branchSource"
    env.CHANGE_TARGET = "branchTarget"
    binding.setVariable('env', env)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    def isOK = false
    script.call({isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNo() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    env.GIT_BUILD_CAUSE = "other"
    def isOK = true
    script.call({isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testTo() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    def isOK = false
    script.call(to: 'branchTarget', {isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNoTo() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    def isOK = true
    script.call(to: 'branchNoExistes', {isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testFrom() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    def isOK = false
    script.call(from: 'branchSource', {isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNoFrom() throws Exception {
    def script = loadScript("vars/on_pull_request.groovy")
    def isOK = true
    script.call(from: 'branchNoExistes', {isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
}
