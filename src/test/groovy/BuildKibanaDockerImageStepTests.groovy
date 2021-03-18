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

class BuildKibanaDockerImageStepTests extends ApmBasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        script = loadScript('vars/buildKibanaDockerImage.groovy')

        env.BASE_DIR = "buildKibana"
    }

    @Test
    void test_without_refspec() throws Exception {
        def result = script.call()
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: master'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:master"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:master were pushed"))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_branch_refspec() throws Exception {
        def result = script.call(refspec: 'foo')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: foo'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:foo"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:foo were pushed"))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_uppercase() throws Exception {
        def result = script.call(refspec: 'PR/111111')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/111111'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr111111"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr111111 were pushed"))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_lowercase() throws Exception {
        def result = script.call(refspec: 'pr/222222')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/222222'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr222222"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr222222 were pushed"))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_refspec_no_case() throws Exception {
        def result = script.call(refspec: 'pR/333333')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana refspec is: PR/333333'))
        assertTrue(assertMethodCallContainsPattern('log', "Tagging docker.elastic.co/kibana/kibana:8.0.0-SNAPSHOT to docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr333333"))
        assertTrue(assertMethodCallContainsPattern('log', "docker.elastic.co/observability-ci/kibana:${SHA} and docker.elastic.co/observability-ci/kibana:pr333333 were pushed"))
        assertJobStatusSuccess()
    }
}
