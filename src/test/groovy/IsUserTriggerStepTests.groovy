import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse


class IsUserTriggerStepTests extends BasePipelineTest {
  Map env = [:]

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
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.model.Cause$UserIdCause',
          shortDescription: 'Started by user admin',
          userId: 'admin',
          userName: 'admin'
        ]
      ]
    }

    def script = loadScript("vars/isUserTrigger.groovy")
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue('admin'.equals(env.BUILD_CAUSE_USER))
    assertJobStatusSuccess()
  }

  @Test
  void testFalse() throws Exception {
    binding.getVariable('currentBuild').getBuildCauses = {
      return [
        [
          _class: 'hudson.triggers.TimerTrigger$TimerTriggerCause',
          shortDescription: 'Started by a timmer',
        ]
      ]
    }

    def script = loadScript("vars/isUserTrigger.groovy")
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
