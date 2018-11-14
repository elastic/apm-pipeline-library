import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GetVaultSecretStepTests extends BasePipelineTest {
  
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

    helper.registerAllowedMethod('sh', [Map.class], { m -> 
      if(m.script.contains("VAULT_TOKEN")){
        return "{plaintext: '12345', encrypted: 'ABCDE'}"
      }
    })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("readJSON", [Map.class], {return [plaintext: '12345', encrypted: 'ABCDE'] })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/getVaultSecret.groovy")
    def jsonValue = script.call()
    assertTrue(jsonValue.plaintext == '12345')
    printCallStack()
    assertJobStatusSuccess()
  }
}