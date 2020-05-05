NAME = 'it/build/parentstream'
DSL = '''pipeline {
  agent any
  stages {
    stage('trigger downstreams') {
      steps {
        script {
          def success = build job: 'success'
          dir('success') {
            copyArtifacts(projectName: 'success', filter: 'junit.xml', selector: specific(buildNumber: success.number.toString()))
          }
          def unstable = build job: 'unstable'
          dir('unstable') {
            copyArtifacts(projectName: 'unstable', filter: 'junit.xml', selector: specific(buildNumber: unstable.number.toString()))
          }
          junit '**/junit.xml'
        }
      }
    }
    stage('trigger downstreams with errors') {
      steps {
        deleteDir()
        script {
          try {
            build job: 'error'
          } catch(e) {
            dir('error') {
              copyArtifacts(projectName: 'error', filter: 'junit.xml', selector: specific(buildNumber: e.number.toString()))
            }
          }
          junit '**/junit.xml'
        }
      }
    }
  }
}'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
