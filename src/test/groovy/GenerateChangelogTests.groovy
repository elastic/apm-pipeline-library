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
import static org.junit.Assert.assertTrue

class GenerateChangelogTests extends ApmBasePipelineTest {
  String scriptName = 'vars/generateChangelog.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    env.REPO_NAME = "apm-pipeline-library"
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "docker run --name tmp_changelog_instance"))
    assertTrue(assertMethodCallContainsPattern('sh', "-v null:/usr/local/src/your-app ferrarimarco/github-changelog-generator"))
    assertTrue(assertMethodCallContainsPattern('sh', "-v null:/usr/local/src/your-app ferrarimarco/github-changelog-generator"))
    assertTrue(assertMethodCallContainsPattern('sh', "--project apm-pipeline-library"))
    assertTrue(assertMethodCallContainsPattern('sh', "--user elastic"))
    assertTrue(assertMethodCallContainsPattern('sh', "--token TOKEN, returnStdout=false"))
    assertJobStatusSuccess()
  }

  @Test
  void testNoRepoSetFails() throws Exception {
    def script = loadScript(scriptName)
    try{
      script.call()
    } catch(e) {
      // NOOP because we are asserting against the error below this
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', "Must provide `repo` argument to this step or set \$REPO_NAME in the environment"))
    assertJobStatusFailure()
  }
}
