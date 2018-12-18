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
        echo "Checkout ${branch} from ${repo} with credentials ${credentialsId}"
        checkout([$class: 'GitSCM', branches: [[name: "${branch}"]], 
          doGenerateSubmoduleConfigurations: false, 
          extensions: [], 
          submoduleCfg: [], 
          userRemoteConfigs: [[credentialsId: "${credentialsId}", 
          url: "${repo}"]]])
      } else {
        error "No valid SCM config passed."
      }
      githubEnv()
      checkApproved()
    }
  }
}

def checkApproved(){
  if(env?.CHANGE_ID == null){
    return true
  }
  def token = getGithubToken()
  def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"], 
    ]]) {
    def prJson = sh(
      script: """#!/bin/bash
      set +x
      curl -s -H 'Authorization: token ${token}' 'https://api.github.com/repos/${repoName}/pulls/${env.CHANGE_ID}'
      """,
      returnStdout: true
    )
    def pr = readJSON(text: prJson)
    log(level: 'INFO', text: """
    Title: ${pr?.title}
    User: ${pr?.user.login}
    Author Association: ${pr?.author_association}
    """)
    
    def prReviewsJson = sh(
      script: """#!/bin/bash
      set +x
      curl -s -H 'Authorization: token ${token}' 'https://api.github.com/repos/${repoName}/pulls/${env.CHANGE_ID}'
      """,
      returnStdout: true
    )
    def reviews = readJSON(text: prReviewsJson)
    def approved = false
    reviews.each{ r ->
      if(r?.state == 'APPROVED'){
        log(level: 'INFO', text: "User: ${r?.user.login} - Author Association: ${pr?.author_association} : ${r?.state}")
        approved = true
      }
    }
    
    if(pr?.author_associatio == 'MEMBER'){
      approved = true
    }
    return approved
  }
}
