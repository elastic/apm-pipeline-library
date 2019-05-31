#!groovy

package co.elastic;

import groovy.text.StreamingTemplateEngine

/**
 * This method returns a string with the template filled with groovy variables
 */
def emailTemplate(params) {

    def fileName = "groovy-html-custom.groovy"
    def fileContents = libraryResource(fileName)
    def engine = new StreamingTemplateEngine()

    return engine.createTemplate(fileContents).make(params).toString()
}

/**
 * This method send an email generated with data from Jenkins
 * @param buildStatus String with job result
 * @param emailRecipients Array with emails: emailRecipients = []
 */
def notifyEmail(Map params = [:]) {
    def build = params.containsKey('build') ? params.build : error('notifyEmail: build parameter it is not valid')
    def buildStatus = params.containsKey('buildStatus') ? params.buildStatus : error('notifyEmail: buildStatus parameter is not valid')
    def emailRecipients = params.containsKey('emailRecipients') ? params.emailRecipients : error('notifyEmail: emailRecipients parameter is not valid')
    def testsSummary = params.containsKey('testsSummary') ? params.testsSummary : null
    def changeSet = params.containsKey('changeSet') ? params.changeSet : []
    def statsUrl = params.containsKey('statsUrl') ? params.statsUrl : ''
    def log = params.containsKey('log') ? params.log : null
    def stepsErrors = params.containsKey('stepsErrors') ? params.stepsErrors : []
    def testsErrors = params.containsKey('testsErrors') ? params.testsErrors : []

    try {

        def icon = "✅"
        def statusSuccess = true

        if(buildStatus != "SUCCESSFUL") {
            icon = "❌"
            statusSuccess = false
        }

        def jobName = env.JOB_NAME.replace("/","%2F")
        def boURL = "${JENKINS_URL}/blue/organizations/jenkins/${jobName}/detail/${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}"

        def body = emailTemplate([
            "jobUrl": boURL,
            "build": build,
            "jenkinsText": env.JOB_NAME,
            "jenkinsUrl": env.JENKINS_URL,
            "statusSuccess": statusSuccess,
            "testsSummary": testsSummary,
            "changeSet": changeSet,
            "statsUrl": statsUrl,
            "log": log,
            "stepsErrors": stepsErrors,
            "testsErrors": testsErrors
        ]);

        mail(to: emailRecipients.join(","),
          subject: "${icon} ${buildStatus} ${env.JOB_NAME}#${env.BRANCH_NAME} ${env.BUILD_NUMBER}",
          body: body,
          mimeType: 'text/html'
        );

    } catch (e){
      log(level: 'ERROR', text: "notifyEmail: Error sending the email - ${e}")
    }
}
