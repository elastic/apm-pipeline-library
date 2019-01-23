import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import java.util.Base64
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class Base64encodeStepTests extends BasePipelineTest {
  Map env = [:]
  def text = "dummy"
  def encoding = "UTF-8"
  def resultToCheck = Base64.getEncoder().encodeToString(text.toString().getBytes(encoding));

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/base64encode.groovy")
    def result = script.call(text: "dummy")
    printCallStack()
    assertTrue(resultToCheck == result)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/base64encode.groovy")
    def result = script.call(text: "dummy", encoding: "UTF-8")
    printCallStack()
    assertTrue(resultToCheck == result)
    assertJobStatusSuccess()
  }
}
