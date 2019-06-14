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

  def extensions = []

  if(reference != null){
    extensions.add([$class: 'CloneOption', depth: 5, noTags: false, reference: "${reference}", shallow: true])
    log(level: 'DEBUG', text: "gitCheckout: Reference repo enabled ${extensions.toString()}")
  }

  if(mergeTarget != null){
    extensions.add([$class: 'PreBuildMerge', options: [mergeTarget: "${mergeTarget}", mergeRemote: "${mergeRemote}"]])
    log(level: 'DEBUG', text: "gitCheckout: Reference repo enabled ${extensions.toString()}")
  }

  dir("${basedir}"){
    if(env?.BRANCH_NAME && branch == null){
      log(level: 'INFO', text: "gitCheckout: Checkout SCM ${env.BRANCH_NAME}")
      checkout scm
    } else if (branch && branch != ""
        && repo
        && credentialsId){
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
      error "No valid SCM config passed."
    }
    githubEnv()
    if(!isUserTrigger() && !isCommentTrigger()){
      githubPrCheckApproved()
    }
  }
}
