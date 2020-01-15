NAME = 'it/timeout/downstream'
DSL = '''
pipeline {
  agent any
  parameters {
    choice(name: 'type', choices: ['timeout', 'success', 'unstable', 'failure'], description: '')
  }
  stages {
    stage('timeout') {
      when {
        expression { return (params.type == 'timeout') }
      }
      steps {
        error 'Issue: timeout checkout'
      }
      post {
        always {
          script { currentBuild.description = 'Issue: timeout checkout'}
        }
      }
    }
    stage('failure') {
      when {
        expression { return (params.type == 'failure') }
      }
      steps {
        error 'Something else'
      }
    }
    stage('success') {
      when {
        expression { return (params.type == 'success') }
      }
      steps {
        echo 'Success'
      }
    }
    stage('unstable') {
      when {
        expression { return (params.type == 'unstable') }
      }
      steps {
        writeFile file: 'junit.xml', text: """<testsuite tests="2">
                                          <testcase classname="foo1" name="ASuccessfulTest"/>
                                          <testcase classname="foo2" name="AFailingTest">
                                              <failure type="NotEnoughFoo"> details about failure </failure>
                                          </testcase>
                                      </testsuite>"""
        junit 'junit.xml'
      }
    }
  }
}
'''

pipelineJob(NAME) {
  parameters {
    choiceParam('type', ['timeout', 'success', 'unstable', 'failure'])
  }
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
