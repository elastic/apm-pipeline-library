#!/usr/bin/env groovy

/**
  Perform a checkout from the SCM configuration on a folder inside the workspace,
  if branch, repo, and credentialsId are defined make a checkout using those parameters.
  
  gitCheckout()
  
  gitCheckout(basedir: 'sub-folder')
  
  gitCheckout(basedir: 'sub-folder', branch: 'master', 
    repo: 'git@github.com:elastic/apm-pipeline-library.git', 
    credentialsId: 'credentials-id')

*/
def call(Map params = [:]){
  def basedir =  params.containsKey('basedir') ? params.basedir : "src"
  def repo =  params?.repo
  def credentialsId =  params?.credentialsId
  def branch =  params?.branch
  
  withEnvWrapper() {
    dir("${basedir}"){
      if(env?.BRANCH_NAME){
        echo "Checkout SCM ${env.BRANCH_NAME}"
        checkout scm
      } else if (branch && branch != ""
          && repo
          && credentialsId){
        echo "Checkout ${branch}"
        checkout([$class: 'GitSCM', branches: [[name: "${branch}"]], 
          doGenerateSubmoduleConfigurations: false, 
          extensions: [], 
          submoduleCfg: [], 
          userRemoteConfigs: [[credentialsId: "${credentialsId}", 
          url: "${repo}"]]])
      } else {
        error "No valid SCM config passed."
      }
      github_enterprise_constructor()
    }
  }
}
