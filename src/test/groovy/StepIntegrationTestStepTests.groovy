import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class StepIntegrationTestStepTests extends BasePipelineTest {
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    binding.setVariable('WORKSPACE', 'WS')
    helper.registerAllowedMethod('sh', [Map.class], { 'OK' })
    helper.registerAllowedMethod('sh', [String.class], { 'OK' })
    helper.registerAllowedMethod('writeFile', [Map.class], { 'OK' })
    helper.registerAllowedMethod('junit', [Map.class], { 'OK' })
    helper.registerAllowedMethod('echoColor', [Map.class], { m -> println m })
    helper.registerAllowedMethod('deleteDir', [], { 'OK' })
    helper.registerAllowedMethod('unstash', [String.class], { 'OK' })
    helper.registerAllowedMethod('withEnvWrapper', [Closure.class], { c -> return c.call() })
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
  }
  
  @Test
  void testNoValidAgent() throws Exception {
    def script = loadScript('vars/stepIntegrationTest.groovy')
    try{
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains('stepIntegrationTest: no valid agentType')
    })
    assertJobStatusFailure()
  }
  
  @Test
  void testNoSource() throws Exception {
    def script = loadScript('vars/stepIntegrationTest.groovy')
    try{
      script.call(agentType: 'go')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains('stepIntegrationTest: no valid source to unstash')
    })
    assertJobStatusFailure()
  }
  
  @Test
  void test() throws Exception {
    def script = loadScript('vars/stepIntegrationTest.groovy')
    script.call(agentType: 'go', source: 'source', baseDir: 'baseDir', tag: 'tag')
    printCallStack()
    assertJobStatusSuccess()
  }
}