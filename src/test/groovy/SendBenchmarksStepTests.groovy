import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class SendBenchmarksStepTests extends BasePipelineTest {
  Map env = [:]
    
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
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.BRANCH_NAME = "branch"
    env.CHANGE_ID = "29480a51"
    env.ORG_NAME = "org"
    env.REPO_NAME = "repo"
    env.GITHUB_TOKEN = "TOKEN"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { "OK" })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("error", [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod("getVaultSecret", [String.class], { s ->
      if("secret".equals(s) || "java-agent-benchmark-cloud".equals(s)){
        return [data: [ user: 'user', password: 'password']] 
      }
      if("secretError".equals(s)){
        return [errors: 'Error message'] 
      }
      if("secretNotValid".equals(s)){
        return [data: [ user: null, password: null]] 
      }
      return null
    })
  }
  
  @Test
  void test() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }
  
  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    script.call(file: 'bench.out', index: 'index-name', url: 'https://vault.example.com', secret: 'secret', archive: true)
    printCallStack()
    assertJobStatusSuccess()
  }
  
  @Test
  void testSecretNotFound() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    try{
      script.call(secret: 'secretNotExists')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: was not possible to get authentication info to send benchmarks")
    })
    assertJobStatusFailure()
  }
  
  @Test
  void testSecretError() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    try {
      script.call(secret: 'secretError')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: Unable to get credentials from the vault: Error message")
    })
    assertJobStatusFailure()
  }
  
  @Test
  void testWrongProtocol() throws Exception {
    def script = loadScript("vars/sendBenchmarks.groovy")
    try {
      script.call(secret: 'secret', url: 'ht://wrong.example.com')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("Benchmarks: unknow protocol, the url is not http(s).")
    })
    assertJobStatusFailure()
  }
}
