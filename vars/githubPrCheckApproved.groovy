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
  If the current build is a PR, it would check if it is approved or created
  by a user with write/admin permission on the repo or a trusted user.

  If it is not approved, the method will throw an error.

  githubPrCheckApproved()
*/
def call(Map params = [:]){
  def changeId =  params.get('changeId', env.CHANGE_ID)
  def org = params.get('org', env.ORG_NAME)
  def repo = params.get('repo', env.REPO_NAME)
  def token = params.get('token', getGithubToken())

  if(!changeId){
    return true
  }
  def approved = false
  def repoName = "${org}/${repo}"
  def pr = githubPrInfo(token: token, repo: repoName, pr: changeId)
  def reviews = githubPrReviews(token: token, repo: repoName, pr: changeId)
  def user = pr?.user?.login
  def userType = pr?.user?.type

  log(level: 'INFO', text: "githubPrCheckApproved: Title: ${pr?.title} - User: ${user} - Author Association: ${pr?.author_association}")

  // The PR is approved to be executed in the CI for the below reasons:
  // - An user with write permissions raised the PR.
  // - An authorized bot created the PR.
  // - If it has already been approved by a member or collaborator.
  // - A trusted user for that particular repo.
  //
  approved = user != null && (isPrApproved(reviews) ||
                              hasWritePermission(token, repoName, user) ||
                              isAuthorizedBot(user, userType) ||
                              isAuthorizedUser(repoName, user))

  if(!approved){
    def message = 'The PR is not allowed to run in the CI yet'
    catchError(buildResult: 'SUCCESS', message: "githubPrCheckApproved: ${message}") {
      error("githubPrCheckApproved: ${message}. (Only users with write permissions can do so.)")
    }
    abortBuild(build: currentBuild, message: message)
    // Abort build is required to run twice with certain sleep to ensure the build gets aborted,
    // otherwise it won't be stopped. Unfortunately this logic cannot be moved to the abortBuild
    // step without facing the NotSerializableException.
    sleep 5
    abortBuild(build: currentBuild, message: message)
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
  def authorizedBots = ['greenkeeper[bot]', 'dependabot[bot]']
  log(level: 'DEBUG', text: "githubPrCheckApproved: User: ${login}, Type: ${type}")
  def ret = false;
  if('bot'.equalsIgnoreCase(type)){
    ret = authorizedBots.any{ it.equals(login) }
  }
  return ret;
}

/**
  Check if the PR come from a trusted user. For such it requires the access to
  the env variable REPO_NAME.
*/
def isAuthorizedUser(repo, login){
  log(level: 'DEBUG', text: "githubPrCheckApproved: isAuthorizedUser(${login})")
  def ret = false
  if(repo) {
    try {
      def fileContent = libraryResource("approval-list/${repo}.yml")
      def authorizedUsers = readYaml(text: fileContent)['USERS']
      ret = authorizedUsers.any{ it.equals(login) }
    } catch(e) {
      ret = false
    }
  } else {
    ret = false
  }
  return ret
}
