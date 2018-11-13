import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class RunIntegrationTestAxisStepTests extends BasePipelineTest {
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    helper.registerAllowedMethod('sh', [Map.class], { 'OK' })
    helper.registerAllowedMethod('sh', [String.class], { 'OK' })
    helper.registerAllowedMethod('deleteDir', [], { 'OK' })
    helper.registerAllowedMethod('unstash', [String.class], { 'OK' })
    helper.registerAllowedMethod('withEnvWrapper', [Closure.class], { c -> return c.call() })
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod('readYaml', [Map.class], { m ->
      m.each{ key, val ->
        if('file'.equals(key) && 'tests/versions/apm_server.yml'.equals(val)){
          return [APM_SERVER: ['1','2','3']]
        }
      }
      return [GO_AGENT: ['1','2','3']]
    })
    helper.registerAllowedMethod('parallel', [Map.class], { m ->
      m.each{ key, value ->
        value.call()
      }
    })
    helper.registerAllowedMethod('stepIntegrationTest', [Map.class], { m -> println m })
  }
  
  @Test
  void testNoValidAgent() throws Exception {
    def script = loadScript('vars/runIntegrationTestAxis.groovy')
    try{
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains('runIntegrationTestAxis: no valid agentType')
    })
    assertJobStatusFailure()
  }
  
  @Test
  void testNoSource() throws Exception {
    def script = loadScript('vars/runIntegrationTestAxis.groovy')
    try{
      script.call(agentType: 'go')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'error'
    }.any { call ->
        callArgsToString(call).contains('runIntegrationTestAxis: no valid source to unstash')
    })
    assertJobStatusFailure()
  }
  
  @Test
  void test() throws Exception {
    def script = loadScript('vars/runIntegrationTestAxis.groovy')
    script.call(agentType: 'go', source: 'source', elasticStack: 'master', baseDir: 'baseDir')
    printCallStack()
    assertJobStatusSuccess()
  }
}