/**
  Check it the build was triggerd by a timer (scheduled job).

  def timmerTrigger = isTimerTrigger()
*/
def isTimerTrigger(){
  def ret = false
  if(currentBuild.getBuildCauses()?.any{ bc -> bc._class == 'hudson.triggers.TimerTrigger$TimerTriggerCause'}){
    ret = true
  }
  log(level: 'DEBUG', text: "isTimerTrigger: ${ret}")
  return ret
}
