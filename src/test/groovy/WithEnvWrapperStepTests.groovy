import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class WithEnvWrapperStepTests extends BasePipelineTest {
  
  def wrapInterceptor = { map, closure ->
    map.each { key, value -> 
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", "${it.password}")
        }
      }
    }
    def res = closure.call()
    map.forEach { key, value ->
      if("varPasswordPairs".equals(key)){
        value.each{ it ->
          binding.setVariable("${it.var}", null)
        }
      }
    }
    return res
  }
  
  def withEnvInterceptor = { list, closure ->
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], fields[1])
    }
    def res = closure.call()
    list.forEach {
      def fields = it.split("=")
      binding.setVariable(fields[0], null)
    }
    return res
  }
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("deleteDir", [], { "OK" })
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], withEnvInterceptor)
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/withEnvWrapper.groovy")
    def isOK = false
    script.call({
      if( binding.getVariable("JOB_GCS_CREDENTIALS") == "apm-ci-gcs-plugin" 
        && binding.getVariable("JOB_GCS_BUCKET") == "apm-ci-artifacts/jobs"
        && binding.getVariable("NOTIFY_TO") == "infra-root+build@elastic.co"
        && binding.getVariable("JOB_GCS_CREDENTIALS") == "apm-ci-gcs-plugin"
        && binding.getVariable("JOB_GCS_BUCKET") == "apm-ci-artifacts/jobs"
        && binding.getVariable("NOTIFY_TO") == "infra-root+build@elastic.co"){
          isOK = true
        }
      })
    printCallStack()
    assertTrue(isOK)
    assertJobStatusSuccess()
  }
}