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

def pythonVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-python/main/.ci/.jenkins_python.yml', 'PYTHON_VERSION')
def nodeVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-nodejs/main/.ci/.jenkins_nodejs.yml', 'NODEJS_VERSION')
def rubyVersions = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-ruby/main/.ci/.jenkins_ruby.yml', 'RUBY_VERSION')
def libraries = YamlParser.getVersions('https://raw.githubusercontent.com/elastic/apm-agent-rum-js/main/.ci/.jenkins_rum.yml', 'TEST_LIBRARIES')
def nodejsVersion = YamlParser.getContent('https://raw.githubusercontent.com/elastic/apm-agent-rum-js/main/dev-utils/.node-version')

*/

def registry = "docker.elastic.co"
def prefix = "observability-ci"

def dockerImages = [
  [
    name: 'metricbeat-integrations-images',
    repo: 'git@github.com:elastic/beats.git',
    folder: 'metricbeat',
    push: true,
    build_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:buildSupportedVersions',
    push_script: 'eval $(gvm $(cat ../.go-version)) && make mage && mage compose:pushSupportedVersions'
  ],
  [
    name: 'metricbeat-integrations-images-x-pack',
    repo: 'git@github.com:elastic/beats.git',
    folder: 'x-pack/metricbeat',
    push: true,
    build_script: 'eval $(gvm $(cat ../../.go-version)) && make mage && mage compose:buildSupportedVersions',
    push_script: 'eval $(gvm $(cat ../../.go-version)) && make mage && mage compose:pushSupportedVersions'
  ],
  [
    name: 'apm-proxy',
    repo: 'git@github.com:elastic/observability-dev',
    branch_docker: 'main',
    tag: 'latest',
    folder: 'tools/apm_proxy/frontend',
    push: true,
    prepare_script: 'cd tools/apm_proxy/frontend && git clone git@github.com:haproxytech/spoa-mirror.git'
  ],
  [
    name: 'apm-proxy-be',
    repo: 'git@github.com:elastic/observability-dev',
    branch_docker: 'main',
    tag: 'latest',
    folder: 'tools/apm_proxy/backend',
    push: true
  ],
  [
    name: 'functional-opbeans',
    repo: 'git@github.com:elastic/observability-test-environments.git',
    tag: 'latest',
    folder: 'tests',
    build_script: "docker build --force-rm -t ${registry}/${prefix}/functional-opbeans:latest functional-opbeans",
    push_script: "docker push ${registry}/${prefix}/functional-opbeans:latest",
    push: true
  ],
  [
    name: 'flakey',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "apps/automation/jenkins-toolbox"
  ],
  [
    name: 'flakeyv2',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "apps/automation/flaky-test-analyzer"
  ],
  [
    name: 'build-analyzer',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "apps/automation/build-analyzer"
  ],
  [
    name: 'rebuild-analyzer',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "apps/automation/rebuild-analyzer"
  ],
  [
    name: 'integrations-test-reporter',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "apps/automation/integrations/reporter"
  ],
  [
    name: 'slack-bridge-hey-apm',
    repo: 'git@github.com:elastic/observability-dev',
    tag: 'latest',
    push: true,
    folder: "tools/report-bridge"
  ],
  [
    name: 'obs-jenkins-heartbeat',
    repo: "git@github.com:elastic/observability-robots.git",
    tag: 'latest',
    push: true,
    prepare_script: '''
      cd apps/beats/heartbeat
      pip3 install pyyaml
      python3 ./generate_heartbeat_configs.py
    ''',
    folder: "apps/beats/heartbeat"
  ],
  [
    name: "bandstand",
    repo: 'git@github.com:elastic/observability-dev',
    tag: "latest",
    folder: "apps/automation/bandstand",
    push: true
  ],
  [
    name: "azure-vm-tools",
    repo: 'git@github.com:elastic/azure-vm-extension',
    tag: "latest",
    folder: ".ci/docker/azure-vm-tools",
    push: true
  ],
  [
    name: 'picklesdoc',
    repo: 'git@github.com:elastic/observability-robots.git',
    tag: 'latest',
    build_script: 'make build',
    push_script: 'make push',
    push: true,
    folder: "apps/pickles"
  ],
  [
    name: 'test-plans',
    repo: 'git@github.com:elastic/observability-robots.git',
    tag: 'latest',
    build_script: 'make build',
    push_script: 'make push',
    push: true,
    folder: "apps/test-plans"
  ],
  /*
    APM ITs Docker images are build daily.
  */
  [
    name: "apm-integration-testing",
    repo: 'git@github.com:elastic/apm-integration-testing.git',
    tag: "daily",
    push: true
  ],
  [
    name: "apm-integration-testing-all",  // Compile all the APM ITs Docker images (using -all suffix to be able to use this automation)
    repo: 'git@github.com:elastic/apm-integration-testing.git',
    build_script: "make -C docker all-tests",
    push_script: "make -C docker all-push",
    push: true
  ],
  [
    name: "oracle-instant-client",
    build_script: """
    docker pull store/oracle/database-instantclient:12.2.0.1;
    docker tag store/oracle/database-instantclient:12.2.0.1 ${registry}/${prefix}/database-instantclient:12.2.0.1;
    docker push ${registry}/${prefix}/database-instantclient:12.2.0.1;
    """
  ],
  [
    name: "weblogic",
    build_script: """
    docker pull store/oracle/weblogic:12.2.1.3-dev;
    docker tag store/oracle/weblogic:12.2.1.3-dev ${registry}/${prefix}/weblogic:12.2.1.3-dev;
    docker push ${registry}/${prefix}/weblogic:12.2.1.3-dev;
    """
  ]
]

