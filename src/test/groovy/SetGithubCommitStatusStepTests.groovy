import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertTrue

class SetGithubCommitStatusStepTests extends BasePipelineTest {

  String url = 'http://github.com/org/repo.git'
  String sha = '29480a51'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    helper.registerAllowedMethod('getGitRepoURL', [], {return url})
    helper.registerAllowedMethod('getGitCommitSha', [], {return sha})
  }

  @Test
  void test() throws Exception {
    def script = loadScript("vars/setGithubCommitStatus.groovy")
    script.call()
    printCallStack()
    assertJobStatusSuccess()
  }

  @Test
  void testParams() throws Exception {
    def script = loadScript("vars/setGithubCommitStatus.groovy")
    script.call(repoUrl: url, commitSha: sha, message: 'Build result.', state: "SUCCESS")
    printCallStack()
    assertJobStatusSuccess()
  }
}
