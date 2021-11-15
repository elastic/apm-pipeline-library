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


/*
FIXME require to approve a few classes to run, we have to find an alternative.
import org.yaml.snakeyaml.Yaml
import java.util.ArrayList

class YamlParser {
  public static List<String> getVersions(String yamlUrl, String key){
    Yaml yamlParser = new Yaml()
    def yamlContent = getContent(yamlUrl)
    return yamlParser.load(yamlContent)[key]
  }
  public static String getContent(String yamlUrl){
    return new java.net.URL(yamlUrl).getText()
  }
}

def pythonVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-python/master/.ci/.jenkins_python.yml', 'PYTHON_VERSION')
def nodeVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-nodejs/master/.ci/.jenkins_nodejs.yml', 'NODEJS_VERSION')
def rubyVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-ruby/master/.ci/.jenkins_ruby.yml', 'RUBY_VERSION')
def libraries = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-rum-js/master/.ci/.jenkins_rum.yml', 'TEST_LIBRARIES')
def nodejsVersion = YamlParser.getContent('https://raw.githubusercontent.com/elastic/apm-agent-rum-js/master/dev-utils/.node-version')

*/

def registry = "docker.elastic.co"
def prefix = "observability-ci"

def dockerImages = [
  [
    name: 'opbeans-dotnet',
    repo: 'https://github.com/elastic/opbeans-dotnet.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-node',
    repo: 'https://github.com/elastic/opbeans-node.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-python',
    repo: 'https://github.com/elastic/opbeans-python.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-frontend',
    repo: 'https://github.com/elastic/opbeans-frontend.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-java',
    repo: 'https://github.com/elastic/opbeans-java.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-go',
    repo: 'https://github.com/elastic/opbeans-go.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-loadgen',
    repo: 'https://github.com/elastic/opbeans-loadgen.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-ruby',
    repo: 'https://github.com/elastic/opbeans-ruby.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  [
    name: 'opbeans-loadgen',
    repo: 'https://github.com/elastic/opbeans-loadgen.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],
  /** FIXME disable until it is fully implemented: https://github.com/elastic/opbeans-flask/pull/5
  [
    name: 'opbeans-flask',
    repo: 'https://github.com/elastic/opbeans-flask.git',
    tag: 'daily',
    folder: '.',
    push: true
  ],*/
  [
    name: 'metricbeat-integrations-images',
    repo: 'https://github.com/elastic/beats.git',
    folder: 'metricbeat',
    push: true,
    docker_build_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:buildSupportedVersions',
    docker_push_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:pushSupportedVersions'
  ],
  [
    name: 'metricbeat-integrations-images-x-pack',
    repo: 'https://github.com/elastic/beats.git',
    folder: 'x-pack/metricbeat',
    push: true,
    docker_build_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:buildSupportedVersions',
    docker_push_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:pushSupportedVersions'
  ],
  [
    name: 'apm-proxy',
    repo: 'https://github.com/elastic/observability-dev',
    tag: 'latest',
    folder: 'tools/apm_proxy/frontend',
    push: true,
    prepare_script: 'git clone https://github.com/haproxytech/spoa-mirror.git'
  ],
  [
    name: 'apm-proxy-be',
    repo: 'https://github.com/elastic/observability-dev',
    tag: 'latest',
    folder: 'tools/apm_proxy/backend',
    push: true
  ],
]


/*
  APM Agent Python Docker images
*/
def pythonVersions = [
  "python-3.6",
  "python-3.7",
  "python-3.8",
  "python-3.9",
  "python-3.10-rc",
  "pypy-3"
]
pythonVersions.each{ version ->
  def pythonVersion = version.replaceFirst("-",":")
  dockerImages.add([
    job: "apm-agent-python-${version}",
    name: "apm-agent-python",
    repo: 'https://github.com/elastic/apm-agent-python.git',
    tag: "${version}",
    folder: 'tests',
    docker_build_opts: "--build-arg PYTHON_IMAGE=${pythonVersion}",
    push: true
  ])
}

/*
  APM Agent Node.js Docker images
*/
def nodeVersions = [
  "17",
  "16",
  "16.0",
  "14",
  "14.0",
  "12",
  "12.0",
  "10",
  "10.0",
  "8",
  "8.6"
]
nodeVersions.each{ version ->
  def nodejsVersion = version.replaceFirst('"', '')
  dockerImages.add([
    job: "apm-agent-nodejs-${version}",
    name: "apm-agent-nodejs",
    repo: 'https://github.com/elastic/apm-agent-nodejs.git',
    tag: "${version}",
    folder: '.ci/docker/node-container',
    docker_build_opts: "--build-arg NODE_VERSION=${nodejsVersion}",
    push: true
  ])
}

/*
  APM Agent Ruby Docker images
*/

dockerImages.add([
  name: "apm-agent-jruby",
  repo: 'https://github.com/elastic/apm-agent-ruby.git',
  folder: '.ci/docker/jruby',
  push: true,
  docker_build_script: "./run.sh --action build --registry ${registry}/${prefix}",
  docker_test_script: "./run.sh --action test --registry ${registry}/${prefix}",
  docker_push_script: "./run.sh --action push --registry ${registry}/${prefix}"
])

def rubyVersions = [
  "ruby:3.0",
  "ruby:2.7",
  "ruby:2.6",
  "ruby:2.5",
  "ruby:2.4",
  "jruby:9.2",
  "docker.elastic.co/observability-ci/jruby:9.2-13-jdk",
  "docker.elastic.co/observability-ci/jruby:9.2-11-jdk",
  "docker.elastic.co/observability-ci/jruby:9.2-8-jdk"
]
// The ones with the observability-ci tag are already built at the very end
// of this pipeline.
rubyVersions.findAll { element -> !element.contains('observability-ci') }.each { version ->
  def rubyVersion = version.replaceFirst(":","-")
  dockerImages.add([
    job: "apm-agent-ruby-${rubyVersion}",
    name: "apm-agent-ruby",
    repo: 'https://github.com/elastic/apm-agent-ruby.git',
    tag: "${rubyVersion}",
    folder: 'spec',
    docker_build_opts: "--build-arg RUBY_IMAGE='${version}'",
    push: true
  ])
}

/*
  APM Agent RUM Docker images
*/
def libraries = [
  "playwright",
  "puppeteer"
]
def nodejsVersion = "12"
libraries.each { library ->
  dockerImages.add([
    name: "node-${library}",
    repo: 'https://github.com/elastic/apm-agent-ruby.git',
    tag: "${nodejsVersion}",
    folder: '.ci/docker/node-${library}',
    docker_build_opts: "--build-arg NODEJS_VERSION='${nodejsVersion}'",
    push: true
  ])
}

dockerImages.each{ item ->
  pipelineJob("apm-shared/docker-images/${item.job ? item.job : item.name}") {
    displayName("${item.name} ${item.tag ? item.tag : ''} - Docker image")
    description("Job to build and push the ${item.name} ${item.tag ? item.tag : ''} Docker image")
    parameters {
      stringParam('registry', "${registry}", "Docker Registry.")
      stringParam('prefix', "${prefix}", "Docker registry namespace.")
      stringParam('tag', "${item.tag ? item.tag : 'latest'}", "Docker image tag.")
      stringParam('name', "${item.name}", "Docker image name.")
      stringParam('folder', "${item.folder ? item.folder : '.'}", "Folder where the Dockrefile is.")
      stringParam('repo', "${item.repo}", "Repository where the Docker file is.")
      booleanParam('push', item.push, "True to push the Docker image to the registry.")
      stringParam('docker_build_opts', "", "Additional flags to the default docker build command.")
      stringParam('docker_build_script', "${item.build_script ? item.build_script : ''}", "Script/command to build the Docker image.")
      stringParam('docker_test_script', "${item.test_script ? item.test_script : ''}", "Script/command to test the Docker image.")
      stringParam('docker_push_script', "${item.push_script ? item.push_script : ''}", "Script/command to push the Docker image.")
      stringParam('prepare_script', "${item.prepare_script ? item.prepare_script : ''}", "Script/command to run before everything.")
    }
    disabled(false)
    quietPeriod(10)
    logRotator {
      numToKeep(10)
      daysToKeep(7)
      artifactNumToKeep(10)
      artifactDaysToKeep(-1)
    }
    triggers {
      cron('H H(3-4) * * 1-5')
    }
    definition {
      cpsScm {
        scm {
          git {
            remote {
              github("elastic/apm-pipeline-library", "ssh")
              credentials("f6c7695a-671e-4f4f-a331-acdce44ff9ba")
            }
            branch('${branch_specifier}')
            extensions {
              wipeOutWorkspace()
            }
          }
        }
        lightweight(false)
        scriptPath(".ci/build-docker-images.groovy")
      }
    }
  }
}
