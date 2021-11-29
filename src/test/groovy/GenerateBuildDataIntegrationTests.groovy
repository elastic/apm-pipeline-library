// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import com.github.tomakehurst.wiremock.junit.WireMockRule
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import net.sf.json.JSONSerializer
import org.junit.Rule
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
  If you need to create new data, then you need to use the Wiremock in conjunction with
  the local jenkins instance to record the BlueOcean responses.

  For such:
  - cd local ** make start
  - wait for Jenkins to start
  - cd local ** make start-local-worker
  - cd local ** make wiremock-build-jobs
    1. will build the jobs in http://localhost:18080/job/it/job/getBuildInfoJsonFiles/ folder
  - wait for Jenkins builds to finish
  - cd local ** make wiremock-start
    1. will download https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.26.3/wiremock-standalone-2.26.3.jar
    2. will run java -jar wiremock-standalone-*.jar --proxy-all="http://localhost:18080" --record-mappings --verbose
  - cd local ** make wiremock-bo
    1. will hit on the BlueOcean URLs for the builds of each of the jobs in the above folder, replacing the 18080 port with 8080, therefore the wiremock proxy will record the calls.
    2. will copy the folders mappings and __files to src/test/resources
*/
class GenerateBuildDataIntegrationTests {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(18081)

  private final String BO_URL = "http://localhost:18081/blue/rest/organizations/jenkins/pipelines/it/getBuildInfoJsonFiles"
  private final String TRADITIONAL_URL = "http://localhost:18081/job/it/job/getBuildInfoJsonFiles/job"

  @Test
  public void abortBuild_without_tests() {
    String targetFolder = "abortBuild_without_tests"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "abort", "ABORTED")
    printStdout(process)
    assertEquals("Process did finish unsuccessfully", 1, process.waitFor())

    // Tests were not executed
    JSONObject obj = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-summary.json").text)
    assertTrue(obj.get("total") == 0)

