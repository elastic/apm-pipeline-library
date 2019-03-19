import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
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
      } else if('git rev-parse HEAD^'.equals(map.script)){
        return "previousCommit"
      } else if(map.script.startsWith("git branch -r --contains")){
        return "${sha}"
      }
      return ""
    })
    helper.registerAllowedMethod('getGitRepoURL', [], {return url})
    helper.registerAllowedMethod('getGitCommitSha', [], {return sha})
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
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
    env.CHANGE_ID = "NotEmpty"
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

  @Test
  void testChangeTargetBaseCommitOnNoMergeChangesInPR() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.CHANGE_TARGET = "NotEmpty"
    env.CHANGE_ID = "NotEmpty"
    env.GIT_COMMIT = sha
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(sha.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnNoGitCommit() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.CHANGE_TARGET = "NotEmpty"
    env.CHANGE_ID = "NotEmpty"
    env.GIT_COMMIT = null
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(sha.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnMergeChangesInPR() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.CHANGE_ID = "NotEmpty"
    env.CHANGE_TARGET = "NotEmpty"
    env.GIT_COMMIT = 'different'
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('pr'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    println(binding.getVariable('env').GIT_BASE_COMMIT)
    assertTrue('previousCommit'.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }

  @Test
  void testChangeTargetBaseCommitOnBranch() throws Exception {
    def script = loadScript("vars/githubEnv.groovy")
    registerMethods()
    env.CHANGE_ID = null
    env.CHANGE_TARGET = null
    env.GIT_COMMIT = sha
    script.call()
    printCallStack()
    assertTrue('org'.equals(binding.getVariable('env').ORG_NAME))
    assertTrue('repo'.equals(binding.getVariable('env').REPO_NAME))
    assertTrue(sha.equals(binding.getVariable('env').GIT_SHA))
    assertTrue('commit'.equals(binding.getVariable('env').GIT_BUILD_CAUSE))
    assertTrue(sha.equals(binding.getVariable('env').GIT_BASE_COMMIT))
  }
}
