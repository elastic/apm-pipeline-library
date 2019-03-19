//import org.jenkinsci.plugins.pipeline.github.trigger.IssueCommentCause

/**
  Check it the build was triggered by a comment in GitHub.

  def commentTrigger = isCommentTrigger()
*/
def call(){
  def triggerCause = currentBuild.rawBuild.getCauses().find { cause ->
    log(level: 'DEBUG', text: "isCommentTrigger: ${cause.getClass().getSimpleName()}")
    return cause.getClass().getSimpleName().equals('IssueCommentCause')
  }
  def ret = triggerCause != null
  log(level: 'DEBUG', text: "isCommentTrigger: ${ret}")
  if(ret){
    env.BUILD_CAUSE_USER = triggerCause.getUserLogin()
    //Only Elastic users are allowed
    def token = getGithubToken()
    def user = githubApiCall(token: token, url: "https://api.github.com/users/${env.BUILD_CAUSE_USER}")
    ret = '@elastic'.equals(user?.company)
  }
  return ret
}
