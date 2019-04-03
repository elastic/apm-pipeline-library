import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class DockerLoginStepTests extends BasePipelineTest {
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

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)

    helper.registerAllowedMethod("sh", [Map.class], { m -> println m.script })
    helper.registerAllowedMethod("sh", [String.class], { "OK" })
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("getVaultSecret", [Map.class], {
      return [user: "my-user", password: "my-password"]
      })
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/dockerLogin.groovy")
    script.call(secret: 'secret/team/ci/secret-name')
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sh"
    }.any { call ->
        callArgsToString(call).contains("docker login -u my-user -p my-password docker.io")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testRegistry() throws Exception {
    def script = loadScript("vars/dockerLogin.groovy")
    script.call(secret: 'secret/team/ci/secret-name', registry: "other.docker.io")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "sh"
    }.any { call ->
        callArgsToString(call).contains("docker login -u my-user -p my-password other.docker.io")
    })
    assertJobStatusSuccess()
  }
}
