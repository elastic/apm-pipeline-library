NAME = 'it/log'
DSL = '''
import groovy.transform.Field
@Field def stashedTestReports = [:]
pipeline {
  agent any
  environment {
      BASE_DIR = 'src'
  }
  stages {
    stage('test-1') {
      steps {
        writeFile file: 'junit-1.xml', text: '<?xml version="1.0" encoding="UTF-8"?><testsuite><testcase classname="foo" name="bar"><error message="error"/><system-out><![CDATA[hookid: foo]]></system-out></testcase></testsuite>'
        junitAndStore(stashedTestReports: stashedTestReports, id: 'test-1', testResults: 'junit-1.xml')
      }
    }
    stage('test-2') {
      steps {
        writeFile file: 'junit-2.xml', text: '<?xml version="1.0" encoding="UTF-8"?><testsuite><testcase classname="foo" name="bar"><error message="error"/><system-out><![CDATA[hookid: foo]]></system-out></testcase></testsuite>'
        junitAndStore(stashedTestReports: stashedTestReports, testResults: 'junit-2.xml')
      }
    }
  }
  post {
    always {
      runbld(stashedTestReports: stashedTestReports, project: 'acme')
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
