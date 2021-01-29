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

import org.jenkinsci.plugins.workflow.graph.StepNode
import org.jenkinsci.plugins.workflow.steps.StepDescriptor

/**
  Wrap the GitHub notify check step

  withGithubCheck(context: 'checkName', description: 'Execute something') {
    // block
  }

  withGithubCheck(context: 'checkName', description: 'Execute something', isBlueOcean: false) {
    // block
  }

*/
def call(Map args = [:], Closure body) {
  def context = args.containsKey('context') ? args.context : error('withGithubCheck: missing context argument')
  def description = args.get('description', context)
  def secret = args.get('secret', 'secret/observability-team/ci/github-app')
  def org = args.get('org', env.ORG_NAME)
  def repository = args.get('repository', env.REPO_NAME)
  def commitId = args.get('commitId', env.GIT_BASE_COMMIT)
  def redirect = detailsUrl(args.get('tab', 'pipeline'), args.get('isBlueOcean', false))

  log(level: 'INFO', text: "redirect: ${redirect}")


  def parameters = [
    name: context,
    commitId: commitId,
    description: description,
    detailsUrl: redirect,
    org: org,
    repository: repository,
    secret: secret
  ]
  try {
    githubCheck(parameters + [status: 'neutral'])
    withAPM(){
      body()
    }
    githubCheck(parameters + [status: 'success'])
  } catch (err) {
    githubCheck(parameters + [status: 'failure'])
    throw err
  }
}

String detailsUrl(redirect, isBo=false) {
  // Use the https
  if (redirect.startsWith('http')) {
    return redirect
  }

  // If pipeline then let's point to the BLueOcean Stage URL
  if (redirect.equals('pipeline')) {
    return getStageLogUrl()
  }

  // Get the URL for the given tab.
  if (isBo) {
    return getBlueoceanTabURL(redirect)
  }
  return getTraditionalPageURL(redirect)
}

boolean isAvailable(Map args = [:]) {
  return (args.get('org', env.ORG_NAME) && args.get('repository', env.REPO_NAME) && args.get('commitId', env.GIT_BASE_COMMIT))
}

def getStageLogUrl(flowNode = null) {
  if(!flowNode) {
    flowNode = getContext(org.jenkinsci.plugins.workflow.graph.FlowNode)
  }
  log(level: 'INFO', text: "flowNode: ${flowNode?.getFunctionName()}")
  if(isStageNode(flowNode)) {
    return "/${flowNode.url}log/?start=0"
  }

  return flowNode?.parents?.findResult { getStageLogUrl(it) }
}

private boolean isStageNode(node = null) {
  if (node instanceof StepNode) {
    StepDescriptor d = ((StepNode) node).getDescriptor()
    return d != null && d.getFunctionName().equals("stage")
  } else {
    return false
  }
}
