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

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertArrayEquals
import static org.junit.Assert.assertThrows

class GithubWorkflowRunTests extends ApmBasePipelineTest {

  def gh_run_view_output = """
✓ main build · 1441334029
Triggered via workflow_dispatch about 7 days ago

JOBS
✓ build in 15s (ID 4157618310)
  ✓ Set up job
  ✓ Run ID main-1636490856318-122
  ✓ Set environment for macos
  ✓ Set up Go
  ✓ Run actions/checkout@v2
  ✓ Checkout and merge PR
  ✓ Run command
  ✓ Post Run actions/checkout@v2
  ✓ Complete job

For more information about a job, try: gh run view --job=<job-id>
View this run on GitHub: https://github.com/owner/repo/actions/runs/1441334029"""

  def gh_run_list_output = """completed        Added parameters input to the workflow  build_and_test  main    workflow_dispatch  1412226110  8s       0m
completed       Added parameters input to the workflow  build_and_test  main    workflow_dispatch  1412111022  22s      33m
completed       Added parameters input to the workflow  build_and_test  main    workflow_dispatch  1411943898  17s      1h
completed       Added parameters input to the workflow  build_and_test  main    workflow_dispatch  1411689241  16s      2h"""

  def gh_run_list_output_expected_result =  ["1412226110", "1412111022", "1411943898", "1411689241"]


