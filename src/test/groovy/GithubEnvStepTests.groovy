import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before;
import org.junit.Test;
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.junit.Assert.assertTrue

class GithubEnvStepTests extends BasePipelineTest {
  
  String url = 'http://github.com/org/repo.git'
  String sha = '29480a51'
  Map env = [:]
  
  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    
    env.GIT_URL = null
    binding.setVariable('env', env)
  }

  void registerMethods(){
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git rev-list HEAD --parents -1'.equals(map.script)) {
          return "${sha} ${sha}"
      }
      return ""
    })
    helper.registerAllowedMethod('getGitRepoURL', [], {return url})
    helper.registerAllowedMethod('getGitCommitSha', [], {return sha})
  }
  
  @Test
  void testNoGitURL() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }
  
  @Test
  void testGitUrl() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.GIT_URL = url
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }
  
  @Test
  void testChangeTarget() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.CHANGE_TARGET = "NotEmpty"
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }
    
  @Test
  void testMerge() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    helper.registerAllowedMethod(method('sh', Map.class), { map ->
      if ('git rev-list HEAD --parents -1'.equals(map.script)) {
          return "${sha} ${sha} ${sha}"
      }
      return ""
    })
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('merge'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }
  
  @Test
  void testSshUrl() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.GIT_URL = 'git@github.com:org/repo.git'
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
  }
}