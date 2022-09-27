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

class MetricbeatStepTests extends ApmBasePipelineTest {
  // test resources file uses this value as filename
  String nodeName = 'worker-0676d01d9601f8191'
  String jsonConfig = "metricbeat_container_" + nodeName + ".json"
  String resources = "target/test-classes"

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/metricbeat.groovy')
    env.NODE_NAME = nodeName
    env.WORKSPACE = "metricbeatTest"
    helper.registerAllowedMethod('writeFile', [Map.class], { m ->
      (new File("${resources}/${m.file}")).withWriter('UTF-8') { writer ->
        writer.write(m.text)
      }
    })
    helper.registerAllowedMethod('writeJSON', [Map.class], { m ->
      def script = loadScript('vars/toJSON.groovy')
      def json =  script.call(m.json)
      (new File("${resources}/${m.file}")).withWriter('UTF-8') { writer ->
        writer.write(json.toString())
      }
    })
    helper.registerAllowedMethod('readJSON', [Map.class], { m ->
      def jsonSlurper = new groovy.json.JsonSlurperClassic()
      File f = new File("${resources}/${m.file}")
      jsonText = f.getText()
      return jsonSlurper.parseText(jsonText)
    })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      def ret = "OK"
      if(m.script.contains('docker run')){
        ret = 'fooID'
      }
      return ret
    })
    helper.registerAllowedMethod('pwd', [], { 'metricbeatTest' })
    helper.registerAllowedMethod('isBuildFailure', [], { false })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooID' })
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    printCallStack(){
      script.call(es_secret: 'foo')
    }
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=metricbeatTest/metricbeat_conf.yml"))
    assertTrue(assertMethodCallContainsPattern('sh', 'Run metricbeat to grab host metrics,'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoSecret_then_logs() throws Exception {
    def id = "fooID"
    def workdir = "metricbeatTest_1"
    def config = "bar.xml"
    def image = "foo:latest"

    helper.registerAllowedMethod('fileExists', [String.class], { f ->
      if(f == "${workdir}/${config}"){
        return false
      } else {
        return true
      }
    })
    printCallStack(){
      script.call(
        config: config,
        image: image,
        workdir: workdir,
        timeout: "30",
        ){
        print("OK")
      }
    }
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${workdir}/${config}"))
    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${jsonConfig}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
  }

  @Test
  void testClosure() throws Exception {
    def id = "fooID"
    def workdir = "metricbeatTest_1"
    def config = "bar.xml"
    def image = "foo:latest"

    helper.registerAllowedMethod('fileExists', [String.class], { f ->
      if(f == "${workdir}/${config}"){
        return false
      } else {
        return true
      }
    })

    printCallStack(){
      script.call(
        es_secret: 'foo',
        config: config,
        image: image,
        workdir: workdir,
        timeout: "30",
        ){
        print("OK")
      }
    }

    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${workdir}/${config}"))
    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${jsonConfig}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertJobStatusSuccess()
  }

  @Test(expected = Exception.class)
  void testClosureError() throws Exception {
    def id = "fooID"
    def output = "foo.log"
    def workdir = "metricbeatTest_1"
    def config = "bar.xml"
    def image = "foo:latest"

    helper.registerAllowedMethod('fileExists', [String.class], { f ->
      if(f == "${workdir}/${config}"){
        return false
      } else {
        return true
      }
    })

    try {
      script.call(
        es_secret: 'foo',
        config: config,
        image: image,
        workdir: workdir,
        timeout: "30",
        ){
        throw new Exception('Ooops!!')
      }
    } finally {
      printCallStack()
      assertTrue(assertMethodCallContainsPattern('writeFile', "file=${workdir}/${config}"))
      assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${jsonConfig}"))
      assertTrue(assertMethodCallContainsPattern('sh', 'Run metricbeat to grab host metrics,'))
      assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    }
  }

  @Test
  void testConfigurationExists() throws Exception {
    printCallStack(){
      script.call(es_secret: 'foo')
    }
    assertTrue(assertMethodCallContainsPattern('sh', 'Run metricbeat to grab host metrics,'))
    assertFalse(assertMethodCallContainsPattern('writeFile', 'file=metricbeat_conf.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void testStop() throws Exception {
    def id = "fooID"
    def output = "foo.log"
    def workdir = "metricbeatTest"

    printCallStack(){
      script.stop(
        workdir: workdir,
      )
    }
    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${jsonConfig}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertJobStatusSuccess()
  }

  @Test
  void testConfigFileNotExists() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })

    def workdir = "metricbeatTest_1"

    printCallStack(){
      script.stop(workdir: workdir)
    }
    assertTrue(assertMethodCallContainsPattern('log', "There is no configuration file to stop metricbeat."))
    assertJobStatusSuccess()
  }
}
