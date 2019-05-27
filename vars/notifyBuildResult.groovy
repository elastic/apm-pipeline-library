/**

**/

def call(Map params = [:]) {
//  def  = params.containsKey('level') ? getLogLevelNum(params.level) : getLogLevelNum('DEBUG')

  node('master'){
    emailext body: '''${SCRIPT, template="groovy-html.template"}''',
      mimeType: 'text/html',
      subject: currentBuild.currentResult + " : 1 " + env.JOB_NAME,
      //"Status: ${currentBuild.result?:'SUCCESS'} - Job \'${env.JOB_NAME}:${env.BUILD_NUMBER}\'",
      attachLog: true,
      compressLog: true,
      recipientProviders: [brokenTestsSuspects(), brokenBuildSuspects(), upstreamDevelopers()],
      to: "ivan.fernandez@elastic.co"
  }

  //job info
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH
  //build info
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/
  //tests execution summary
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/blueTestSummary/
  //Tests executed
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/tests/
  //curl -L https://apm-ci.elastic.co/blue/rest/organizations/jenkins/pipelines/apm-shared/pipelines/apm-apm-pipeline-library-mbp/branches/develop/runs/23/tests/|jq '.[]|select(.status=="PASSED")'

  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/changeSet/
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/artifacts/
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/changeSet/
  //Steps execution info
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/steps/
  //Stages execution info
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/nodes/
  //curl https://apm-ci.elastic.co/blue/rest/organizations/jenkins/pipelines/apm-shared/pipelines/apm-apm-pipeline-library-mbp/branches/develop/runs/23/nodes/|jq '.[]|select(.result=="SUCCESS")'

  //Full pipeline log
  //JENKINS_URL/blue/rest/organizations/jenkins/pipelines/JOB_PATH/runs/BUILD_NUMBER/log/


}
