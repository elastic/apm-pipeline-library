import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class LogStepTests extends BasePipelineTest {
  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    helper.registerAllowedMethod("echoColor", [Map.class], { m ->
      def echoColor = loadScript("vars/echoColor.groovy")
      echoColor.call(m)
    })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/log.groovy")
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    script.call(text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[DEBUG]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testDebug() throws Exception {
    def script = loadScript("vars/log.groovy")
    env.PIPELINE_LOG_LEVEL = 'DEBUG'
    script.call(level: 'DEBUG', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[DEBUG]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testInfo() throws Exception {
    def script = loadScript("vars/log.groovy")
    script.call(level: 'INFO', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[INFO]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testWarn() throws Exception {
    def script = loadScript("vars/log.groovy")
    env.PIPELINE_LOG_LEVEL = 'WARN'
    script.call(level: 'WARN', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[WARN]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testError() throws Exception {
    def script = loadScript("vars/log.groovy")
    env.PIPELINE_LOG_LEVEL = 'ERROR'
    script.call(level: 'ERROR', text: "message")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("message")
        callArgsToString(call).contains("[ERROR]")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testLevel() throws Exception {
    def script = loadScript("vars/log.groovy")
    env.PIPELINE_LOG_LEVEL = 'WARN'
    script.call(level: 'DEBUG', text: "messageDEBUG")
    script.call(level: 'INFO', text: "messageINFO")
    script.call(level: 'WARN', text: "messageWARN")
    script.call(level: 'ERROR', text: "messageERROR")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        !callArgsToString(call).contains("[DEBUG]")
        !callArgsToString(call).contains("[INFO]")
        callArgsToString(call).contains("[WARN]")
        callArgsToString(call).contains("[ERROR]")
    })
    assertJobStatusSuccess()
  }
}
