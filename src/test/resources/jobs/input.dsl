NAME = 'it/input'
DSL = '''
pipeline {
  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  stages {
    stage('Timeout') {
      options { timeout(time: 1, unit: 'SECONDS') }
      stages {
        stage('Input') {
          input {
            message 'Should we release a new version?'
            ok 'Yes, we should.'
          }
          steps { echo 'Unreached. Timeout should be triggered' }
        }
        stage('When-Timeout') {
          when { expression { return (env.INPUT_ABORTED == 'false') } }
          steps { echo 'Reached when no aborted' }
        }
        stage('Always') {
          steps { echo "INPUT_ABORTED=${env.INPUT_ABORTED}" }
        }
      }
    }
    stage('Manual') {
      stages {
        stage('Approval') {
          input {
            message 'Should we release a new version?'
            ok 'Yes, we should.'
          }
          when { expression { return (env.INPUT_ABORTED == 'false') } }
          steps { echo 'maybe reachable' }
        }
        stage('When-Manual') {
          when { expression { return (env.INPUT_ABORTED == 'false') } }
          steps { echo 'Reached when no aborted' }
        }
        stage('Always') {
          steps { echo "INPUT_ABORTED=${env.INPUT_ABORTED}" }
        }
      }
    }
    stage('ManualWithVariable') {
      stages {
        stage('Approval') {
          input {
            message 'Should we release a new version?'
            ok 'Yes, we should.'
            parameters {
              choice(
                choices: [
                  'foo',
                  'bar'
                 ],
                 description: 'URL',
                 name: 'URL')
            }
          }
          when { expression { return (env.INPUT_ABORTED == 'false') } }
          steps { validateVariable(variable) }
        }
      }
    }
    stage('InputStep') {
      stages {
        stage('Approval') {
          agent none
          steps {
            script {
              // Scripted approach
              def ret = input message: 'input step', ok: 'caption', parameters: [choice(choices: ['foo', 'bar'], description: '', name: 'variable')], submitterParameter: 'user_submitter'
              if (env.INPUT_ABORTED == 'false') {
                validateVariable(ret.variable)
              }
            }
          }
        }
        stage('When-Manual') {
          when { expression { return (env.INPUT_ABORTED == 'false') } }
          steps { echo 'Reached when no aborted' }
        }
      }
    }
  }
}

def validateVariable(variable) {
  echo "${variable}"
  if (variable.equals('foo') || variable.equals('bar') ) {
    echo 'found'
  } else {
    error 'This should not happen!!'
  }
}
'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
