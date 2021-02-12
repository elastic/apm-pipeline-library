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

class LicenseScanTest extends ApmBasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/licenseScan.groovy')

    helper.registerAllowedMethod('sh', [Map.class],{m -> return 0})
  }

  @Test
  void testGo() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/*.go'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type go analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'golang'))
    assertJobStatusSuccess()
  }

  @Test
  void testNode() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/package.json'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type nodejs analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'node:lts'))
    assertJobStatusSuccess()
  }

  @Test
  void testRuby() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/*.rb' || m.glob ==  '**/Gemfile'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type gem analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'ruby:2.5'))
    assertJobStatusSuccess()
  }

  @Test
  void testPython() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/*.py' || m.glob ==  '**/requirements.txt'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa analyze'))
    assertJobStatusSuccess()
  }

  @Test
  void testPhp() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/*.php' || m.glob ==  '**/composer.json'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type composer analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'composer:1.10.7'))
    assertJobStatusSuccess()
  }

  @Test
  void testAnt() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/build.xml'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type ant analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'apache-ant'))
    assertJobStatusSuccess()
  }

  @Test
  void testMaven() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/pom.xml'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type mvn analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'maven'))
    assertJobStatusSuccess()
  }

  @Test
  void testGradle() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/build.gradle'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa --type gradle analyze'))
    assertTrue(assertMethodCallContainsPattern('sh', 'gradle'))
    assertJobStatusSuccess()
  }

  @Test
  void testNet() throws Exception {
    helper.registerAllowedMethod('findFiles', [Map.class], {
      m -> return m.glob == '**/*.csproj'
    })
    script.call()

    printCallStack()
    assertTrue(assertMethodCallContainsPattern('getVaultSecret', 'secret=secret/jenkins-ci/fossa/api-token'))
    assertTrue(assertMethodCallContainsPattern('withEnvMask', 'FOSSA_API_KEY'))
    assertTrue(assertMethodCallContainsPattern('sh', 'fossa analyze'))
    assertJobStatusSuccess()
  }
}
