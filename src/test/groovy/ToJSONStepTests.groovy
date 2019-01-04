import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import net.sf.json.JSONObject
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class ToJSONStepTests extends BasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("error", [String.class], {s -> 
      printCallStack()
      throw new Exception(s)
      })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/toJSON.groovy")
    def obj = script.call("{'dummy': 'value'}")
    printCallStack()
    assertTrue(obj instanceof JSONObject)
    assertJobStatusSuccess()
  }
  
  @Test
  void testNoJSON() throws Exception {
    def script = loadScript("vars/toJSON.groovy")
    def obj = script.call("")
    printCallStack()
    assertTrue(obj == null)
    assertJobStatusSuccess()
  }
  
  @Test
  void testPOJO() throws Exception {
    def script = loadScript("vars/toJSON.groovy")
    def pojo = [p1: 'value', p2: 'value']
    def obj = script.call(pojo)
    printCallStack()
    assertTrue(obj instanceof JSONObject)
    assertJobStatusSuccess()
  }
}