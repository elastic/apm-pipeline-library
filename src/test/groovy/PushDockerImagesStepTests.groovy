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

class PushDockerImagesStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/pushDockerImages.groovy')
    env.GIT_BASE_COMMIT = 'commit'
  }

  @Test
  void test_windows() throws Exception {
    testWindows() {
      script.call()
    }
  }

  @Test
  void test_missing_registry() throws Exception {
    testMissingArgument('registry') {
      script.call()
    }
  }

  @Test
  void test_missing_secret() throws Exception {
    testMissingArgument('secret') {
      script.call(registry: 'foo')
    }
  }
  @Test
  void test_missing_targetNamespace() throws Exception {
    testMissingArgument('targetNamespace') {
      script.call(registry: 'foo', secret: 'bar')
    }
  }

  @Test
  void test_missing_version() throws Exception {
    testMissingArgument('version') {
      script.call(registry: 'foo', secret: 'bar', targetNamespace: 'target')
    }
  }

  @Test
  void test_missing_project() throws Exception {
    testMissingArgument('project.name') {
      script.call(registry: 'foo', secret: 'bar', targetNamespace: 'target', version: '1.2')
    }
  }

  @Test
  void test_calculateTags_pr() throws Exception {
    helper.registerAllowedMethod('isPR', { return true })
    env.CHANGE_ID = '1'
    def ret = script.calculateTags('8.2-SNAPSHOT', '')
    printCallStack()
    assertTrue(ret.equals(['commit', 'pr-1']))
  }

  @Test
  void test_calculateTags_branch() throws Exception {
    helper.registerAllowedMethod('isPR', { return false })
    def ret = script.calculateTags('8.2-SNAPSHOT', '8.2.0-SNAPSHOT')
    printCallStack()
    assertTrue(ret.equals(['commit', '8.2-SNAPSHOT', '8.2.0-SNAPSHOT']))

    ret = script.calculateTags('8.2-SNAPSHOT', '')
    printCallStack()
    assertTrue(ret.equals(['commit', '8.2-SNAPSHOT']))
  }

  @Test
  void test_doTagAndPush() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { return 0 })
    script.doTagAndPush(registry: 'my-registry',
                        name: 'my-name',
                        variant: '-cloud',
                        sourceTag: '8.2-SNAPSHOT',
                        targetTag: 'commit',
                        sourceNamespace: 'beats',
                        targetNamespace: 'beats-ci')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', '"my-registry/beats/my-name-cloud:8.2-SNAPSHOT" "my-registry/beats-ci/my-name-cloud:commit"'))
  }

  @Test
  void test_with_snapshots() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { return 0 })
    script.call(
      secret: "my-secret",
      registry: "my-registry",
      arch: 'amd64',
      version: '8.2.0',
      snapshot: true,
      project: [
        name: 'filebeat',
        variants: [
          ''          : 'beats',
          '-cloud'    : 'beats-ci'
        ]
      ],
      targetNamespace: 'observability-ci'
    )
    printCallStack()
    assertTrue(assertMethodCallOccurrences('sh', 12))
  }

  @Test
  void test_without_snapshots() throws Exception {
    helper.registerAllowedMethod('sh', [Map.class], { return 0 })
    script.call(
      secret: "my-secret",
      registry: "my-registry",
      arch: 'amd64',
      version: '8.2.0',
      snapshot: false,
      project: [
        name: 'filebeat',
        variants: [
          ''          : 'beats',
          '-cloud'    : 'beats-ci'
        ]
      ],
      targetNamespace: 'observability-ci'
    )
    printCallStack()
    assertTrue(assertMethodCallOccurrences('sh', 8))
  }
}
