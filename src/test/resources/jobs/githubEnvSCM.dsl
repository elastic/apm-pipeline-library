NAME = 'it/githuEnvSCM'

pipelineJob(NAME) {
  definition {
    cpsScm {
        scm {
            git('https://github.com/kuisathaverat/test.git')
        }
        scriptPath('githubEnv.groovy')
    }
  }
}
