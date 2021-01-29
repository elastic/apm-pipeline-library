import groovy.util.FileNameFinder
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JobScriptsSpec extends Specification {
    @Shared
    @ClassRule
    JenkinsRule jenkinsRule = new JenkinsRule()

    @Unroll
    def 'test script #file.name'(File file) {
        given:
        def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))
        new DslScriptLoader(jobManagement).runScript(new File('jobs/folders.groovy').text)

        when:
        new DslScriptLoader(jobManagement).runScript(file.text)

        then:
        noExceptionThrown()

        where:
        file << new FileNameFinder().getFileNames('jobs', '**/*.groovy').collect { new File(it) }
    }
}
