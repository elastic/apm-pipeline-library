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

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GitCheckoutStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/gitCheckout.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.WORKSPACE = 'WS'
    env.remove('BRANCH_NAME')
    binding.getVariable('currentBuild').getBuildCauses = {
      return null
    }
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Checkout SCM ${env.BRANCH_NAME}"))
    assertJobStatusSuccess()
  }

  @Test
  void testBaseDir() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "Checkout SCM ${env.BRANCH_NAME}"))
    assertJobStatusSuccess()
  }

  @Test
  void testBranch() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Checkout master'))
    assertTrue(assertMethodCallContainsPattern('log', 'Reference repo disabled'))
    assertTrue(assertMethodCallContainsPattern('checkout', 'reference=,'))
    assertJobStatusSuccess()
  }

  @Test
  void testReferenceRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      reference: 'repo')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Checkout master'))
    assertTrue(assertMethodCallContainsPattern('log', 'Reference repo enabled'))
    assertTrue(assertMethodCallContainsPattern('checkout', 'CloneOption, depth=5, noTags=false, reference=repo, shallow=true'))
    assertJobStatusSuccess()
  }

  @Test
  void testMergeRemoteRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      mergeRemote: 'upstream',
      mergeTarget: 'master',
      shallow: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'options:[mergeTarget:master, mergeRemote:upstream]]]'))
    assertJobStatusSuccess()
  }

  @Test
  void testMergeTargetRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      mergeTarget: "master",
      shallow: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'options:[mergeTarget:master, mergeRemote:origin]]]'))
    assertJobStatusSuccess()
  }

  @Test
  void testRepo_without_GIT_URL() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(repo: 'git@github.com:elastic/apm-pipeline-library.git', branch: 'master',
                credentialsId: 'credentials-id')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Override GIT_URL with the params.rep'))
    assertJobStatusSuccess()
  }

  @Test
  void testRepo_with_GIT_URL() throws Exception {
    def script = loadScript(scriptName)
    env.GIT_URL = 'git@github.com:elastic/apm-pipeline-library.git'
    script.scm = 'SCM'
    script.call(repo: env.GIT_URL, branch: 'master', credentialsId: 'credentials-id')
    printCallStack()
    assertFalse(assertMethodCallContainsPattern('log', 'Override GIT_URL with the params.rep'))
    assertJobStatusSuccess()
  }

  @Test
  void testErrorBranchIncomplete() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master')
    } catch(e){
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=null or credentialsId=null'))
  }

  @Test
  void testErrorBranchAndBranchNameVariable() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Please use the checkout either with the env.BRANCH_NAME or the gitCheckout'))
    assertJobStatusFailure()
  }

  @Test
  void testErrorNoBranchAndNoBranchNameVariable() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Please double check the environment variable env.BRANCH_NAME=null'))
    assertJobStatusFailure()
  }

  @Test
  void testErrorBranchNoCredentials() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git')
    } catch(e){
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=git@github.com:elastic/apm-pipeline-library.git or credentialsId=null'))
    assertJobStatusFailure()
  }

  @Test
  void testErrorBranchNoRepo() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master',
                  credentialsId: 'credentials-id')
    } catch(e){
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=null or credentialsId=credentials-id'))
    assertJobStatusFailure()
  }

  @Test
  void testErrorEmptyBranch() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: '', credentialsId: 'credentials-id',
                  repo: 'git@github.com:elastic/apm-pipeline-library.git')
    } catch(e){
      // NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=, repo=git@github.com:elastic/apm-pipeline-library.git or credentialsId=credentials-id'))
    assertJobStatusFailure()
  }

  @Test
  void testUserTriggered() throws Exception {
    helper.registerAllowedMethod("isUserTrigger", {return true})
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master',
                  credentialsId: 'credentials-id')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=null or credentialsId=credentials-id'))
    assertJobStatusFailure()
  }

  @Test
  void testCommentTriggered() throws Exception {
    helper.registerAllowedMethod("isCommentTrigger", {return true})
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master',
                  credentialsId: 'credentials-id')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=null or credentialsId=credentials-id'))
    assertJobStatusFailure()
  }

  @Test
  void testUpstreamTriggered() throws Exception {
    helper.registerAllowedMethod('isUpstreamTrigger', {return true})
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    try {
      script.call(basedir: 'sub-folder', branch: 'master',
                  credentialsId: 'credentials-id')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'branch=master, repo=null or credentialsId=credentials-id'))
    assertJobStatusFailure()
  }

  @Test
  void testFirstTimeContributorWithoutNotify() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubPrCheckApproved', [], {
      updateBuildStatus('FAILURE')
      throw new Exception('githubPrCheckApproved: The PR is not approved yet')
    })
    script.scm = 'SCM'
    try{
      script.call(basedir: 'sub-folder', branch: 'master',
        repo: 'git@github.com:elastic/apm-pipeline-library.git',
        credentialsId: 'credentials-id',
        reference: 'repo')
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void testFirstTimeContributorWithNotify() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('githubPrCheckApproved', [], {
      updateBuildStatus('FAILURE')
      throw new Exception('githubPrCheckApproved: The PR is not approved yet')
    })
    env.BRANCH_NAME = 'master'
    script.scm = 'SCM'
    try{
      script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: true)
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=CI-approved contributor, description=It requires manual inspection, status=FAILURE'))
    assertJobStatusFailure()
  }

  @Test
  void testWithoutFirstTimeContributorWithNotify() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=CI-approved contributor, status=SUCCESS'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithFirstTimeContributorWithoutNotify() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: false)
    printCallStack()
    assertTrue(assertMethodCallOccurrences('githubNotify', 0))
    assertJobStatusSuccess()
  }

  @Test
  void testWithFirstTimeContributorWithNotifyAndCommentTrigger() throws Exception {
    helper.registerAllowedMethod("isCommentTrigger", {return true})
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'BRANCH'
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', githubNotifyFirstTimeContributor: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('githubNotify', 'context=CI-approved contributor, status=SUCCESS'))
    assertJobStatusSuccess()
  }

  @Test
  void testWithShallowAndMergeTarget() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
        repo: 'git@github.com:elastic/apm-pipeline-library.git',
        credentialsId: 'credentials-id',
        mergeTarget: 'master',
        shallow: true)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'refusing to merge unrelated histories'))
    assertTrue(assertMethodCallContainsPattern('checkout', 'CloneOption, depth=0, noTags=false, reference=, shallow=false'))
    assertJobStatusSuccess()
  }

  @Test
  void testDisabledShallowAndDepth() throws Exception {
    def script = loadScript(scriptName)
    script.scm = 'SCM'
    script.call(basedir: 'sub-folder', branch: 'master',
      repo: 'git@github.com:elastic/apm-pipeline-library.git',
      credentialsId: 'credentials-id',
      shallow: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('checkout', 'CloneOption, depth=0, noTags=false, reference=, shallow=false'))
    assertJobStatusSuccess()
  }

  @Test
  void testIsDefaultSCM() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.isDefaultSCM(null))
    env.BRANCH_NAME = 'master'
    assertTrue(script.isDefaultSCM(null))
    assertFalse(script.isDefaultSCM('foo'))
    assertJobStatusSuccess()
  }

  @Test
  void testIsDefaultSCMWithCustomisation() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.scm = [ extensions: [] ]
    script.call(repo: 'git@github.com:elastic/apm-pipeline-library.git', shallow: false)
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Checkout SCM master with some customisation'))
    assertJobStatusSuccess()
  }

  @Test
  void testIsDefaultSCMWithoutCustomisation() throws Exception {
    def script = loadScript(scriptName)
    env.BRANCH_NAME = 'master'
    script.scm = [ extensions: [] ]
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', 'Checkout SCM master with default customisation from the Item'))
    assertJobStatusSuccess()
  }
}
