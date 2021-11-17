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

/**

  This step triggers workflow run on Github actions.

  Example:
     script {
       def args = [
           repo: "owner/repository",
           workflow: "build.yml", 
           ref: "main",
           parameters: [
             path: "filebeat"
           ],
           credentialsId: "github-workflow-token",
           ghVersion: "2.1.0"]
       def runId = githubWorkflowRun.triggerGithubActionsWorkflow(args)
       def runInfo = githubWorkflowRun.getWorkflowRun(args + [runId: runId]) 
   }
*/

import com.cloudbees.groovy.cps.NonCPS

def call(Map args = [:]) {
    // time in minutes to wait before build completed
    def buildTimeLimit = args.get('buildTimeLimit', 30)
    def runId = triggerGithubActionsWorkflow(args)
    def startDate = new Date()
    // the following loop intended to wait till triggered run completed
    // or time out specifed by buildTimeLimit is reached
    while(startDate.time  + buildTimeLimit*60000 -  new Date().time > 0) {
        def runInfo = getWorkflowRun(args + [runId: runId])
        if (runInfo.status == "completed") return runInfo
        sleep(buildTimeLimit > 10 ? 300 : 60)
    }
    error("Build time out")
}

def triggerGithubActionsWorkflow(Map args = [:]) {
    if (!args.workflow) error('triggerGithubActionsWorkflow: workflow parameter is required.')
    def ref = args.get("ref", "master")
    def runner = args.get("runner", "ubuntu-latest")
    def lookupId = args.get("lookupId", "${ref}-${new Date().getTime()}-${env.BUILD_ID}")
    def repo = args.get("repo", "${env.ORG_NAME}/${env.REPO_NAME}")
    def parameters = args.get("parameters", [:])
    def inputs = (parameters + [id: lookupId, runner: runner]).collect{ "${it}" }
    gh(ghDefaultArgs(args) + [command: "workflow run ${args.workflow}", 
        forceInstallation: true, flags: [repo: repo, ref: ref, field: inputs]])
    sleep(30)
    def runId = lookupForRunId(args+[lookupId: lookupId])
    if (runId) return runId
    error("Triggered workflow with id '${lookupId}' but failed to get runId for it")
}

def lookupForRunId(Map args = [:]) {
    if (!args.workflow) error('lookupForRunId: workflow parameter is required.')
    if (!args.lookupId) error('lookupForRunId: lookupId parameter is required.')
    def repo = args.get("repo", "${env.ORG_NAME}/${env.REPO_NAME}")
    def limit = args.get("limit", 100)
    def runsText = gh(ghDefaultArgs(args) + [command: "run list", 
        flags: [repo: repo, workflow: args.workflow, limit: limit]])
    for(def run: getRunIdsFromGhOutput(runsText)) {
        def runText=gh(ghDefaultArgs(args) + [command: "run view -v ${run}",
            flags: [repo: repo]])
        if (checkTextForLookupId(runText, args.lookupId)) {
            return run as int
        }
    }
    return 0
}

@NonCPS
def checkTextForLookupId(def text, def lookupId) {
    return text.split("\n").find { it ==~ /^\s+\S\s+Run ID ${lookupId}\s*$/} != null
}

def getWorkflowRun(Map args = [:]) {
    if (!args.runId) error('getWorkflowRun: runId parameter is required.')
    def repo = args.get("repo", "${env.ORG_NAME}/${env.REPO_NAME}")
    return toJSON(gh(ghDefaultArgs(args) + [forceInstallation: true,
        command: "api repos/${repo}/actions/runs/${args.runId}"]))
}

def ghDefaultArgs(Map args = [:]) {
    def ghArgs = [
        version: args.get("ghVersion", "2.1.0"),
        forceInstallation: false]
    if (args.credentialsId) ghArgs += [credentialsId: args.credentialsId]
    return ghArgs
}

def getRunIdsFromGhOutput(def runsText) {
    def runIds = []
    for(def s: runsText.split("\n")) {
        def r = s.split(/\s+/)
        runIds.add(r[-3]) 
    }
   return runIds
}
