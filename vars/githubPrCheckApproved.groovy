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

import com.cloudbees.groovy.cps.NonCPS

/**
  If the current build is a PR, it would check if it is approved or created
  by a user with write/admin permission on the repo.
  If it is not approved, the method will throw an error.

  githubPrCheckApproved()
*/
def call(Map params = [:]){
  if(env?.CHANGE_ID == null){
    return true
  }
  def approved = false
  def token = getGithubToken()
  def repoName = "${env.ORG_NAME}/${env.REPO_NAME}"
  def pr = githubPrInfo(token: token, repo: repoName, pr: env.CHANGE_ID)
  def reviews = githubPrReviews(token: token, repo: repoName, pr: env.CHANGE_ID)
  def user = pr?.user?.login
  def userType = pr?.user?.type

  log(level: 'INFO', text: "githubPrCheckApproved: Title: ${pr?.title} - User: ${user} - Author Association: ${pr?.author_association}")

  approved = user != null && (isPrApproved(reviews) || hasWritePermission(token, repoName, user) || isAuthorizedBot(user, userType))

  if(!approved){
    catchError(buildResult: 'SUCCESS', message: 'githubPrCheckApproved: The PR is not allowed to run in the CI yet') {
      error('githubPrCheckApproved: The PR is not allowed to run in the CI yet. (Only users with write permissions can do so.)')
    }
    abortBuild()
  }
  return approved
}

/**
  Check reviews to find one approved by a MEMBER or COLLABORATOR.
*/
def isPrApproved(reviews){
  def ret = false
  if(reviews?.size() == 0){
    log(level: 'DEBUG', text: "githubPrCheckApproved: There are no reviews yet")
    return false
  }

  reviews.each{ r ->
    if(r?.state == 'APPROVED' && (r?.author_association == "MEMBER" || r?.author_association == "COLLABORATOR")){
      log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${r?.user?.login} - Author Association: ${r.author_association} : ${r.state}")
      ret = true
      return
    }
  }
  return ret
}

/**
  Check user has write or admin permissions.
*/
def hasWritePermission(token, repo, user){
  def json = githubRepoGetUserPermission(token: token, repo: repo, user: user)
  log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${user}, Repo: ${repo}, Permision: ${json?.permission}")
  return json?.permission == 'admin' || json?.permission == 'write'
}

/**
  Check if the PR come from a bot and if the bot is authorized.
*/
def isAuthorizedBot(login, type){
  def authorizedBots = ['greenkeeper[bot]']
  log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${login}, Type: ${type}")
  def ret = false;
  if('bot'.equalsIgnoreCase(type)){
    ret = authorizedBots.any{ it.equals(login) }
  }
  return ret;
}

def abortBuild() {
  b = currentBuild
  rawBuild = getRawBuild(b)
  if (rawBuild.isBuilding()) {
    log(level: 'INFO', text: "The PR is not allowed to run in the CI yet, let's stop it")
    rawBuild.doStop()
    setDescription(rawBuild, 'Not allowed to run in the CI yet')
    sleep 5
    rawBuild.doStop()    // Try again in case the signal was not yet processed.
  }
  rawBuild = null // make null to keep pipeline serializable
  b = null // make null to keep pipeline serializable
}

@NonCPS
def getRawBuild(b) {
  return rawBuild = b.rawBuild
}

@NonCPS
def setDescription(b, description) {
  b.setDescription(description)
}
