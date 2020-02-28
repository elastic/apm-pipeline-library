NAME = 'it/rebuildPipeline-apm-agent-python-mbp'
DSL = '''pipeline {
  agent any
  parameters {
    booleanParam(name: 'Run_As_Master_Branch', defaultValue: false, description: 'Allow to run any steps on a PR, some steps normally only run on master branch.')
    booleanParam(name: 'bench_ci', defaultValue: true, description: 'Enable benchmarks.')
    booleanParam(name: 'tests_ci', defaultValue: true, description: 'Enable tests.')
    booleanParam(name: 'package_ci', defaultValue: true, description: 'Enable building packages.')
  }
  stages {
    stage('error') {
      steps {
        error 'force a build error'
      }
    }
  }
  post {
    cleanup {
      rebuildPipeline()
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
