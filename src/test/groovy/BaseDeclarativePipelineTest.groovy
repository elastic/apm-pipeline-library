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
import co.elastic.mock.GetVaultSecretMock
import co.elastic.TestUtils

class BaseDeclarativePipelineTest extends BasePipelineTest {

  Map env = [:]

  @Override
  void setUp() {
    super.setUp()

    registerDeclarativeMethods()
    registerScriptedMethods()
    registerSharedLibraryMethods()

    binding.setVariable('env', env)
    binding.setProperty('getVaultSecret', new GetVaultSecretMock())
  }

  void registerDeclarativeMethods() {
    helper.registerAllowedMethod('always', [Closure.class], null)
    helper.registerAllowedMethod('ansiColor', [String.class], null)
    helper.registerAllowedMethod('agent', [Closure.class], null)
    helper.registerAllowedMethod('beforeAgent', [Boolean.class], { true })
    helper.registerAllowedMethod('disableResume', [], null)
    helper.registerAllowedMethod('durabilityHint', [String.class], null)
    helper.registerAllowedMethod('issueCommentTrigger', [String.class], null)
    helper.registerAllowedMethod('label', [String.class], null)
    helper.registerAllowedMethod('options', [Closure.class], null)
    helper.registerAllowedMethod('pipeline', [Closure.class], null)
    helper.registerAllowedMethod('post', [Closure.class], null)
    helper.registerAllowedMethod('quietPeriod', [Integer.class], null)
    helper.registerAllowedMethod('rateLimitBuilds', [Map.class], null)
    helper.registerAllowedMethod('stage', [Closure.class], null)
    helper.registerAllowedMethod('stage', [String.class, Closure.class], {stageName, body ->
      def stageResult
      helper.registerAllowedMethod('when', [Closure.class], {
        helper.registerAllowedMethod('branch', [String.class], {branchName  ->
          if(branchName == env.BRANCH_NAME) {
            return true
          }
          throw new RuntimeException("Stage '${stageName}' skipped due to when expression returned false")
        })
      })
      switch (currentBuild.result) {
        case 'FAILURE':
          break
        default:
          try {
            stageResult = body()
            stagesExecuted.add(stageName)
          }
          catch (RuntimeException re) { }
          catch (Exception e) { throw e }
      }
      return stageResult
    })
    helper.registerAllowedMethod('stages', [Closure.class], null)
    helper.registerAllowedMethod('stash', [Map.class], null)
    helper.registerAllowedMethod('steps', [Closure.class], null)
    helper.registerAllowedMethod('timeout', [Map.class], null)
    helper.registerAllowedMethod('timestamps', [], null)
    helper.registerAllowedMethod('triggers', [Closure.class], null)
  }

  void registerScriptedMethods() {
    helper.registerAllowedMethod('bat', [String.class], null)
    helper.registerAllowedMethod('credentials', [String.class], { s -> s })
    helper.registerAllowedMethod('deleteDir', [], null)
    helper.registerAllowedMethod('dir', [String.class, Closure.class], { i, c ->
      c.call()
    })
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
    helper.registerAllowedMethod('isUnix', [ ], { true })
    helper.registerAllowedMethod('junit', [Map.class], null)
    helper.registerAllowedMethod('retry', [Integer.class, Closure.class], { i, c ->
      c.call()
    })
    helper.registerAllowedMethod('sleep', [Integer.class], null)
    helper.registerAllowedMethod('sh', [String.class], null)
    helper.registerAllowedMethod('timeout', [Integer.class, Closure.class], null)
    helper.registerAllowedMethod('unstash', [String.class], null)
    helper.registerAllowedMethod('withEnv', [List.class, Closure.class], TestUtils.withEnvInterceptor)
    helper.registerAllowedMethod('wrap', [Map.class, Closure.class], TestUtils.wrapInterceptor)
    helper.registerAllowedMethod('writeFile', [Map.class], { m ->
      (new File("target/${m.file}")).withWriter('UTF-8') { writer ->
        writer.write(m.text)
      }
    })
  }

  void registerSharedLibraryMethods() {
    helper.registerAllowedMethod('dockerLogin', [Map.class], { true })
    helper.registerAllowedMethod('getVaultSecret', [Map.class], { m ->
      def s = m.secret
      if('secret'.equals(s)){
        return [data: [ user: 'username', password: 'user_password']]
      }
      if('secretError'.equals(s)){
        return [errors: 'Error message']
      }
      if('secretNotValid'.equals(s)){
        return [data: [ user: null, password: null]]
      }
      return null
    })
    helper.registerAllowedMethod('gitCheckout', [Map.class], null)
    helper.registerAllowedMethod('log', [Map.class], {m -> println m.text})
    helper.registerAllowedMethod('notifyBuildResult', [], null)
    helper.registerAllowedMethod('randomNumber', [Map.class], { m -> return m.min })
    helper.registerAllowedMethod('withGithubNotify', [Map.class, Closure.class], null)
  }
}
