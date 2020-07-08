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
  Perform a checkout from the SCM configuration on a folder inside the workspace,
  if branch, repo, and credentialsId are defined make a checkout using those parameters.

  gitCheckout()

  gitCheckout(basedir: 'sub-folder')

  gitCheckout(basedir: 'sub-folder', branch: 'master',
    repo: 'git@github.com:elastic/apm-pipeline-library.git',
    credentialsId: 'credentials-id',
    reference: '/var/lib/jenkins/reference-repo.git')

*/
def call(Map params = [:]){
  def basedir =  params.containsKey('basedir') ? params.basedir : "src"
  def repo =  params?.repo
  def credentialsId =  params?.credentialsId
  def branch =  params?.branch
  def reference = params?.reference
  def mergeRemote = params.containsKey('mergeRemote') ? params.mergeRemote : "origin"
  def mergeTarget = params?.mergeTarget
  def notify = params.containsKey('githubNotifyFirstTimeContributor') ? params.get('githubNotifyFirstTimeContributor') : false
  def shallowValue = params.containsKey('shallow') ? params.get('shallow') : false
  def depthValue = params.containsKey('depth') ? params.get('depth') : 5
  def retryValue = params.containsKey('retry') ? params.get('retry') : 3
  def refspec = '+refs/pull/*/head:refs/remotes/origin/pr/*'

  if(!env?.GIT_URL && params.repo) {
    log(level: 'DEBUG', text: 'Override GIT_URL with the params.repo to support simple pipeline rather than multibranch pipelines only.')
    env.GIT_URL = params.repo
  }

  // isCustomised
  def customised = params.containsKey('mergeRemote') || params.containsKey('shallow') || params.containsKey('depth') ||
                   params.containsKey('reference') || params.containsKey('mergeTarget') || params.containsKey('credentialsId') ||
                   params.containsKey('repo') || params.containsKey('branch')

  def githubCheckContext = 'CI-approved contributor'
  def extensions = []

  if (shallowValue && mergeTarget != null) {
    // https://issues.jenkins-ci.org/browse/JENKINS-45771
    log(level: 'INFO', text: "'shallow' is forced to be disabled when using mergeTarget to avoid refusing to merge unrelated histories")
    shallowValue = false
  }

  // Shallow cloning in PRs might cause some issues when running on Multibranch Pipelines, therefore
  // the shallow cloning has been forced to be disabled on PRs.
  // NOTE: This could be skipped with something like the below commit, but it's too risky:
  //  https://github.com/elastic/apm-pipeline-library/commit/e2a2832569879f9a03d50c59038602075a47e929
  if(isPR()) {
    log(level: 'INFO', text: "'shallow' is forced to be disabled when running on PullRequests")
    shallowValue = false
  }

  extensions.add([$class: 'CloneOption', depth: shallowValue ? depthValue : 0, noTags: false, reference: "${reference != null ? reference : '' }", shallow: shallowValue])
  log(level: 'DEBUG', text: "gitCheckout: Reference repo ${reference != null ? 'enabled' : 'disabled' } ${extensions.toString()}")

  if(mergeTarget != null){
    extensions.add([$class: 'PreBuildMerge', options: [mergeTarget: "${mergeTarget}", mergeRemote: "${mergeRemote}"]])
    log(level: 'DEBUG', text: "gitCheckout: Reference repo enabled ${extensions.toString()}")
  }

  dir("${basedir}"){
    if(customised && isDefaultSCM(branch)){
      log(level: 'INFO', text: "gitCheckout: Checkout SCM ${env.BRANCH_NAME} with some customisation.")
      checkout([$class: 'GitSCM', branches: scm.branches,
        doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
        extensions: extensions,
        submoduleCfg: scm.submoduleCfg,
        userRemoteConfigs: scm.userRemoteConfigs])
    } else if(isDefaultSCM(branch)){
      log(level: 'INFO', text: "gitCheckout: Checkout SCM ${env.BRANCH_NAME} with default customisation from the Item.")
      checkout scm
    } else if (branch && branch != '' && repo && credentialsId){
      log(level: 'INFO', text: "gitCheckout: Checkout ${branch} from ${repo} with credentials ${credentialsId}")
      checkout([$class: 'GitSCM', branches: [[name: "${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: extensions,
        submoduleCfg: [],
        userRemoteConfigs: [[
          refspec: '+refs/heads/*:refs/remotes/origin/* +refs/pull/*/head:refs/remotes/origin/PR/*',
          credentialsId: "${credentialsId}",
          url: "${repo}"]]])
    } else {
      def message = 'No valid SCM config passed. '
      if(env.BRANCH_NAME && branch) {
        message += 'Please use the checkout either with the env.BRANCH_NAME or the gitCheckout(branch: , repo: , credentialsId: ...) format.'
      } else if (repo || credentialsId || branch) {
        message += "Please double check the parameters branch=${branch}, repo=${repo} or credentialsId=${credentialsId} are passed."
      } else {
        message += "Please double check the environment variable env.BRANCH_NAME=${env.BRANCH_NAME} is correct."
      }
      error "${message}"
    }

    // Fetch all the references. It requires the ORG_NAME/REPO_NAME which it's set with the githubEnv.setGitRepoEnvironment step.
    githubEnv.setGitRepoEnvironment()
    fetchPullRefs(refspec)

    // Set all the environment variables that other steps can consume later on.
    githubEnv()

    // Let's see the reason for this particular build, there are 3 different reasons:
    // - An user with run permissions did trigger the build manually.
    // - A GitHub comment
    // - Another pipeline/job did trigger this build but with certain exclusions.
    if(isUserTrigger() || isCommentTrigger() || isUpstreamTriggerWithExclusions()){
      // Ensure the GH check gets reset as there is a cornercase where a specific commit got relaunched and this check failed.
      if (notify) {
        githubNotify(context: githubCheckContext, status: 'SUCCESS', targetUrl: ' ')
      }
    } else {
      log(level: 'DEBUG', text: 'Neither a user trigger nor a comment trigger, it is required to evaluate the PR ownership')
      try {
        githubPrCheckApproved()
        if (notify) {
          githubNotify(context: githubCheckContext, status: 'SUCCESS', targetUrl: ' ')
        }
      } catch(err) {
        if (notify) {
          githubNotify(context: githubCheckContext, description: 'It requires manual inspection', status: 'FAILURE', targetUrl: ' ')
        }
        throw err
      }
    }
  }
}

/**
  If the same project triggered this build (likely related to a timeout issue) then let's verify githubPrCheckApproved,
  otherwise it's a valid use case.

  NOTE: if the upstream was caused by one of the whitelisted triggers then it won't be populated. In other words,
  the rebuild will fail if githubPrCheckApproved, there is no an easy way to do something different.
*/
def isUpstreamTriggerWithExclusions() {
  def buildCause = currentBuild.getBuildCauses()?.find{ it._class == 'hudson.model.Cause$UpstreamCause'}
  if (buildCause?.upstreamProject?.equals(currentBuild.fullProjectName)) {
    return isUpstreamTrigger() && githubPrCheckApproved()
  }
  return isUpstreamTrigger()
}

def isDefaultSCM(branch) {
  return env?.BRANCH_NAME && branch == null
}

def fetchPullRefs(refs){
  gitCmd(cmd: 'fetch', args: refs, store: true)
}
