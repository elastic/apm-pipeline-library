pipelineJob('hello-world') {
  definition {
    cpsScm {
      scm {
        git {
          remote {
            url('https://github.com/elastic/apm-pipeline-library.git')
          }
          branch('*/master')
        }
      }
      lightweight()
    }
  }
}
