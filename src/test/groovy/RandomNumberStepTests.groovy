import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class RandomNumberStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/randomNumber.groovy")
    def i = script.call()
    printCallStack()
    assertTrue(i > 0 && i <= 100)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/randomNumber.groovy")
    def i = script.call(max: 2, min: 1)
    printCallStack()
    assertTrue(i >= 1 && i <= 2)
    assertJobStatusSuccess()
  }
}
