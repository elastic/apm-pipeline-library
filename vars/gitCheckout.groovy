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
      sh "export"
    }
  }
}

def checkApproved(){
  if(env?.CHANGE_ID == null){
    return true
  }
  def approved = false
  def token = getGithubToken()
  def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
  def pr = getPrInfo(token, repoName, env.CHANGE_ID)
  def reviews = getPrReviews(token, repoName, env.CHANGE_ID)
  
  log(level: 'INFO', text: "Title: ${pr?.title} - User: ${pr?.user.login} - Author Association: ${pr?.author_association}")
  
  if(reviews?.size() == 0){
    log(level: 'INFO', text: "There are no reviews yet")
    approved = false
  }
  
  reviews.each{ r ->
    if(r?.state == 'APPROVED' && r?.author_association == "MEMBER"){
      log(level: 'INFO', text: "User: ${r?.user.login} - Author Association: ${r?.author_association} : ${r['state']}")
      approved = true
    }
  }
  
  if(pr?.author_association == 'MEMBER'){
    log(level: 'INFO', text: "The user is MEMBER")
    approved = true
  }
  
  if(!approved){
    error("The PR is not approced yet")
  }
}

def getPrInfo(token, repoName, prID){
  return makeGithubApiCall(token, "https://api.github.com/repos/${repoName}/pulls/${prID}")
}

def getPrReviews(token, repoName, prID){
  return makeGithubApiCall(token, "https://api.github.com/repos/${repoName}/pulls/${prID}/reviews")
}

def makeGithubApiCall(token, url){
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [
    [var: 'GITHUB_TOKEN', password: "${token}"], 
    ]]) {
    def json = sh(
      script: """#!/bin/bash
      set +x
      curl -s -H 'Authorization: token ${token}' '${url}'
      """,
      returnStdout: true
    )
    return readJSON(text: json)
  }
}
