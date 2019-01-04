import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class HttpRequestStepTests extends BasePipelineTest {

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
    def script = loadScript("vars/httpRequest.groovy")
    def body = script.call(url: "https://www.google.com")
      
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Response Code: 200")
    })
    assertJobStatusSuccess()
  }
  
  @Test
  void testGetWithParams() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    def body = script.call(url: "https://www.google.com",
      method: "GET", headers: ["User-Agent": "dummy"])
      
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Response Code: 200")
    })
    assertJobStatusSuccess()
  }
  
  
  @Test
  void testPostWithParams() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    def body = script.call(url: "https://duckduckgo.com",
      method: "POST", 
      headers: ["User-Agent": "dummy"],
      data: "q=value&other=value")
      
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "log"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Response Code: 200")
    })
    assertJobStatusSuccess()
  }

  @Test
  void testNoURL() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    try {
      script.call()
    } catch(e) { 
      //NOOP 
    }
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Invalid URL")
    })
  }
  
  @Test
  void testInvalidURL() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    try {
      script.call(url: "htttttp://google.com")
    } catch(e) { 
      //NOOP 
    }
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Invalid URL")
    })
  }
  
  @Test
  void testConnectionError() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    try {
      script.call(url: "https://thisdomaindoesnotexistforsure.com")
    } catch(e) { 
      //NOOP 
    }
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Failure connecting to the service")
    })
  }
  
  @Test
  void testHttpError() throws Exception {
    def script = loadScript("vars/httpRequest.groovy")
    try {
      script.call(url: "https://google.com",
        method: "POST",
        headers: ["User-Agent": "dummy"])
    } catch(e) { 
      //NOOP 
    }
    assertTrue(helper.callStack.findAll { call ->
        call.methodName == "error"
    }.any { call ->
        callArgsToString(call).contains("httpRequest: Failure connecting to the service")
    })
  }
}