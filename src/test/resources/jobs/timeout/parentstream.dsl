NAME = 'it/timeout/parentstream'
DSL = '''
import groovy.transform.Field

@Field def downstreamJobs = [:]

pipeline {
  agent any
  environment {
    PIPELINE_LOG_LEVEL = 'DEBUG'
    GIT_BUILD_CAUSE = 'pr'  // gitCheckout step is not called so let's mimic its behaviour
  }
  stages {
    stage('timeout') { steps { runBuild('timeout') } }
    stage('failure') { steps { runBuild('failure') } }
    stage('success') { steps { runBuild('success') } }
    stage('unstable') { steps { runBuild('unstable') } }
    stage('Populate Test failures') {
      steps {
        script {
          if (downstreamJobs.isEmpty()) {
            error("Assertion failed - There were some failures when running the above stages.")
          } else {
            echo 'Assertion passed - populate test errors'
          }
        }
      }
    }
  }
  post {
    always {
      notifyBuildResult(downstreamJobs: downstreamJobs, rebuild: true, shouldNotify: false)
    }
  }
}
def runBuild(String type) {
  def downstreamBuild
  try {
    downstreamBuild = build(job: 'downstream', propagate: true, quietPeriod: 0,  wait: true,
                            parameters: [string(name: 'type', value: "${type}")])
    if (type.equals('timeout') || type.equals('failure')) {
      error("Assertion failed - ${type} should fail.")
    } else {
      echo 'Assertion passed - build'
    }
  } catch(e) {
    downstreamBuild = e
    if (type.equals('timeout') || type.equals('failure')) {
      echo 'Assertion passed - type'
    } else {
      error("Assertion failed - ${type} should not go throught the catch.")
    }
    if (e?.number?.trim()) {
      echo "Assertion passed - build number ${e?.number}"
    } else {
      error("Assertion failed - build number failed.")
    }
  } finally {
    downstreamJobs["downstream-${type}"] = downstreamBuild
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
