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
    }

    @Test
    void test_without_target() throws Exception {
        def result = script.call()
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana branch is: master'))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_branch_target() throws Exception {
        def result = script.call(branch: 'foo')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana branch is: foo'))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_target_uppercase() throws Exception {
        def result = script.call(branch: 'PR/123456')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana branch is: PR/123456'))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_target_lowercase() throws Exception {
        def result = script.call(branch: 'pr/123456')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana branch is: PR/123456'))
        assertJobStatusSuccess()
    }

    @Test
    void test_with_PR_target_no_case() throws Exception {
        def result = script.call(branch: 'pR/123456')
        assertTrue(assertMethodCallContainsPattern('log', 'Kibana branch is: PR/123456'))
        assertJobStatusSuccess()
    }
}
