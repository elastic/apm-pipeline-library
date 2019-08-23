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
  def notify = params?.get('githubNotifyFirstTimeContributor', false)
  def shallowValue = params?.get('shallow', true)
  def depthValue = params?.get('depth', 5)

  def githubCheckContext = 'CI-approved contributor'
  def extensions = []

  if (shallowValue && mergeTarget != null) {
    // https://issues.jenkins-ci.org/browse/JENKINS-45771
    error 'It might cause refusing to merge unrelated histories'
  }

  extensions.add([$class: 'CloneOption', depth: shallowValue ? depthValue : 0, noTags: false, reference: "${reference != null ? reference : '' }", shallow: shallowValue])
  log(level: 'DEBUG', text: "gitCheckout: Reference repo ${reference != null ? 'enabled' : 'disabled' } ${extensions.toString()}")

  if(mergeTarget != null){
    extensions.add([$class: 'PreBuildMerge', options: [mergeTarget: "${mergeTarget}", mergeRemote: "${mergeRemote}"]])
    log(level: 'DEBUG', text: "gitCheckout: Reference repo enabled ${extensions.toString()}")
  }

  dir("${basedir}"){
    if(env?.BRANCH_NAME && branch == null){
      log(level: 'INFO', text: "gitCheckout: Checkout SCM ${env.BRANCH_NAME}")
      checkout scm
    } else if (branch && branch != '' && repo && credentialsId){
      log(level: 'INFO', text: "gitCheckout: Checkout ${branch} from ${repo} with credentials ${credentialsId}")
      checkout([$class: 'GitSCM', branches: [[name: "${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: extensions,
        submoduleCfg: [],
        userRemoteConfigs: [[
          refspec: '+refs/heads/*:refs/remotes/origin/* +refs/pull/*/head:refs/remotes/origin/pr/*',
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
    githubEnv()
    if(isUserTrigger() || isCommentTrigger()){
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
