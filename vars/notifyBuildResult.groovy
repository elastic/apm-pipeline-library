/**

**/

def call(Map params = [:]) {
  def  = params.containsKey('level') ? getLogLevelNum(params.level) : getLogLevelNum('DEBUG')

  emailext body: '''${SCRIPT, template="resources/groovy-html.template"}''',
    mimeType: 'text/html',
    subject: currentBuild.currentResult + " : 1 " + env.JOB_NAME,
    //"Status: ${currentBuild.result?:'SUCCESS'} - Job \'${env.JOB_NAME}:${env.BUILD_NUMBER}\'",
    attachLog: true,
    compressLog: true,
    recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
    to: "ivan.fernandez@elastic.co"

}
