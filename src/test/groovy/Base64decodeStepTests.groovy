import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import java.util.Base64
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class Base64decodeStepTests extends BasePipelineTest {
  Map env = [:]
  def text = "dummy"
  def encoding = "UTF-8"

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
    def script = loadScript("vars/base64decode.groovy")
    def result = script.call(input: "ZHVtbXk=")
    printCallStack()
    assertTrue(text == result)
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/base64decode.groovy")
    def result = script.call(input: "ZHVtbXk=", encoding: "UTF-8")
    printCallStack()
    assertTrue(text == result)
    assertJobStatusSuccess()
  }
}
