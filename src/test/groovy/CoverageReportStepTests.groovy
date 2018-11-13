import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class CoverageReportStepTests extends BasePipelineTest {
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    helper.registerAllowedMethod("publishHTML", [Map.class],  null)
    helper.registerAllowedMethod("cobertura", [Map.class], null)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/coverageReport.groovy")
    script.call("folder")
    printCallStack()
    assertJobStatusSuccess()
  }
}