pipeline {
    agent {label 'master'}
    stages {
        stage('Generate Jobs') {
            steps {
                checkout([$class: 'GitSCM',
                  branches: [[name: '*/jobDSL']],
                  doGenerateSubmoduleConfigurations: false,
                  extensions: [],
                  submoduleCfg: [],
                  userRemoteConfigs: [[
                    credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken',
                    url: 'http://github.com/kuisathaverat/apm-pipeline-library.git'
                ]]])
                jobDsl(failOnMissingPlugin: true,
                    removedConfigFilesAction: 'DELETE',
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    targets: '.ci/jobsDSL/**/*.dsl',
                    unstableOnDeprecation: true
                )
            }
        }
    }
}
