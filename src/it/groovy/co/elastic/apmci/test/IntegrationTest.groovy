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

package co.elastic.apmci.test

import jenkins.branch.BranchSource
import jenkins.scm.impl.mock.MockSCMController
import jenkins.scm.impl.mock.MockSCMDiscoverBranches
import jenkins.scm.impl.mock.MockSCMSource
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.junit.Test
import org.jvnet.hudson.test.recipes.LocalData

import static org.hamcrest.collection.IsEmptyCollection.empty
import static org.hamcrest.core.IsNot.not
import static org.junit.Assert.assertThat

class IntegrationTest extends BaseIntegrationTestCase {

  @Test
  void testLog() throws Exception {
    j.withExistingHome(new File(getClass().getResource("/co/elastic/apmci/test/IntegrationTest/apm-ci").toURI()))
    WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, 'echo');
    job.setDefinition(new CpsFlowDefinition(fileContentsFromResources('it/log.groovy')))
    j.buildAndAssertSuccess(job)
    WorkflowRun build = job.getLastBuild()
    j.assertBuildStatusSuccess(build)
    j.assertLogContains("info message", build)
    j.assertLogContains("debug message", build)
  }

  @Test
  void testEchoMultiBranch() throws Exception {
    MockSCMController controller = MockSCMController.create()
    controller.createRepository("repoFoo")
    controller.createBranch("repoFoo", "master")
    controller.addFile("repoFoo", "master", "Jenkinsfile", "Jenkinsfile", fileContentsFromResources('it/echo.groovy').getBytes())

    WorkflowMultiBranchProject project = j.createProject(WorkflowMultiBranchProject.class)
    project.getSourcesList().add(new BranchSource(new MockSCMSource(controller, "repoFoo",  new MockSCMDiscoverBranches())))

    project.scheduleBuild2(0)
    j.waitUntilNoActivity()
    assertThat(project.getItems(), not(empty()))
  }

}
