NAME = 'it/githuEnv'
DSL = '''pipeline {
  agent any
  environment {
    REPO = 'apm-pipeline-library'
    BASE_DIR = "src/github.com/elastic/${env.REPO}"
    JOB_GIT_CREDENTIALS = "f6c7695a-671e-4f4f-a331-acdce44ff9ba"
  }
  stages {
    stage('checkout') {
      steps {
        deleteDir()
        whenFalse(env.GIT_COMMIT==null){
          error("The GIT_COMMIT env var is defined")
        }
        sh(label: 'Env vars before', script: 'export|grep GIT_')
        gitCheckout(basedir: "${BASE_DIR}",
          branch: "master",
          repo: "git@github.com:elastic/${REPO}.git",
          credentialsId: "${JOB_GIT_CREDENTIALS}"
        )
        dir("${BASE_DIR}"){
          setEnvVar("REAL_GIT_COMMIT", getGitCommitSha())
          sh(label: 'Env vars after', script: 'export|grep GIT_')
          whenFalse("${env.REAL_GIT_COMMIT}" == "${env.GIT_BASE_COMMIT}"){
            error("The GIT_BASE_COMMIT value is incorrect, we expect ${env.GIT_COMMIT} and it is ${env.GIT_BASE_COMMIT}")
          }
          whenFalse("${env.REAL_GIT_COMMIT}" == "${env.GIT_COMMIT}"){
            error("The GIT_COMMIT value is incorrect, we expect ${env.REAL_GIT_COMMIT} and it is ${env.GIT_COMMIT}")
          }
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
