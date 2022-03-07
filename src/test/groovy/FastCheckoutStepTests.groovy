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

class FastCheckoutStepTests extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/fastCheckout.groovy')
  }

  @Test
  void testNoUrl() throws Exception {
    testMissingArgument('url') {
      script.call()
    }
  }

  @Test
  void testDefaultBranch() throws Exception {
    script.call(url: 'https://foo.com/bar/barfoo.git')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "efectiveBranch: 'origin/main', efectiveRefspec: '+refs/heads/main:refs/remotes/origin/main'"))
    assertJobStatusSuccess()
  }

  @Test
  void testBranch() throws Exception {
    script.call(refspec: 'foo', url: 'https://foo.com/bar/barfoo.git')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "efectiveBranch: 'origin/foo', efectiveRefspec: '+refs/heads/foo:refs/remotes/origin/foo'"))
    assertJobStatusSuccess()
  }
  @Test
  void testPR() throws Exception {
    script.call(refspec: 'PR/12345', url: 'https://foo.com/bar/barfoo.git')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "efectiveBranch: 'origin/PR/12345', efectiveRefspec: '+refs/pull/12345/head:refs/remotes/origin/PR/12345'"))
    assertJobStatusSuccess()
  }
  @Test
  void testCommit() throws Exception {
    script.call(refspec: 'aa3bed18072672e89a8e72aec43c96831ff2ce05', url: 'https://foo.com/bar/barfoo.git')
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('log', "efectiveBranch: 'aa3bed18072672e89a8e72aec43c96831ff2ce05', efectiveRefspec: 'aa3bed18072672e89a8e72aec43c96831ff2ce05'"))
    assertJobStatusSuccess()
  }
}