/*
  Opbeans Docker images
*/

def opbeansDockerImages = [
  "opbeans-dotnet",
  "opbeans-node",
  "opbeans-python",
  "opbeans-frontend",
  "opbeans-java",
  "opbeans-go",
  "opbeans-loadgen",
  "opbeans-ruby"
  /** FIXME disable until it is fully implemented: git@github.com:elastic/opbeans-flask/pull/5
  "opbeans-flask",*/
]

opbeansDockerImages.each{ name ->
  dockerImages.add([
    name: "${name}",
    repo: "git@github.com:elastic/${name}.git",
    branch_docker: 'main',
    tag: 'daily',
    folder: '.',
    push: true
  ])
}

/*
  APM Pipeline library Docker images
*/
def apmPipelineLibraryDockerImages = [
  "apache-ant",
  "dind-buildx",
  "github-label-sync",
  "gren",
  "shellcheck",
  "yamllint",
  "kibana-yarn",
  "kibana-devmode",
  "vmware-mock"
]

apmPipelineLibraryDockerImages.each{ name ->
  def tag = 'latest'
  def dockerImage = "${registry}/${prefix}/${name}:${tag}"
  dockerImages.add([
    name: "${name}",
    repo: 'git@github.com:elastic/apm-pipeline-library.git',
    branch_docker: 'main',
    tag: "${tag}",
    folder: '.ci/docker',
    build_script: "docker build --force-rm -t ${dockerImage} ${name}",
    push_script: "docker push ${dockerImage}",
    test_script: "make test-${name}",
    push: true
  ])
}

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
    repo: 'git@github.com:elastic/apm-agent-python.git',
    branch_docker: 'main',
    tag: "${version}",
    folder: 'tests',
    build_opts: "--build-arg PYTHON_IMAGE=${pythonVersion}",
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
    repo: 'git@github.com:elastic/apm-agent-nodejs.git',
    branch_docker: 'main',
    tag: "${version}",
    folder: '.ci/docker/node-container',
    build_opts: "--build-arg NODE_VERSION=${nodejsVersion}",
    push: true
  ])
}

/*
  APM Agent Ruby Docker images
*/

dockerImages.add([
  name: "apm-agent-jruby",
  repo: 'git@github.com:elastic/apm-agent-ruby.git',
  folder: '.ci/docker/jruby',
  push: true,
  build_script: "./run.sh --action build --registry ${registry}/${prefix}",
  test_script: "./run.sh --action test --registry ${registry}/${prefix}",
  push_script: "./run.sh --action push --registry ${registry}/${prefix}"
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
    repo: 'git@github.com:elastic/apm-agent-ruby.git',
    branch_docker: 'main',
    tag: "${rubyVersion}",
    folder: 'spec',
    build_opts: "--build-arg RUBY_IMAGE='${version}'",
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
    repo: 'git@github.com:elastic/apm-agent-rum-js.git',
    branch_docker: 'main',
    tag: "${nodejsVersion}",
    folder: ".ci/docker/node-${library}",
    build_opts: "--build-arg NODEJS_VERSION='${nodejsVersion}'",
    push: true
  ])
}

dockerImages.each{ item ->
  pipelineJob("apm-shared/docker-images/${item.job ?: item.name}") {
    displayName("${item.name} ${item.tag ?: ''} - Docker image")
    description("Job to build and push the ${item.name} ${item.tag ?: ''} Docker image")
    parameters {
      stringParam('branch_specifier', "${item.branch ?: 'main'}", "Branch where the Jenkinsfile is.")
      stringParam('branch_docker', "${item.branch_docker ?: 'master'}", "Branch where the Dockerfile is.")
      stringParam('registry', "${registry ?: ''}", "Docker Registry.")
      stringParam('prefix', "${prefix ?: ''}", "Docker registry namespace.")
      stringParam('tag', "${item.tag ?: 'latest'}", "Docker image tag.")
      stringParam('name', "${item.name}", "Docker image name.")
      stringParam('folder', "${item.folder ?: '.'}", "Folder where the Dockrefile is.")
      stringParam('repo', "${item.repo ?: ''}", "Repository where the Docker file is.")
      booleanParam('push', item.push ?: false, "True to push the Docker image to the registry.")
      stringParam('docker_build_opts', "${item.build_opts ?: ''}", "Additional flags to the default docker build command.")
      stringParam('docker_build_script', "${item.build_script ?: ''}", "Script/command to build the Docker image.")
      stringParam('docker_test_script', "${item.test_script ?: ''}", "Script/command to test the Docker image.")
      stringParam('docker_push_script', "${item.push_script ?: ''}", "Script/command to push the Docker image.")
      stringParam('prepare_script', "${item.prepare_script ?: ''}", "Script/command to run before everything.")
    }
    disabled(false)
    quietPeriod(10)
    logRotator {
      numToKeep(10)
      daysToKeep(7)
      artifactNumToKeep(10)
      artifactDaysToKeep(-1)
    }
    properties {
      disableResume()
      durabilityHint{
        hint('PERFORMANCE_OPTIMIZED')
      }
      pipelineTriggers {
        triggers {
          cron{
              spec('H H(3-4) * * 1-5')
            }
        }
      }
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
