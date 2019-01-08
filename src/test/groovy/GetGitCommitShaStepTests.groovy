import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class GetGitCommitShaStepTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    String sha = '29480a51'
    def script = loadScript("vars/getGitCommitSha.groovy")
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git rev-parse HEAD'.equals(map.script)) {
          return sha
      }
      return "0"
    })
    String ret = script.call()
    printCallStack()
    assertTrue(sha.equals(ret))
  }
}