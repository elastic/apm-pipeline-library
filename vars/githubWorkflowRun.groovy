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

  Triggers workflow run on Github actions.

  githubWorkflowRun( args map )

  Args:
    repo - repositiry owner and name 
    workflow - workflow file name
    ref - reference ( branch, tag or hash)
    parameters - map with parameters to pass to the workflow 

  Returs:
    runInfo - information about

  Example:
     script {
       Map args = [
           repo: "owner/repository",
           workflow: "build.yml", 
           ref: "main",
           parameters: [
             path: "filebeat"
           ],
           credentialsId: "github-workflow-token",
           ghVersion: "2.1.0"]
       int runId = githubWorkflowRun.triggerGithubActionsWorkflow(args)
       Map runInfo = githubWorkflowRun.getWorkflowRun(args + [runId: runId]) 
   }
*/

import groovy.json.JsonSlurperClassic 

def call(Map args = [:]) {
    try {
        def buildTimeLimit = args.get('buildTimeLimit', 1)
        def runId = triggerGithubActionsWorkflow(args)
        def startDate = new Date()
        while(startDate.time  + buildTimeLimit*3600000 -  new Date().time > 0) {
            def runInfo = getWorkflowRun(args + [runId: runId])
            if (runInfo.status == "completed") return runInfo
            Thread.sleep(300000); // sleep 5 min
        }
        throw new Error("Build time out")
    } catch(error) {
        log(level: 'ERROR', text: "githubWorkflowRun: Failed to run workflow:\n${error.message} ")
    }
    return null
}

def triggerGithubActionsWorkflow(Map args = [:]) {
    if (!args.repo || !args.workflow) {
        throw new IllegalArgumentException("repo: '${args.repo}', workflow: '${args.workflow}'")
    }
    def ghVersion = args.get("ghVersion", "2.1.0")
    def ref = args.get("ref", "master")
    def runner = args.get("runner", "ubuntu-latest")
    def runId = "${ref}-${new Date().getTime()}-${env.BUILD_ID}"
    def parameters = args.get("parameters", [:])
    def inputs = (parameters + [id: runId, runner: runner]).collect{ "${it}" }
    gh(command: "workflow run ${args.workflow}", credentialsId: args.credentialsId,
        version: ghVersion, forceInstallation: true,
        flags: [repo: args.repo, ref: ref, field: inputs ])
    Thread.sleep(30000)
    def runsText = gh(command: "run list", credentialsId: args.credentialsId,
        version: ghVersion, forceInstallation: false,
        flags: [repo: args.repo, workflow: args.workflow, limit: 100])
    def runIds = []
    for(def s: runsText.split("\n")) {
        def r = s.split(/\s+/)
        if (r[-1] == "0m"|| r[-1] == "1m") runIds.add(r[-3]) 
    }
    for(def run: runIds) {
        def runText=gh(command: "run view -v ${run}", credentialsId: args.credentialsId,
            version: ghVersion, forceInstallation: false, flags: [repo: args.repo])
        if (runText.split("\n").find { it ==~ /^\s+\S\s+Run ID ${runId}\s*$/} ) {
            return run as int
        }
    }
    return 0
}

def getWorkflowRun(Map args = [:]) {
    if (!args.repo || !args.runId) {
        throw new IllegalArgumentException("repo: '${args.repo}', runId: '${args.runId}'")
    }
    def ghVersion = args.get("ghVersion", "2.1.0")
    String response = gh(command: "api repos/${args.repo}/actions/runs/${args.runId}",
        version: ghVersion, forceInstallation: false, credentialsId: args.credentialsId, flags: [])
    return new groovy.json.JsonSlurperClassic().parseText(response)
}
