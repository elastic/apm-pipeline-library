// jenkins-test-harness does not handle detached plugins properly
// see https://issues.jenkins.io/browse/JENKINS-60295?focusedCommentId=400912&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-400912
package hudson
import java.util.logging.Logger
import org.jvnet.hudson.test.TestPluginManager
/**
 * Modify plugin load behavior for jenkins for test
 */
class CustomPluginManager extends TestPluginManager {
    private static final Logger LOGGER = Logger.getLogger( CustomPluginManager.name )

    // Skip loading all detached plugins as they conflict with our explicit build.gradle jenkinsPlugins dependencies
    @Override
    void considerDetachedPlugin( String shortName ) {
        LOGGER.info( 'Skipping load of detached plugin: ' + shortName )
    }
}
