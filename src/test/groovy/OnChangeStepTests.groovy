import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class OnChangeStepTests extends BasePipelineTest {
  Map env = [:]
    
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.GIT_BUILD_CAUSE = "commit"
    env.BRANCH_NAME = "branch"
    binding.setVariable('env', env)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/on_change.groovy")
    def isOK = false
    script.call({isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testPr() throws Exception {
    def script = loadScript("vars/on_change.groovy")
    env.GIT_BUILD_CAUSE = "pr"
    def isOK = false
    script.call({isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNo() throws Exception {
    def script = loadScript("vars/on_change.groovy")
    env.GIT_BUILD_CAUSE = "other"
    def isOK = true
    script.call({isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testTo() throws Exception {
    def script = loadScript("vars/on_change.groovy")
    def isOK = false
    script.call(to: 'branch', {isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNoTo() throws Exception {
    def script = loadScript("vars/on_change.groovy")
    def isOK = true
    script.call(to: 'branchNoExistes', {isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
}
