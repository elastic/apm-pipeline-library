pipeline {
    agent { label 'ubuntu' }

    stages {
        stage('Hello') {
            environment {
              HOME = "${env.WORKSPACE}"
              PATH = "${env.PATH}:${HOME}/bin"
            }
            steps {
              deleteDir()
              ghInstall(folder: "${HOME}/bin")
              sshagent(['f6c7695a-671e-4f4f-a331-acdce44ff9ba']) {
                  withCredentials([usernamePassword(credentialsId: '2a9602aa-ab9f-4e52-baf3-b71ca88469c7-UserAndToken', passwordVariable: 'GH_TOKEN', usernameVariable: 'GH_USERNAME')]) {
                    ghLogin(token: env.GH_TOKEN)
                    ghClone(repo: 'git@github.com:elastic/observability-test-environments.git')
                    dir('observability-test-environments'){
                      sh 'touch test'
                      ghPush()
                      ghPR()
                    }
                  }
              }
            }
        }
    }
}

def ghInstall(Map args = [:]){
  def folder = args.containsKey('folder') ? args.folder : error('ghInstall: Folder invalid.')
  dir("${folder}"){
    sh(label: 'Install gh', script: """
      VERSION=1.4.0
      OS=\$(uname -s|tr [:upper:] [:lower:])
      ARCH=amd64
      VERSION_NAME=gh_\${VERSION}_\${OS}_\${ARCH}

      if [ "\${OS}" == "darwin" ]; then
        OS="macOS"
      fi
      curl -sSfL -o "\${VERSION_NAME}.tgz" "https://github.com/cli/cli/releases/download/v\${VERSION}/\${VERSION_NAME}.tar.gz"
      tar -xzf "\${VERSION_NAME}.tgz"
      mv "\${VERSION_NAME}/bin/gh" "${folder}"
      rm -fr "\${VERSION_NAME}.tgz" "\${VERSION_NAME}"
      gh version
      gh config set prompt disabled
      gh config set git_protocol ssh
      gh auth status || true
    """)
  }
}

def ghLogin(Map args = [:]){
  def token = args.containsKey('token') ? args.token : error('ghLogin: GitHub Token invalid.')
  withEnv(["GH_TOKEN=${token}","GH_HOST=github.com"]){
    sh(label: 'GitHub login', script: """
      echo \${GH_TOKEN}|gh auth login --with-token

      #GITHUB_USERNAME="\$(gh api /user | jq -r .login)"
      #git config --global credential.helper store

      #git config --global credential.helper '!f() { sleep 1; echo "username=\${GITHUB_USERNAME}"; echo "password=\${GH_TOKEN}"; }; f'

      #echo "https://\${GITHUB_USERNAME}:\${GH_TOKEN}@github.com" > "\${HOME}/.git-credentials"
      #echo "https://\${GH_TOKEN}:x-oauth-basic@github.com" > "\${HOME}/.git-credentials"

      # Test this https://github.com/cli/cli/issues/1633#issuecomment-759827549
      #git config --global 'credential.https://github.com' '!gh auth git-credential'
    """)
  }
  sh('gh auth status')
}

def ghLogout(){
  sh(label: 'GitHub logout', script: 'gh logout')
}

def ghClone(Map args = [:]){
  def repo = args.containsKey('repo') ? args.repo : error('ghClone: GitHub repo invalid.')
  def src = args.containsKey('src') ? args.src : 'src'
  sh(label: "gh clone", script: "gh repo clone ${repo} ${src}")
}

def ghPush(Map args = [:]){

}

def ghPR(Map args = [:]){

}
