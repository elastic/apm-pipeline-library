NAME = 'it/matrixAgent'
DSL = '''pipeline {
  agent any
  stages {
    stage('Matrix sample') {
      steps {
        matrix(
          agent: 'linux',
          axes:[
            axis('VAR_NAME_00', [ 1, 2 ]),
            axis('VAR_NAME_01', [ 'a', 'b', 'c', 'd', 'e' ])
          ],
          excludes: [
            axis('VAR_NAME_00', [ 1 ]),
            axis('VAR_NAME_01', [ 'd', 'e' ]),
          ]
        ) {
          echo "${VAR_NAME_00} - ${VAR_NAME_01}"
        }
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
