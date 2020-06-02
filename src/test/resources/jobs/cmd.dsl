NAME = 'it/cmd'
DSL = '''pipeline {
  agent none
  stages {
    stage('linux') {
      agent { label 'linux && immutable' }
      steps {
        script {
          def ret = cmd(script: 'echo hi', returnStdout: true)
          echo "Returned: ${ret}"
          if (!ret.trim().equals('hi')) {
            error('Failed when asserting hi')
          }

          ret = cmd(script: 'force failure', returnStatus: true)
          if (ret == 0) {
            error('Failed when asserting an error')
          }
        }
      }
    }
    stage('windows') {
      agent { label 'windows-immutable' }
      steps {
        script {
          def ret = cmd(script: 'echo hi', returnStdout: true)
          echo "Returned: ${ret}"
          if (!ret.trim().equals('hi')) {
            error('Failed when asserting hi')
          }

          ret = cmd(script: 'force failure', returnStatus: true)
          if (ret == 0) {
            error('Failed when asserting an error')
          }
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
