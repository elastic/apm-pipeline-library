NAME = 'it/beats/beatsWhen'
DSL = '''pipeline {
  agent { label 'linux && immutable' }
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
  }
  parameters {
    booleanParam(name: 'auditbeat', defaultValue: 'true', description: '')
  }
  stages {
    stage('prepare') {
      steps {
        deleteDir()

        writeYaml(file: 'branches.yaml', data: readYaml(text: """
when:
  branches: true
""").when)

        writeYaml(file: 'comments.yaml', data: readYaml(text: """
when:
  comments:
    - "/test auditbeat"
    - "foo"
""").when)

        writeYaml(file: 'labels.yaml', data: readYaml(text: """
when:
  labels:
    - "auditbeat"
    - "foo"
""").when)

        writeYaml(file: 'parameters.yaml', data: readYaml(text: """
when:
  parameters:
    - "auditbeat"
    - "foo"
""").when)

        writeYaml(file: 'tags.yaml', data: readYaml(text: """
when:
  tags: true
""").when)
      }
    }
    stage('branches') {
      environment { BRANCH_NAME = 'foo' }
      steps { verify('branches.yaml') }
    }
    stage('comment') {
      environment { GITHUB_COMMENT = 'foo' }
      steps { verify('comments.yaml') }
    }
    stage('labels') {
      // labels work only for MBPs
      steps { verifyFalse('labels.yaml') }
    }
    stage('parameters') {
      steps { verify('parameters.yaml') }
    }
    stage('tags') {
      environment { TAG_NAME = 'foo' }
      steps { verify('tags.yaml') }
    }
    stage('markdown') {
      steps { archiveArtifacts(allowEmptyArchive: false, artifacts: 'build-reasons/*.*') }
    }
  }
}

def verify(fileName) {
  whenFalse(beatsWhen(project: 'test', content: readYaml(file: fileName))) {
    error 'Assert failed'
  }
}

def verifyFalse(fileName) {
  whenTrue(beatsWhen(project: 'test', content: readYaml(file: fileName))) {
    error 'Assert failed'
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
