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
import static org.junit.Assert.assertFalse

class BuildKibanaDockerImageStepTests extends ApmBasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        script = loadScript('vars/buildKibanaDockerImage.groovy')
        env.BASE_DIR = 'base_dir'
    }

    @Test
    void test_with_baseDir() throws Exception {
        def result = script.call(baseDir: 'foo', packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: main'))
        assertTrue(assertMethodCallContainsPattern('log', 'Cloning Kibana repository, refspec main, into foo'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:main"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:main was pushed"))
        assertTrue(env.DEPLOY_NAME == 'main')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-main')
        assertJobStatusSuccess()
    }

    @Test
    void test_without_refspec() throws Exception {
        def result = script.call(packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: main'))
        assertTrue(assertMethodCallContainsPattern('log', 'Cloning Kibana repository, refspec main, into base_dir/build'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:main"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:main was pushed"))
        assertTrue(env.DEPLOY_NAME == 'main')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-main')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_branch_refspec() throws Exception {
        def result = script.call(refspec: 'foo', packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: foo'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:foo"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:foo was pushed"))
        assertTrue(env.DEPLOY_NAME == 'foo')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-foo')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_uppercase() throws Exception {
        def result = script.call(refspec: 'PR/111111', packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/111111'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:pr111111"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:pr111111 was pushed"))
        assertTrue(env.DEPLOY_NAME == 'pr111111')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-pr111111')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_lowercase() throws Exception {
        def result = script.call(refspec: 'pr/222222', packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/222222'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:pr222222"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:pr222222 was pushed"))
        assertTrue(env.DEPLOY_NAME == 'pr222222')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-pr222222')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_no_case() throws Exception {
        def result = script.call(refspec: 'pR/333333', packageJSON: 'buildKibana/package.json')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/333333'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:pr333333"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:pr333333 was pushed"))
        assertTrue(env.DEPLOY_NAME == 'pr333333')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-pr333333')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_custom_registry() throws Exception {
        def registry = 'hub.docker.com'
        def result = script.call(refspec: 'pr/444444', packageJSON: 'buildKibana/package.json', dockerRegistry: registry)
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/444444'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging ${registry}/kibana/kibana:8.0.0-SNAPSHOT to ${registry}/observability-ci/kibana:${SHA}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging ${registry}/kibana/kibana:8.0.0-SNAPSHOT to ${registry}/observability-ci/kibana:pr444444"))
        assertTrue(assertMethodCallContainsPattern('log', "${registry}/observability-ci/kibana:${SHA} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "${registry}/observability-ci/kibana:pr444444 was pushed"))
        assertTrue(env.DEPLOY_NAME == 'pr444444')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-pr444444')
        assertJobStatusSuccess()
    }

    @Test
    void test_with_custom_values() throws Exception {
        def registry = 'hub.docker.com'
        def targetTag = 'ABCDEFG'
        def src = 'the_source'
        def target = 'the_target'
        def result = script.call(refspec: 'pr/555555', packageJSON: 'buildKibana/package.json', targetTag: targetTag, dockerRegistry: registry, dockerImageSource: src, dockerImageTarget: target)
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/555555'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging ${src}:8.0.0-SNAPSHOT to ${target}:${targetTag}"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging ${src}:8.0.0-SNAPSHOT to ${target}:pr55555"))
        assertTrue(assertMethodCallContainsPattern('log', "${target}:${targetTag} was pushed"))
        assertTrue(assertMethodCallContainsPattern('log', "${target}:pr555555 was pushed"))
        assertTrue(env.DEPLOY_NAME == 'pr555555')
        assertTrue(env.KIBANA_DOCKER_TAG == '8.0.0-SNAPSHOT-pr555555')
        assertJobStatusSuccess()
    }

    @Test
    void test_skip_ubuntu() throws Exception {
        def result = script.call(refspec: 'pr/555555', skipUbuntu: true,  packageJSON: 'buildKibana/package.json')
        assertTrue(env.BUILD_DOCKER_UBUNTU == '')
        assertTrue(env.BUILD_DOCKER_CLOUD == '1')
        assertFalse(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT"))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana-ci/kibana-cloud:8.0.0-SNAPSHOT"))
        assertJobStatusSuccess()
    }

    @Test
    void test_skip_cloud() throws Exception {
        def result = script.call(refspec: 'pr/555555', skipCloud: true, packageJSON: 'buildKibana/package.json')
        assertTrue(env.BUILD_DOCKER_UBUNTU == '1')
        assertTrue(env.BUILD_DOCKER_CLOUD == '')
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT"))
        assertFalse(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana-ci/kibana-cloud:8.0.0-SNAPSHOT"))
        assertJobStatusSuccess()
    }
}
