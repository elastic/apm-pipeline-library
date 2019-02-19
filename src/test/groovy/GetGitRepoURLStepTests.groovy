import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class GetGitRepoURLStepTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    String url = 'http://github.com/org/repo.git'
    def script = loadScript("vars/getGitRepoURL.groovy")
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git config --get remote.origin.url'.equals(map.script)) {
          return url
      }
      return ""
    })
    String ret = script.call()
    printCallStack()
    assertTrue(url.equals(ret))
  }
}
