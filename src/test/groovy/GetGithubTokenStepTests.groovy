import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GetGithubTokenStepTests extends BasePipelineTest {
  Map env = [:]

  def withCredentialsInterceptor = { list, closure ->
    list.forEach {
      env[it.variable] = "dummyValue"
    }
    def res = closure.call()
    list.forEach {
      env.remove(it.variable)
    }
    return res
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable('env', env)
    helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], withCredentialsInterceptor)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/getGithubToken.groovy")
    def value = script.call()
    printCallStack()
    assertTrue(value == "dummyValue")
    assertJobStatusSuccess()
  }

  @Test
  void testCredentialsId() throws Exception {
    def script = loadScript("vars/getGithubToken.groovy")
    def value = script.call(credentialsId: "dummy")
    printCallStack()
    assertTrue(value == "dummyValue")
    assertJobStatusSuccess()
  }
}
