/**
  Check it the build was triggered by a comment in GitHub.

  def commentTrigger = isCommentTrigger()
*/
def call(){
  def ret = currentBuild.getBuildCauses()?.any{ it._class.startsWith('org.jenkinsci.plugins.pipeline.github.trigger.IssueCommentCause')}
  log(level: 'DEBUG', text: "isCommentTrigger: ${ret}")
  if(ret){
    def buildCause = currentBuild.getBuildCauses().find{ it._class.startsWith('org.jenkinsci.plugins.pipeline.github.trigger.IssueCommentCause')}
    log(level: 'DEBUG', text: "isCommentTrigger: ${buildCause}")
    env.BUILD_CAUSE_USER = buildCause?.userLogin
    //Only Elastic users are allowed
    def token = getGithubToken()
    def user = githubApiCall(token: token, url: "https://api.github.com/users/${env.BUILD_CAUSE_USER}")
    ret = '@elastic'.equals(user?.company)
  }
  return ret
}
