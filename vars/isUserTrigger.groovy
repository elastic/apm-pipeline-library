/**
  Check it the build was triggered by a user.

  def userTrigger = isUserTrigger()
*/
def call(){
  def ret = currentBuild.getBuildCauses()?.any{ it._class == 'hudson.model.Cause$UserIdCause'}
  log(level: 'DEBUG', text: "isTimerTrigger: ${ret}")
  if(ret){
    def buildCause = currentBuild.getBuildCauses().find{ it._class == 'hudson.model.Cause$UserIdCause'}
    env.BUILD_CAUSE_USER = buildCause?.userId
  }
  return ret
}
