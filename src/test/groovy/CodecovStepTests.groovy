import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class CodecovStepTests extends BasePipelineTest {
  Map env = [:]
  String url = 'http://github.com/org/repo.git'
  
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
      binding.setVariable("${fields[0]}", "${fields[1]}")
    }
    def res = closure.call()
    list.forEach {
      def fields = it.split("=")
      binding.setVariable("${fields[0]}", null)
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
    helper.registerAllowedMethod("deleteDir", [], { "OK" })
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], withEnvInterceptor)
    helper.registerAllowedMethod("readJSON", [Map.class], {return [
      head: [
        repo: [
          owner: [
            login: 'user'
          ]
        ],
        ref: 'refs/'
      ]]})
    helper.registerAllowedMethod('getGitRepoURL', [], {return url})
    helper.registerAllowedMethod("getVaultSecret", [String.class], { s ->
      if("repo-codecov".startsWith(s)){
        return [data: [ value: 'codecov-token']] 
      }
      return null
    })
    helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], { list, closure ->
      list.each{ map ->
        map.each{ key, value -> 
          if("variable".equals(key)){
            binding.setVariable("${value}", "defined")
          }
        }
      }
      def res = closure.call()
      list.each{ map ->
        map.each{ key, value -> 
          if("variable".equals(key)){
            binding.setVariable("${value}", null)
          }
        }
      }
      return res
    })
  }

  @Test
  void testNoRepo() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call()
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("Codecov: No repository specified.")
    })
    assertJobStatusSuccess()
  }
  
  @Test
  void testNoToken() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call("noToken")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "echo"
    }.any { call ->
        callArgsToString(call).contains("Codecov: Repository not found: noToken")
    })
    assertJobStatusSuccess()
  }
  
  @Test
  void test() throws Exception {
    def script = loadScript("vars/codecov.groovy")
    script.call("repo")
    printCallStack()
    assertJobStatusSuccess()
  }
}