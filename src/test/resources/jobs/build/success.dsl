NAME = 'it/build/success'
DSL = '''pipeline {
  agent any
  stages {
    stage('success') {
      steps {
        writeFile file: 'junit.xml', text: '<?xml version="1.0" encoding="UTF-8"?><testsuite><testcase classname="bar" name="foo"/></testsuite>'
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
