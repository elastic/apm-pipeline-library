import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class CheckoutElasticDocsToolsTests extends BasePipelineTest {
  Map env = [:]
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    
    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/checkoutElasticDocsTools.groovy")
    script.call(dir: "folder")
    printCallStack()
    assertJobStatusSuccess()
  }
  
  @Test
  void testNoParam() throws Exception {
    def script = loadScript("vars/checkoutElasticDocsTools.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }
}