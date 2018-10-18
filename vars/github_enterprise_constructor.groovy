/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call(){
  if(!env.GIT_URL){
    return
  }
  
  def tmpUrl = env.GIT_URL
  
  if (env.GIT_URL.startsWith("git")){
    tmpUrl = tmpUrl - "git@github.com:"
  } else {
    tmpUrl = tmpUrl - "https://github.com/" - "http://github.com/"
  }
  
  def parts = tmpUrl.split("/")
  env.ORG_NAME = parts[0]
  env.REPO_NAME = parts[1] - ".git"
  env.GIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

  if (env.CHANGE_TARGET){
    env.GIT_BUILD_CAUSE = "pr"
  } else {
    env.GIT_BUILD_CAUSE = sh (
      script: 'git rev-list HEAD --parents -1 | wc -w', // will have 2 shas if commit, 3 or more if merge
      returnStdout: true
    ).trim().toInteger() > 2 ? "merge" : "commit"
  }

  println "Found Git Build Cause: ${env.GIT_BUILD_CAUSE}"
}