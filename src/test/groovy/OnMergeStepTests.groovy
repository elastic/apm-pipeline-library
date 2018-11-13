import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class OnMergeStepTests extends BasePipelineTest {
  Map env = [:]
  String url = 'http://github.com/org/repo.git'
  String sha1 = '29480a51'
  String sha2 = '29480a52'
  String sha3 = '29480a53'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.GIT_BUILD_CAUSE = "merge"
    env.BRANCH_NAME = "branch1"
    binding.setVariable('env', env)

    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ("git name-rev --name-only ${sha1}".equals(map.script)) {
        return "remotes/origin/branch1"
      }
      if ("git name-rev --name-only ${sha2}".equals(map.script)) {
        return "remotes/origin/branch2"
      }
      if ("git name-rev --name-only ${sha3}".equals(map.script)) {
        return "remotes/origin/branch3"
      }
      if ('git rev-list HEAD --parents -1'.equals(map.script)) {
        return "${sha1} ${sha2} ${sha3}"
      }
      if ("git rev-parse \$(git --no-pager log -n1 | grep 'Merge:')".equals(map.script)) {
        return "${sha1} ${sha2} ${sha3}"
      }
      return "noMatch"
    })
    
    helper.registerAllowedMethod('unstash', [String.class], null)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    def isOK = false
    script.call({isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNo() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    env.GIT_BUILD_CAUSE = "other"
    def isOK = true
    script.call({isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testTo() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    def isOK = false
    script.call(to: 'branch1', {isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNoTo() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    def isOK = true
    script.call(to: 'branchNoExistes', {isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testFrom() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    def isOK = false
    script.call(from: 'branch3', {isOK = true})
    printCallStack()
    assertTrue(isOK)
  }
  
  @Test
  void testNoFrom() throws Exception {
    def script = loadScript("vars/on_merge.groovy")
    def isOK = true
    script.call(from: 'branch1', {isOK = false})
    printCallStack()
    assertTrue(isOK)
  }
}
