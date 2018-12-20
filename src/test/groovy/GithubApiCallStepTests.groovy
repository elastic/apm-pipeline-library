import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class GithubApiCallStepTests extends BasePipelineTest {
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
  
  def shInterceptor = {
    if(env?.url == "http://error"){
      return """{
        "message": "Not Found",
        "documentation_url": "https://developer.github.com/v3"
      }"""
    }
    return """[
      {
        "id": 186086539,
        "node_id": "MDE3OlB1bGxSZXF1ZXN0UmV2aWV3MTg2MDg2NTM5",
        "user": {
          "login": "githubusername",
          "type": "User",
          "site_admin": false
        },
        "body": "",
        "state": "APPROVED",
        "pull_request_url": "https://api.github.com/repos/elastic/apm-agent-java/pulls/388",
        "author_association": "MEMBER",
        "submitted_at": "2018-12-18T14:13:16Z",
        "commit_id": "4457d4e98f91501bb7914cbb29e440a857972fee"
      }
    ]"""
  }
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    
    helper.registerAllowedMethod("sh", [Map.class], shInterceptor)
    helper.registerAllowedMethod("sh", [String.class], shInterceptor)
    helper.registerAllowedMethod("wrap", [Map.class, Closure.class], wrapInterceptor)
    helper.registerAllowedMethod("githubBranchRef", [], {return "master"})
    helper.registerAllowedMethod("readJSON", [Map.class], {return [:]})
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/githubApiCall.groovy")
    def ret = script.call(url: "dummy", token: "dummy")
    printCallStack()
    assertTrue(ret instanceof Map)
    assertJobStatusSuccess()
  }
  
  @Test
  void testErrorNoToken() throws Exception {
    def script = loadScript("vars/githubApiCall.groovy")
    script.call(url: "dummy")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('makeGithubApiCall: no valid Github token.')
    })
  }
  
  @Test
  void testErrorNoUrl() throws Exception {
    def script = loadScript("vars/githubApiCall.groovy")
    script.call(token: "dummy")
    printCallStack()
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains('makeGithubApiCall: no valid Github REST API URL.')
    })
  }
  
  @Test
  void testRequestError() throws Exception {
    def script = loadScript("vars/githubApiCall.groovy")
    env.url = "http://error"
    def ret = script.call(token: "dummy", url: "http://error")
    printCallStack()
    assertTrue(ret instanceof Map)
    assertJobStatusSuccess()
  }
}