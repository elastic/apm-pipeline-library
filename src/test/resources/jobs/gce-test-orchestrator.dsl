NAME = 'it/gce-test-orchestrator'
DSL = '''
pipeline {
  agent none
  stages {
    stage('Test for a while') {
      steps {
        parallelStepsForAWhile()
      }
    }
    stage('Test in batches') {
      steps {
        buildCall(10, 10)
      }
    }
    stage('Test in massive requests') {
      steps {
        buildCall(3, 100)
      }
    }
  }
}

def buildCall(num, factor) {
  for (i = (1 * factor); i < (num * factor); (i++ * factor)) {
    buildRun(i, false)
    // Let's wait until all the builds for the first batch are done.
    sleep 5
    buildRun(1, true)
  }
}

def buildRun(i, wait) {
  build(job: 'gce-test', propagate: false, quietPeriod: 0,  wait: wait,
          parameters: [string(name: 'num', value: "${i}")])
}

// It runs in batches of 10 node requests
def parallelStepsForAWhile(){
  for (i = 0; i < 100; i++) {
    buildRun(10, false)
    sleep 5
    buildRun(1, true)
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
