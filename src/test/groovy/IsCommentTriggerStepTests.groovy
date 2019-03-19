import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import hudson.model.Cause;


class IsCommentTriggerStepTests extends BasePipelineTest {
  class IssueCommentCause extends Cause {
    private final String userLogin
    private final String comment

    public IssueCommentCause(final String userLogin, final String comment) {
      this.userLogin = userLogin
      this.comment = comment
    }

    public String getUserLogin() {
      return userLogin
    }

    public String getComment() {
      return comment
    }

    public String getShortDescription(){
      return String.format("%s commented: %s", userLogin, comment);
    }
  }

  class RawBuild {
    private final Cause cause

    public RawBuild(Cause cause){
      this.cause = cause
    }

    public Cause getCause(String clazz) {
      return cause
    }

    public List<Cause> getCauses(){
      List<Cause> list = new ArrayList()
      list.add(cause)
      return list
    }
  }

  Map env = [:]

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    env.WORKSPACE = "WS"
    binding.setVariable('env', env)
    helper.registerAllowedMethod("log", [Map.class], {m -> println m.text})
    helper.registerAllowedMethod("getGithubToken", {return 'TOKEN'})
    helper.registerAllowedMethod("githubApiCall", [Map.class], {return [login: 'user', company: '@elastic']})
  }

  @Test
  void test() throws Exception {
    Cause cause = new IssueCommentCause("admin","Started by a comment")
    binding.getVariable('currentBuild').rawBuild = new RawBuild(cause)
    def script = loadScript("vars/isCommentTrigger.groovy")
    def ret = script.call()
    printCallStack()
    assertTrue(ret)
    assertTrue('admin'.equals(env.BUILD_CAUSE_USER))
    assertJobStatusSuccess()
  }

  @Test
  void testNoCommentTriggered() throws Exception {
    binding.getVariable('currentBuild').rawBuild = new RawBuild(null)
    def script = loadScript("vars/isCommentTrigger.groovy")
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }

  @Test
  void testNoElasticUser() throws Exception {
    Cause cause = new IssueCommentCause("admin","Started by a comment")
    binding.getVariable('currentBuild').rawBuild = new RawBuild(cause)
    helper.registerAllowedMethod("githubApiCall", [Map.class], {return [login: 'user', company: '@none']})
    def script = loadScript("vars/isCommentTrigger.groovy")
    def ret = script.call()
    printCallStack()
    assertFalse(ret)
    assertJobStatusSuccess()
  }
}