  def ghClosure = { args -> 
      if (args.command == "run list") {
        return """completed       success test 1006       build   main    workflow_dispatch       1441334029      31s     7d
completed       success test 1006       build   main    workflow_dispatch       1441316856      18s     7d
completed       success test 1006       build   main    workflow_dispatch       1441316123      18s     7d"""

      }
      if (args.command == "run view -v 1441334029") {
        return """
✓ main build · 1441334029
Triggered via workflow_dispatch about 7 days ago

JOBS
✓ build in 15s (ID 4157618310)
  ✓ Set up job
  ✓ Run ID main-1636490856318-122
  ✓ Set environment for macos
  ✓ Set up Go
  ✓ Run actions/checkout@v2
  ✓ Checkout and merge PR
  ✓ Run command
  ✓ Post Run actions/checkout@v2
  ✓ Complete job

For more information about a job, try: gh run view --job=<job-id>
View this run on GitHub: https://github.com/owner/repo/actions/runs/1441334029
"""
      }
      if (args.command == "run view -v 1441316856") {
        return """
✓ main build · 1441316856
Triggered via workflow_dispatch about 7 days ago

JOBS
✓ build in 8s (ID 4157568294)
  ✓ Set up job
  ✓ Run ID main-1636490543641-121
  - Set environment for macos
  ✓ Set up Go
  ✓ Run actions/checkout@v2
  ✓ Checkout and merge PR
  ✓ Run command
  ✓ Post Run actions/checkout@v2
  ✓ Complete job

For more information about a job, try: gh run view --job=<job-id>
View this run on GitHub: https://github.com/owner/repo/actions/runs/1441316856
"""
      }

      if (args.command == "run view -v 1441316123") {
        return """
✓ main build · 1441316123
Triggered via workflow_dispatch about 7 days ago

JOBS
✓ build in 8s (ID 4157568123)
  ✓ Set up job
  ✓ Run ID main-1636490543641-120
  - Set environment for macos
  ✓ Set up Go
  ✓ Run actions/checkout@v2
  ✓ Checkout and merge PR
  ✓ Run command
  ✓ Post Run actions/checkout@v2
  ✓ Complete job

For more information about a job, try: gh run view --job=<job-id>
View this run on GitHub: https://github.com/owner/repo/actions/runs/1441316856
"""
      }

      if (args.command == "api repos/owner/repo/actions/runs/1441316856") {
        return '{"id": 1441316856, "status": "completed"}'
      }
      if (args.command == "api repos/owner/repo/actions/runs/1441334029") {
        return '{"message": "Not Found"}'
      }
      if (args.command == "api repos/owner/repo/actions/runs/1441316123") {
        return '{"id": 1441316123, "status": "queued"}'
      }
      return ""
    }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubWorkflowRun.groovy')
    helper.registerAllowedMethod('gh', [Map.class], ghClosure)
  }

  @Test
  void test_call_with_missing_args_call() throws Exception {
    testMissingArgument('workflow') {
      script.call()
    }
  }

  @Test
  void test_call_with_missing_args_triggerGithubActionsWorkflow() throws Exception {
    testMissingArgument('workflow') {
      script.triggerGithubActionsWorkflow()
    }
  }

  @Test
  void test_call_with_missing_args_lookupForRunId() throws Exception {
    testMissingArgument('workflow') {
      script.lookupForRunId()
    }
    testMissingArgument('lookupId') {
      script.lookupForRunId(workflow: 'workflow')
    }
  }

  @Test
  void test_call_with_missing_args_ghDefaultArgs() throws Exception {
    def result = script.ghDefaultArgs()
    assertTrue(result.version == "2.1.0")
    assertTrue(result.forceInstallation == false)
  }

  @Test
  void test_call_with_args_ghDefaultArgs() throws Exception {
    def result = script.ghDefaultArgs(ghVersion: "2.2.0")
    assertTrue(result.version == "2.2.0")
    assertTrue(result.forceInstallation == false)
  }

  @Test
  void test_getRunIdsFromGhOutput() throws Exception {
    def result = script.getRunIdsFromGhOutput(gh_run_list_output).sort()
    assertArrayEquals(gh_run_list_output_expected_result.sort().toArray(), result.sort().toArray())
  }

  @Test
  void test_correct_lookupId_checkTextForLookupId() throws Exception {
    def result = script.checkTextForLookupId(gh_run_view_output, "main-1636490856318-122")
    assertTrue(result)
  }

  @Test
  void test_incorrect_lookupId_checkTextForLookupId() throws Exception {
    def result = script.checkTextForLookupId(gh_run_view_output, "4157618310")
    assertFalse(result)
    result = script.checkTextForLookupId(gh_run_view_output, "")
    assertFalse(result)
    result = script.checkTextForLookupId(gh_run_view_output, null)
    assertFalse(result)
  }

  @Test
  void test_lookupForRunId() throws Exception {
    def result = script.lookupForRunId(workflow: "build", lookupId: "main-1636490543641-121")
    def expected = 1441316856
    assertTrue(expected == result)
    result = script.lookupForRunId(workflow: "build", lookupId: "missing-id")
    expected = 0
    assertTrue(expected == result)
  }

  @Test
  void test_triggerGithubActionsWorkflow() throws Exception {
    def result = script.triggerGithubActionsWorkflow(workflow: "build", lookupId: "main-1636490543641-121")
    def expected = 1441316856
    assertTrue(expected == result)
  }

  @Test
  void test_failed_triggerGithubActionsWorkflow() throws Exception {
     Exception exception = assertThrows(java.lang.Exception, {
        script.triggerGithubActionsWorkflow(workflow: "build", lookupId: "missing-id")
    })
    String expectedMessage = "Triggered workflow with id 'missing-id' but failed to get runId for it"
    String actualMessage = exception.getMessage()
    assertTrue(actualMessage.contains(expectedMessage))
  }

  @Test
  void test_call() throws Exception {
    def result = script.call(workflow: "build", lookupId: "main-1636490543641-121", repo: "owner/repo") 
    def expected = [id: 1441316856, status: "completed"]
    assertTrue(expected.id == result.id && expected.status == result.status)
  }

  @Test
  void test_not_found_error_call() throws Exception {
    Exception exception = assertThrows(java.lang.Exception, {
      script.call(workflow: "build", lookupId: "main-1636490856318-122", repo: "owner/repo") 
    })
    String expectedMessage = "Triggered workflow run but failed to get run info"
    String actualMessage = exception.getMessage()
    assertTrue(actualMessage.contains(expectedMessage))
  }

  @Test
  void test_timeout_error_call() throws Exception {
     Exception exception = assertThrows(java.lang.Exception, {
       script.call(workflow: "build", lookupId: "main-1636490543641-120", repo: "owner/repo", buildTimeLimit: 0.1) 
     })
    String expectedMessage = "Build time out"
    String actualMessage = exception.getMessage()
    assertTrue(actualMessage.contains(expectedMessage))
  }

  @Test
  void test_getWorkflowRun() throws Exception {
    def result = script.getWorkflowRun(runId: 1441316856, repo: "owner/repo") 
    def expected = [id: 1441316856, status: "completed"]
    assertTrue(expected.id == result.id && expected.status == result.status)
  }
}
