/**
  Check it the build was triggered by a timer (scheduled job).

  def timmerTrigger = isTimerTrigger()
*/
def call(){
  def ret = currentBuild.getBuildCauses()?.any{ it._class == 'hudson.triggers.TimerTrigger$TimerTriggerCause'}
  log(level: 'DEBUG', text: "isTimerTrigger: ${ret}")
  return ret
}
