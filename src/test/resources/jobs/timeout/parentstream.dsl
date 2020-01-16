NAME = 'it/timeout/parentstream'
DSL = '''
import groovy.transform.Field

@Field def rubyDownstreamJobs = [:]

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
      when { expression { return !rubyDownstreamJobs.isEmpty() } }
      steps { error("There were some failures when running the 'Test' stage.") }
    }
  }
  post {
    always {
      notifyBuildResult(downstreamJobs: rubyDownstreamJobs, rebuild: true, shouldNotify: false)
    }
  }
}
def runBuild(String type) {
  def downstreamBuild
  try {
    downstreamBuild = build(job: 'downstream', propagate: true, quietPeriod: 0,  wait: true,
                            parameters: [string(name: 'type', value: "${type}")])
  } catch(e) {
    downstreamBuild = e
    warnError('Test Failures') {
      error("Downstream job for 'downstream' with '${type}' failed")
    }
  } finally {
    rubyDownstreamJobs["downstream-${type}"] = downstreamBuild
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
