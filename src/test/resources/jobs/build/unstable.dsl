NAME = 'it/build/unstable'
DSL = '''pipeline {
  agent any
  stages {
    stage('unstable') {
      steps {
        writeFile file: 'junit.xml', text: '<?xml version="1.0" encoding="UTF-8"?><testsuite><testcase classname="foo" name="bar"><error message="error"/><system-out><![CDATA[hookid: foo]]></system-out></testcase></testsuite>'
        junit 'junit.xml'
        archiveArtifacts(allowEmptyArchive: true, artifacts: 'junit.xml')
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
