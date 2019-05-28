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
def notifyEmail(build, buildStatus, emailRecipients, testsSummary, changeSet, statsUrl, log) {

    try {

        def icon = "✅"
        def statusSuccess = true

        if(buildStatus != "SUCCESSFUL") {
            icon = "❌"
            statusSuccess = false
        }

        def body = emailTemplate([
            "build": build,
            "jenkinsText": env.JOB_NAME,
            "jenkinsUrl": env.RUN_DISPLAY_URL,
            "statusSuccess": statusSuccess,
            "testsSummary": testsSummary,
            "changeSet": changeSet,
            "statsUrl": statsUrl,
            "log": log
        ]);

        mail(to: emailRecipients.join(","),
          subject: "${icon} ${buildStatus} ${env.JOB_NAME}#${env.BRANCH_NAME} ${env.BUILD_NUMBER}",
          body: body,
          mimeType: 'text/html'
        );

    } catch (e){
        println "ERROR SENDING EMAIL ${e}"
    }
}
