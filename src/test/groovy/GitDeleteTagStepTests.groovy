import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GitDeleteTagStepTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    binding.setVariable("BUILD_TAG", "tag")
    helper.registerAllowedMethod('sh', [String.class], { "OK" })
    helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], { list, closure ->
      def res = closure.call()
      return res
    })
    helper.registerAllowedMethod('usernamePassword', [Map.class], { m -> 
      m.each{ k, v ->
        binding.setVariable("${v}", "defined")
      }
    })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/gitDeleteTag.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }
}