NAME = 'it/withTotpVault'
DSL = '''pipeline {
  agent any
  stages {
    stage('query totp in vault') {
      steps {
        withTotpVault(secret: 'totp-apm/code/v1v', code_var_name: 'VAULT_TOTP'){
          sh 'echo "VAULT_TOTP=${VAULT_TOTP}" > file.txt'
        }
        sh 'cat file.txt'
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