    obj = JSONSerializer.toJSON(new File("target/${targetFolder}/build-report.json").text)
    assertFalse(obj.isEmpty())
    assertFalse(obj.get("job").isEmpty())
    assertTrue(obj.get("test_coverage").isEmpty())
    assertFalse(obj.get("test_summary").isEmpty())
    assertFalse(obj.get("changeSet").isEmpty())
    assertFalse(obj.get("artifacts").isEmpty())
    assertTrue(obj.get("test").isEmpty())
    assertFalse(obj.get("build").isEmpty())
  }

  @Test
  public void successBuild_without_tests() {
    String targetFolder = "successBuild_without_tests"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "success", "ABORTED")
    printStdout(process)
    assertEquals("Process did finish unsuccessfully", 1, process.waitFor())

    // Tests were not executed
    JSONObject obj = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-summary.json").text)
    assertTrue(obj.get("total") == 0)

    obj = JSONSerializer.toJSON(new File("target/${targetFolder}/build-report.json").text)
    assertFalse(obj.isEmpty())
    assertFalse(obj.get("job").isEmpty())
    assertTrue(obj.get("test_coverage").isEmpty())
    assertFalse(obj.get("test_summary").isEmpty())
    assertFalse(obj.get("changeSet").isEmpty())
    assertFalse(obj.get("artifacts").isEmpty())
    assertTrue(obj.get("test").isEmpty())
    assertFalse(obj.get("build").isEmpty())
    assertFalse(obj.get("env").isEmpty())
  }

  @Test
  public void cobertura_tests() {
    String targetFolder = "cobertura_tests"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "cobertura", "SUCCESS")
    printStdout(process)
    assertEquals("Process did finish unsuccessfully, there are no executed tests.", 1, process.waitFor())

    // Tests were not executed
    JSONObject obj = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-summary.json").text)
    assertTrue(obj.get("total") == 0)

    obj = JSONSerializer.toJSON(new File("target/${targetFolder}/build-report.json").text)
    assertFalse(obj.isEmpty())
    assertFalse(obj.get("job").isEmpty())
    assertFalse(obj.get("test_coverage").isEmpty())
    assertFalse(obj.get("test_summary").isEmpty())
    assertTrue(obj.get("changeSet").isEmpty())
    assertFalse(obj.get("artifacts").isEmpty())
    assertTrue(obj.get("test").isEmpty())
    assertFalse(obj.get("build").isEmpty())
    assertFalse(obj.get("env").isEmpty())
  }

  @Test
  public void unstableBuild() {
    String targetFolder = "unstableBuild"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "unstable", "UNSTABLE")
    printStdout(process)
    assertEquals("Process did finish successfully", 0, process.waitFor())

    // Tests were executed
    JSONObject obj = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-summary.json").text)
    assertTrue(obj.get("total") > 0)

    obj = JSONSerializer.toJSON(new File("target/${targetFolder}/build-report.json").text)
    assertFalse(obj.isEmpty())
    assertFalse(obj.get("job").isEmpty())
    assertTrue(obj.get("test_coverage").isEmpty())
    assertFalse(obj.get("test_summary").isEmpty())
    assertFalse(obj.get("changeSet").isEmpty())
    assertFalse(obj.get("artifacts").isEmpty())
    assertFalse(obj.get("test").isEmpty())
    assertFalse(obj.get("build").isEmpty())
    assertNotNull(obj.get("build").causes.shortDescription)
    assertFalse(obj.get("env").isEmpty())

    // Then metadata is removed
    assertNull(obj.get("build").actions)
    assertNull(obj.get("changeSet")[0].author?._class)
    assertNull(obj.get("changeSet")[0].author?._links)

    // Then some duplicated entries don't exist anymore
    assertNull(obj.get("build.branch"))
    assertNull(obj.get("build.changeSet"))
    assertNull(obj.get("build.pullRequest"))

    // Then a flatten test in the bulk file
    new File("target/${targetFolder}/ci-test-report-bulk.json").eachLine { line ->
      obj = JSONSerializer.toJSON(line)
      assertNotNull("There are some entries in the bulk file.", obj)
      if (obj?.test?.age) {
        assertEquals("Only one test entry that matches 1 age.", 1, obj.test.age)
      }
    }

    // Then a build report without test
    obj = JSONSerializer.toJSON(new File("target/${targetFolder}/ci-build-report.json").text)
    assertFalse(obj.isEmpty())
    assertFalse(obj.get("job").isEmpty())
    assertFalse(obj.get("test_summary").isEmpty())
    assertNull(obj.get("test"))
  }

  @Test
  public void errorBuild() {
    String targetFolder = "errorBuild"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "error", "UNSTABLE")
    printStdout(process)
    assertEquals("Process did finish unsuccessfully", 1, process.waitFor())

    // Tests were not executed
    JSONObject obj = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-summary.json").text)
    assertTrue(obj.get("total") == 0)

    JSONArray errors = JSONSerializer.toJSON(new File("target/${targetFolder}/steps-errors.json").text)
    assertFalse("There are steps errors", errors.isEmpty())
    obj = errors.get(0)
    assertEquals("Log transformation happens successfully", "foo", obj.get("displayDescription"))
    assertEquals("It was an error signal", "Error signal", obj.get("displayName"))
  }

  @Test
  public void unstableBuild_with_tests_normalisation() {
    String targetFolder = "unstableBuild_with_tests_normalisation"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "unstable", "UNSTABLE")
    printStdout(process)
    assertEquals("Process did finish successfully", 0, process.waitFor())

    // Tests were executed
    JSONArray tests = JSONSerializer.toJSON(new File("target/${targetFolder}/tests-errors.json").text)
    assertFalse("There are tests", tests.isEmpty())

    JSONObject obj = tests.get(0)
    println obj
    assertNull("No _links object", obj.get("_links"))
    assertNull("No _class object", obj.get("_class"))
    assertNull("No state object", obj.get("state"))
    assertNull("No hasStdLog object", obj.get("hasStdLog"))
    assertNotNull("There is an errorStackTrace object", obj.get("errorStackTrace"))
  }

  @Test
  public void errorBuild_with_steps_normalisation() {
    String targetFolder = "errorBuild_with_steps_normalisation"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "error", "UNSTABLE")
    printStdout(process)
    assertEquals("Process did finish unsuccessfully", 1, process.waitFor())

    JSONArray errors = JSONSerializer.toJSON(new File("target/${targetFolder}/steps-errors.json").text)
    assertFalse("There are steps errors", errors.isEmpty())
    JSONObject obj = errors.get(0)
    assertNull("No _class object", obj.get("_class"))
    assertNull("No _index object", obj.get("_index"))
    assertNull("No actions object", obj.get("actions"))
    assertTrue("BO_URL transformation happens successfully", obj.get("url").matches("http.*/blue/rest/organizations/jenkins/pipelines/it/pipelines/getBuildInfoJsonFiles/pipelines/error/runs/1/steps/7/log"));
  }

  @Test
  public void emptyBuild_with_default_manipulation() {
    String targetFolder = "emptyBuild_with_default_manipulation"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "empty", "SUCCESS")
    printStdout(process)
    assertEquals("Process did finish successfully", 0, process.waitFor())

    def content = new File("target/${targetFolder}/job-info.json").text
    assertFalse(content.isEmpty())
    JSONObject info = JSONSerializer.toJSON(content)
    assertTrue(info.isEmpty())
  }

  @Test
  public void multiTestFailuresBuild() {
    String targetFolder = "multiTestFailures"
    Process process = runCommand(targetFolder, TRADITIONAL_URL, BO_URL, "multiTestFailures", "UNSTABLE")
    printStdout(process)
    assertEquals("Process did finish successfully", 0, process.waitFor())

    // Then a flatten test in the bulk file
    new File("target/${targetFolder}/ci-test-report-bulk.json").eachLine { line ->
      def obj = JSONSerializer.toJSON(line)
      assertNotNull("There are some entries in the bulk file.", obj)
      if (obj?.test?.age) {
        assertEquals("Only one test entry that matches 1 age.", 1, obj.test.age)
      }
    }
  }

  Process runCommand(String targetFolder, String tradditionalUrl, String boUrl, String jobName, String status) {
    //Build command
    List<String> commands = new ArrayList<String>()
    commands.add("../../resources/scripts/generate-build-data.sh")
    commands.add(boUrl + "/${jobName}/")
    commands.add(boUrl + "/${jobName}/runs/1")
    commands.add(status)
    commands.add('1')

    ProcessBuilder pb = new ProcessBuilder(commands)
    Map<String, String> env = pb.environment()
    env.put('JENKINS_URL', 'http://localhost:18081/')
    env.put('PIPELINE_LOG_LEVEL', 'INFO')
    env.put('BRANCH_NAME', 'main')
    env.put('BUILD_DISPLAY_NAME', '#1')
    env.put('BUILD_ID', '1')
    env.put('BUILD_NUMBER', '1')
    env.put('BUILD_TAG', 'jenkins-project-main-1')
    env.put('BUILD_URL', tradditionalUrl + "/${jobName}/1")
    env.put('BO_JOB_URL', BO_URL + "/${jobName}")
    env.put('BO_BUILD_URL', BO_URL + "/${jobName}/1")
    env.put('CHANGE_AUTHOR', 'v1v')
    env.put('CHANGE_BRANCH', 'main')
    env.put('CHANGE_FORK', 'v1v/project')
    env.put('CHANGE_ID', '1')
    env.put('CHANGE_TARGET', 'my-target')
    env.put('CHANGE_URL', 'https://github.com/foo/project/issues/1')
    env.put('GIT_COMMIT', '4f0aea0e892678e46d62fd0a156f9c9c4b670995')
    env.put('GIT_PREVIOUS_COMMIT', '4f0aea0e892678e46d62fd0a156f9c9c4b670995')
    env.put('GIT_PREVIOUS_SUCCESSFUL_COMMIT', '4f0aea0e892678e46d62fd0a156f9c9c4b670995')
    env.put('JOB_BASE_NAME', jobName)
    env.put('JOB_DISPLAY_URL', tradditionalUrl + "/${jobName}/display/redirect")
    env.put('JOB_NAME', "project/${jobName}")
    env.put('JOB_URL', tradditionalUrl + "/${jobName}")
    env.put('ORG_NAME', 'acme')
    env.put('OTEL_ELASTIC_URL', 'https://kibana.elastic.dev/app/apm/services/jenkins/transactions/view?rangeFrom=2021-03-06T21:41:11.403Z&rangeTo=2021-03-06T22:01:11.403Z&transactionName=load-testing/cron_gce&transactionType=unknown&latencyAggregationType=avg&traceId=38f56b69e3b7c76c8e914b2d467d0475&transactionId=b877b754e2e3a8c4')
    env.put('REPO_NAME', 'project')
    File location = new File("target/${targetFolder}")
    location.mkdirs()
    pb.directory(location)
    pb.redirectErrorStream(true)
    Process process = pb.start()

    return process
  }

  private printStdout(Process process) {
    InputStream stdout = process.getInputStream()
    BufferedReader reader = new BufferedReader (new InputStreamReader(stdout))
    def line = ''
    println("Stdout:")
    while ((line = reader.readLine ()) != null) {
      println(line)
    }
  }
}
