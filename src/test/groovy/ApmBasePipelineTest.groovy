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

import com.lesfurets.jenkins.unit.BasePipelineTest
import co.elastic.mock.DockerMock
import co.elastic.mock.GetVaultSecretMock
import co.elastic.mock.PullRequestMock
import co.elastic.mock.StepsMock
import co.elastic.mock.WithSecretVaultMock
import co.elastic.TestUtils

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString

class ApmBasePipelineTest extends BasePipelineTest {
  Map env = [:]

  String SHA = '29480a51'
  String REPO_URL = 'http://github.com/org/repo.git'
  String EXAMPLE_URL = 'https://ec.example.com:9200'

  @Override
  void setUp() {
    super.setUp()

    env.BRANCH_NAME = 'master'
    env.BUILD_ID = '1'
    env.JENKINS_URL = 'http://jenkins.example.com:8080/'
    env.BUILD_URL = "${env.JENKINS_URL}job/folder/job/mpb/job/${env.BRANCH_NAME}/${env.BUILD_ID}/"
    env.JOB_BASE_NAME = 'master'
    env.JOB_NAME = "folder/mbp/${env.JOB_BASE_NAME}"
    env.RUN_DISPLAY_URL = "${env.JENKINS_URL}job/folder/job/mbp/job/${env.JOB_BASE_NAME}/${env.BUILD_ID}/display/redirect"
    env.WORKSPACE = 'WS'

    registerDeclarativeMethods()
    registerScriptedMethods()
    registerSharedLibraryMethods()

    binding.setVariable('env', env)
    binding.setProperty('docker', new DockerMock())
    binding.setProperty('getVaultSecret', new GetVaultSecretMock())
    binding.setProperty('pullRequest', new PullRequestMock())
    binding.setProperty('steps', new StepsMock())
    binding.setProperty('withSecretVault', new WithSecretVaultMock())
  }

