// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**
Scan the repository for third-party dependencies and report the results.

licenseScan()

*/
def call(Map params = [:]) {
  withEnvMask(vars: [
      [var: "FOSSA_API_KEY", password: getVaultSecret(secret: 'secret/jenkins-ci/fossa/api-token')?.data?.token ],
    ]){
      def scanned = false
      def isOK = true
      if(findFiles(glob: '**/*.go')){
        isOK = isOK && scanGo()
        scanned = true
      }
      if(findFiles(glob: '**/package.json')){
        isOK = isOK && scanNode()
        scanned = true
      }
      if(findFiles(glob: '**/*.rb')
        && findFiles(glob: '**/Gemfile')
      ){
        isOK = isOK && scanRuby()
        scanned = true
      }
      if(findFiles(glob: '**/*.py')
        && findFiles(glob: '**/requirements.txt')){
        isOK = isOK && scanDefault()
        scanned = true
      }
      if(findFiles(glob: '**/*.php')
        && findFiles(glob: '**/composer.json')){
        isOK = isOK && scanPhp()
        scanned = true
      }
      if(findFiles(glob: '**/build.xml')){
        isOK = isOK && scanAnt()
        scanned = true
      }
      if(findFiles(glob: '**/pom.xml')){
        isOK = isOK && scanMaven()
        scanned = true
      }
      if(findFiles(glob: '**/build.gradle')){
        isOK = isOK && scanGradle()
        scanned = true
      }
      if(findFiles(glob: '**/*.csproj')){
        isOK = isOK && scanDefault()
        scanned = true
      }

      //Try to scan the project in any case.
      if(!scanned){
        isOK = isOK && scanDefault()
      }

      if(!isOK){
        error("licenseScan: The Third party license scan failed.")
      }
    }
}


def scanGo(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      golang:1.14.4-stretch -c "
        GO111MODULE="off" go get -v -u github.com/kardianos/govendor
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanNode(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      node:lts -c "
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanRuby(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      ruby:2.5 -c "
        bundle update
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanDefault(){
  return sh(label: 'License Scanning', script: '''
    if [ ! -f .fossa.yml ]; then
      fossa init --include-all
    fi
    fossa analyze --no-ansi
  ''',
  returnStatus: true) == 0
}

def scanPhp(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      wordpress:php:7.2 -c "
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanGradle(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      gradle:6.5.0-jdk11 -c "
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanMaven(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      maven:3.6.3-jdk-11 -c "
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}

def scanAnt(){
  return sh(label: 'License Scanning', script: '''
    set +x
    docker run -t --rm \
      -e FOSSA_API_KEY=${FOSSA_API_KEY} \
      -v $(pwd):/app \
      -w /app \
      -v $(command -v fossa):/app/fossa \
      --entrypoint /bin/bash \
      docker.elastic.co/observability-ci/apache-ant:1.10.8 -c "
        if [ ! -f .fossa.yml ]; then
          ./fossa init --include-all
        fi
        ./fossa analyze --no-ansi
      "
  ''',
  returnStatus: true) == 0
}
