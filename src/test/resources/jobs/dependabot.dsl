NAME = 'it/dependabot'
DSL = '''pipeline {
  agent any
  stages {
    stage('dependabot') {
      steps {
        dependabot(project: 'v1v/apm-agent-rum-js', package: 'npm_and_yarn')
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
