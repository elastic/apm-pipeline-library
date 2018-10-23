/**
 Send the becnhmarks to the cloud service.
 
 sendBenchmarks()
 sendBenchmarks(file: 'bench.out')
*/
def call(Map params = [:]) {
  def benchFile = params.containsKey('file') ? params.file : 'bench.out'
  
  withEnvBenchmarksData {
    sh """#!/bin/bash
    set +x
    go get -v -u github.com/elastic/gobench
    \${GOPATH}/bin/gobench -index benchmark-go -es "\${CLOUD_URL}" < ${benchFile}
    """
  }
}