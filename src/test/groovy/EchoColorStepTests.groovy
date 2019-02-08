import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class EchoColorStepTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/echoColor.groovy")
    script.call(text: '[ERROR]', colorfg: 'red', colorbg: 'black')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("\u001B[31;40m[ERROR]\u001B[0m")
    })
  }
}
