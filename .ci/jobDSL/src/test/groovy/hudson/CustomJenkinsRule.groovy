// jenkins-test-harness does not handle detached plugins properly
// see https://issues.jenkins.io/browse/JENKINS-60295?focusedCommentId=400912&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-400912
package hudson
import org.jvnet.hudson.test.JenkinsRule
import java.util.logging.Logger
/**
 * JenkinsRule to use in Job DSL scripts tests
 */
class CustomJenkinsRule extends JenkinsRule {
  private static final Logger LOGGER = Logger.getLogger( CustomJenkinsRule.name )
    CustomJenkinsRule() {
        super()
        LOGGER.info( 'Loading CustomJenkinsRule')
        this.pluginManager = new CustomPluginManager()
    }
}
