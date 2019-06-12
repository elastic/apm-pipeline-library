@Library('apm@current') _

pipeline {
  agent { label 'master' }
  environment {
    NOTIFY_TO = credentials('notify-to')
    PIPELINE_LOG_LEVEL='INFO'
    DOCKERHUB_SECRET = 'secret/apm-team/ci/elastic-observability-dockerhub'
    DOCKERELASTIC_SECRET = 'secret/apm-team/ci/docker-registry/prod'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    cron('H H(4-5) * * 1-5')
  }
  stages {
    stage('Run Tasks'){
      steps {
        build(job: 'apm-shared/apm-test-pipeline',
          parameters: [string(name: 'branch_specifier', value: 'master')],
          propagate: false,
          wait: false
        )

        build(job: 'apm-shared/apm-docker-images-pipeline',
          parameters: [
            string(name: 'registry', value: 'docker.elastic.co'),
            string(name: 'tag_prefix', value: 'observability-ci'),
            string(name: 'secret', value: 'secret/apm-team/ci/docker-registry/prod'),
            booleanParam(name: 'python', value: true),
            booleanParam(name: 'weblogic', value: true),
            booleanParam(name: 'apm_integration_testing', value: true),
            booleanParam(name: 'helm_kubectl', value: true),
            booleanParam(name: 'jruby', value: true),
            string(name: 'branch_specifier', value: 'master')
          ],
          propagate: false,
          wait: false
        )

        build(job: 'apm-shared/apm-docker-opbeans-pipeline',
          parameters: [
            string(name: 'registry', value: 'docker.elastic.co'),
            string(name: 'tag_prefix', value: 'observability-ci'),
            string(name: 'version', value: 'daily'),
            string(name: 'secret', value: "${DOCKERELASTIC_SECRET}"),
            booleanParam(name: 'opbeans', value: true),
            string(name: 'branch_specifier', value: 'master')
          ],
          propagate: false,
          wait: false
        )
      }
    }
  }
  post {
    always {
      notifyBuildResult()
    }
  }
}