  void registerDeclarativeMethods() {
    helper.registerAllowedMethod('aborted', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('always', [Closure.class], null)
    helper.registerAllowedMethod('ansiColor', [String.class], null)
    helper.registerAllowedMethod('agent', [Closure.class], null)
    helper.registerAllowedMethod('beforeAgent', [Boolean.class], { true })
    helper.registerAllowedMethod('disableResume', [], null)
    helper.registerAllowedMethod('durabilityHint', [String.class], null)
    helper.registerAllowedMethod('failure', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('failFast', [Boolean.class], null)
    helper.registerAllowedMethod('issueCommentTrigger', [String.class], null)
    helper.registerAllowedMethod('label', [String.class], null)
    helper.registerAllowedMethod('options', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('pipeline', [Closure.class], null)
    helper.registerAllowedMethod('post', [Closure.class], null)
    helper.registerAllowedMethod('quietPeriod', [Integer.class], null)
    helper.registerAllowedMethod('rateLimitBuilds', [Map.class], null)
    helper.registerAllowedMethod('script', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('skipDefaultCheckout', [], null)
    helper.registerAllowedMethod('stage', [Closure.class], null)
    helper.registerAllowedMethod('stage', [String.class, Closure.class], { stageName, body ->
      def stageResult
      helper.registerAllowedMethod('when', [Closure.class], { Closure bodyWhen ->
        helper.registerAllowedMethod('branch', [String.class], { branchName  ->
          if(branchName == env.BRANCH_NAME) {
            return true
          }
          throw new RuntimeException("Stage \"${stageName}\" skipped due to when conditional")
        })
        helper.registerAllowedMethod('tag', [String.class], { tagName  ->
          // Default comparator = EQUALS in this particular implementation
          if(tagName == env.BRANCH_NAME) {
            return true
          }
          throw new RuntimeException("Stage \"${stageName}\" skipped due to when conditional")
        })
        helper.registerAllowedMethod('tag', [Map.class], { m  ->
          if (m.comparator.equals('REGEXP')) {
            if (env.BRANCH_NAME ==~ m.pattern) {
              return true
            }
          }
          throw new RuntimeException("Stage \"${stageName}\" skipped due to when conditional")
        })
        helper.registerAllowedMethod('allOf', [Closure.class], { Closure cAllOf ->
          helper.registerAllowedMethod('branch', [String.class], { branchName  ->
            if(branchName == env.BRANCH_NAME) {
              return true
            }
            throw new RuntimeException("Stage \"${stageName}\" skipped due to when conditional (branch)")
          })
          helper.registerAllowedMethod('expression', [Closure.class], { Closure cExp ->
            if(cExp()) {
              return true
            }
            throw new RuntimeException("Stage '${stageName}' skipped due to when conditional (expression)")
          })
          return cAllOf()
        })
        helper.registerAllowedMethod('anyOf', [Closure.class], { Closure cAnyOf ->
          def result = false
          helper.registerAllowedMethod('branch', [String.class], { branchName  ->
            if(branchName == env.BRANCH_NAME) {
              result = true
              return result
            }
          })
          helper.registerAllowedMethod('tag', [Map.class], { m  ->
            if (m.comparator.equals('REGEXP')) {
              if (env.BRANCH_NAME ==~ m.pattern) {
                result = true
                return result
              }
            }
            if (!result) {
              throw new RuntimeException("Stage \"${stageName}\" skipped due to when conditional (branch)")
            }
          })
          return cAnyOf()
        })
        return bodyWhen()
      })

      switch (currentBuild.result) {
        case 'FAILURE':
          break
        default:
          try {
            stageResult = body()
          }
          catch (RuntimeException re) {
            // skip stage due to when conditional
          }
          catch (Exception e) {
            throw e
          }
      }
      return stageResult
    })
    helper.registerAllowedMethod('stages', [Closure.class], null)
    helper.registerAllowedMethod('stash', [Map.class], null)
    helper.registerAllowedMethod('steps', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('success', [Closure.class], { body -> body() })
    helper.registerAllowedMethod('timeout', [Map.class], null)
    helper.registerAllowedMethod('timestamps', [], null)
    helper.registerAllowedMethod('triggers', [Closure.class], null)
    helper.registerAllowedMethod('unstable', [Closure.class], { body -> body() })
  }

  void registerScriptedMethods() {
    helper.registerAllowedMethod('archive', [String.class], null)
    helper.registerAllowedMethod('bat', [String.class], null)
    helper.registerAllowedMethod('booleanParam', [Map.class], null)
    helper.registerAllowedMethod('brokenTestsSuspects', { "OK" })
    helper.registerAllowedMethod('brokenBuildSuspects', { "OK" })
    helper.registerAllowedMethod('build', [Map.class], null)
    helper.registerAllowedMethod('catchError', [Closure.class], { c ->
      try{
        c()
      } catch(e){
        //NOOP
      }
    })
    helper.registerAllowedMethod('catchError', [Map.class, Closure.class], { m, c ->
      try{
        c()
      } catch(e){
        //NOOP
      }
    })
    helper.registerAllowedMethod('checkout', [String.class], null)
    helper.registerAllowedMethod('credentials', [String.class], { s -> s })
    helper.registerAllowedMethod('deleteDir', [], null)
    helper.registerAllowedMethod('dir', [String.class, Closure.class], { i, c ->
      c.call()
    })
    helper.registerAllowedMethod('emailext', [Map.class], { println("sending email") })
    helper.registerAllowedMethod('environment', [Closure.class], { Closure c ->
      def envBefore = [env: binding.getVariable('env')]
      println "Env section - original env vars: ${envBefore.toString()}"
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c.delegate = envBefore
      c()

      def envNew = envBefore.env
      envBefore.each { k, v ->
        if (k != 'env') {
            envNew["$k"] = v
        }

      }
      println "Env section - env vars set to: ${envNew.toString()}"
      binding.setVariable('env', envNew)
    })
    helper.registerAllowedMethod('error', [String.class], { s ->
      updateBuildStatus('FAILURE')
      throw new Exception(s)
    })
    helper.registerAllowedMethod('fileExists', [String.class], { true })
    helper.registerAllowedMethod('githubNotify', [Map.class], { m ->
      if(m.context.equalsIgnoreCase('failed')){
        updateBuildStatus('FAILURE')
        throw new Exception('Failed')
      }
    })
    helper.registerAllowedMethod('isUnix', [ ], { true })
    helper.registerAllowedMethod('junit', [Map.class], null)
    helper.registerAllowedMethod('mail', [Map.class], { m ->
      println('Writting mail-out.html file with the email result')
      def f = new File("target/mail-out-${env.TEST}.html")
      f.write(m.body)
      println f.toString()
    })
    helper.registerAllowedMethod('readFile', [Map.class], { '' })
    helper.registerAllowedMethod('readJSON', [Map.class], { m ->
      return readJSON(m)
    })
    helper.registerAllowedMethod('retry', [Integer.class, Closure.class], { count, c ->
      Exception lastError = null
      while (count-- > 0) {
        try {
          c.call()
          lastError = null
          break
        } catch (error) {
          lastError = error
        }
      }
      if (lastError) {
        throw lastError
      }
    })
    helper.registerAllowedMethod('sleep', [Integer.class], null)
    helper.registerAllowedMethod('sh', [Map.class], { 'OK' })
    helper.registerAllowedMethod('sh', [String.class], { 'OK' })
    helper.registerAllowedMethod('sshagent', [List.class, Closure.class], { m, body -> body() })
    helper.registerAllowedMethod('string', [Map.class], { m -> return m })
    helper.registerAllowedMethod('timeout', [Integer.class, Closure.class], null)
    helper.registerAllowedMethod('unstash', [String.class], null)
    helper.registerAllowedMethod('upstreamDevelopers', { "OK" })
    helper.registerAllowedMethod('usernamePassword', [Map.class], { m ->
      m.each{ k, v ->
        binding.setVariable("${v}", 'defined')
      }
    })
    helper.registerAllowedMethod('withEnv', [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod('wrap', [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod('writeFile', [Map.class], { m ->
      (new File("target/${m.file}")).withWriter('UTF-8') { writer ->
        writer.write(m.text)
      }
    })
    helper.registerAllowedMethod('writeJSON', [Map.class], { "OK" })
  }

  void registerSharedLibraryMethods() {
    helper.registerAllowedMethod('base64encode', [Map.class], { return "YWRtaW46YWRtaW4xMjMK" })
    helper.registerAllowedMethod('cancelPreviousRunningBuilds', [Map.class], null)
    helper.registerAllowedMethod('cobertura', [Map.class], null)
    helper.registerAllowedMethod('dockerLogin', [Map.class], { true })
    helper.registerAllowedMethod('echoColor', [Map.class], { m ->
      def echoColor = loadScript('vars/echoColor.groovy')
      echoColor.call(m)
    })
    helper.registerAllowedMethod('getBlueoceanDisplayURL', [], { "${env.JENKINS_URL}blue/organizations/jenkins/folder%2Fmbp/detail/${env.BRANCH_NAME}/${env.BUILD_ID}/" })
    helper.registerAllowedMethod('getBlueoceanTabURL', [String.class], { "${env.JENKINS_URL}blue/organizations/jenkins/folder%2Fmbp/detail/${env.BRANCH_NAME}/${env.BUILD_ID}/tests" })
    helper.registerAllowedMethod('getBuildInfoJsonFiles', [String.class,String.class], { "OK" })
    helper.registerAllowedMethod('getGitCommitSha', [], {return SHA})
    helper.registerAllowedMethod('getGithubToken', {return 'TOKEN'})
    helper.registerAllowedMethod('getGitRepoURL', [], {return REPO_URL})
    helper.registerAllowedMethod('getTraditionalPageURL', [String.class], { "${env.JENKINS_URL}job/folder-mbp/job/${env.BRANCH_NAME}/${env.BUILD_ID}/testReport" })
    helper.registerAllowedMethod('getVaultSecret', [Map.class], { m ->
      getVaultSecret(m.secret)
    })
    helper.registerAllowedMethod('getVaultSecret', [String.class], { s ->
      getVaultSecret(s)
    })
    helper.registerAllowedMethod('gitCheckout', [Map.class], null)
    helper.registerAllowedMethod('gitCmd', [Map.class], null)
    helper.registerAllowedMethod('githubApiCall', [Map.class], {
      return [[login: 'foo'], [login: 'bar'], [login: 'elastic']]
    })
    helper.registerAllowedMethod('githubBranchRef', [], {return 'master'})
    helper.registerAllowedMethod('githubEnv', [], null)
    helper.registerAllowedMethod('githubPrCheckApproved', [], { return true })
    helper.registerAllowedMethod("githubPrInfo", [Map.class], {
      return [title: 'dummy PR', user: [login: 'username'], author_association: 'NONE']
    })
    helper.registerAllowedMethod('gitPush', [Map.class], { return "OK" })
    helper.registerAllowedMethod('httpRequest', [Map.class], { true })
    helper.registerAllowedMethod('isCommentTrigger', { return false })
    helper.registerAllowedMethod('isUpstreamTrigger', { return false })
    helper.registerAllowedMethod('isUserTrigger', { return false })
    helper.registerAllowedMethod('log', [Map.class], {m -> println m.text})
    helper.registerAllowedMethod('notifyBuildResult', [], null)
    helper.registerAllowedMethod('preCommitToJunit', [Map.class], null)
    helper.registerAllowedMethod('publishHTML', [Map.class],  null)
    helper.registerAllowedMethod('randomNumber', [Map.class], { m -> return m.min })
    helper.registerAllowedMethod('rebuildPipeline', [], { true })
    helper.registerAllowedMethod('sendDataToElasticsearch', [Map.class], { "OK" })
    helper.registerAllowedMethod('toJSON', [Map.class], { m ->
      def script = loadScript('vars/toJSON.groovy')
      return script.call(m)
    })
    helper.registerAllowedMethod('toJSON', [String.class], { s ->
      def script = loadScript('vars/toJSON.groovy')
      return script.call(s)
    })
    helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], TestUtils.withCredentialsInterceptor)
    helper.registerAllowedMethod('withEnvWrapper', [Closure.class], { closure -> closure.call() })
    helper.registerAllowedMethod('withGithubNotify', [Map.class, Closure.class], null)
  }

  def getVaultSecret(String s) {
    if('secret'.equals(s) || 'java-agent-benchmark-cloud'.equals(s) ||
       'secret/team/ci/secret-name'.equals(s) || 'secret/apm-team/ci/benchmark-cloud'.equals(s)){
      return [data: [ user: 'username', password: 'user_password', url: "${EXAMPLE_URL}", apiKey: 'my-api-key']]
    }
    if('secretError'.equals(s)){
      return [errors: 'Error message']
    }
    if('secretNotValid'.equals(s)){
      return [data: [ user: null, password: null, url: null, apiKey: null]]
    }
    if('secret-codecov'.equals(s) || 'repo-codecov'.equals(s)){
      return [data: [ value: 'codecov-token']]
    }
    return null
  }

  def readJSON(params){
    def jsonSlurper = new groovy.json.JsonSlurperClassic()
    def jsonText = params.text
    if(params.file){
      File f = new File("src/test/resources/${params.file}")
      jsonText = f.getText()
    }
    return jsonSlurper.parseText(jsonText)
  }

  // Asserts helpers
  def assertMethodCallContainsPattern(String methodName, String pattern) {
    return helper.callStack.findAll { call ->
      call.methodName == methodName
    }.any { call ->
      callArgsToString(call).contains(pattern)
    }
  }

  def assertMethodCall(String methodName) {
    return helper.callStack.find { call ->
      call.methodName == methodName
    }
  }

  def assertMethodCallOccurrences(String methodName, int compare) {
    return helper.callStack.findAll { call ->
      call.methodName == methodName
    }.size() == compare
  }
}
