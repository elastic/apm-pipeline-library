import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class TarStepTests extends BasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod('sh', [String.class], { "OK" })
    helper.registerAllowedMethod("isUnix", [], {true})
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    binding.setVariable('WORKSPACE', "WS")
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/tar.groovy")
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }
  
  @Test
  void testError() throws Exception {
    def script = loadScript("vars/tar.groovy")
    helper.registerAllowedMethod('sh', [String.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: false, archive: true)
    printCallStack()
    assertJobStatusUnstable()
  }

  @Test
  void testAllowMissing() throws Exception {
    def script = loadScript("vars/tar.groovy")
    helper.registerAllowedMethod('sh', [String.class], { throw new Exception("Error") })
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: true, archive: false)
    printCallStack()
    assertJobStatusSuccess()
  }
    
  @Test
  void testIsNotUnix() throws Exception {
    def script = loadScript("vars/tar.groovy")
    helper.registerAllowedMethod("isUnix", [], {false})
    script.call(file:'archive.tgz', dir: 'folder', pathPrefix: 'folder', allowMissing: true)
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("tar step is compatible only with unix systems")
    })
    assertJobStatusSuccess()
  }
}