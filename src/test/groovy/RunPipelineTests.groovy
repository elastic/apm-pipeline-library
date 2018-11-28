import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class RunPipelineTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod("pipeline", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("agent", [String.class], { "OK" })
    helper.registerAllowedMethod("agent", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("label", [String.class], { "OK" })
    helper.registerAllowedMethod("stages", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("steps", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("post", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("success", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("aborted", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("failure", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("unstable", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("always", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("when", [Closure.class], { "OK" })
    helper.registerAllowedMethod("parallel", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("failFast", [Boolean.class], { "OK" })
    helper.registerAllowedMethod("script", [Closure.class], { body -> body() })
    helper.registerAllowedMethod("options", [Closure.class], { "OK" })
    helper.registerAllowedMethod("environment", [Closure.class], { "OK" })
  }

  @Test
  void testDefault() throws Exception {
    def script = loadScript("vars/runPipeline.groovy")
    script.any = "any"
    script.call(name: "default")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("Hello, I am pipeline")
    })
  }
  
  @Test
  void testNoName() throws Exception {
    def script = loadScript("vars/runPipeline.groovy")
    script.any = "any"
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("Hello, I am pipeline")
    })
  }
  
  @Test
  void testNameMatch() throws Exception {
    def script = loadScript("vars/runPipeline.groovy")
    script.call(name: 'test')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("Hello, I am Test pipeline")
    })
  }
}