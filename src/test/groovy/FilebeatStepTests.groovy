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

class FilebeatStepTests extends ApmBasePipelineTest {
  // test resources file uses this value as filename
  String nodeName = 'worker-0676d01d9601f8191'
  String jsonConfig = "filebeat_container_" + nodeName + ".json"
  String resources = "target/test-classes"

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/filebeat.groovy')
    env.NODE_NAME = nodeName
    env.WORKSPACE = "filebeatTest"
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
    helper.registerAllowedMethod('sh', [Map.class], { "OK" })
    helper.registerAllowedMethod('pwd', [], { 'filebeatTest' })
    helper.registerAllowedMethod('isBuildFailure', [], { false })
    helper.registerAllowedMethod('readFile', [Map.class], { 'fooID' })
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    printCallStack(){
      script.call()
    }
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=filebeatTest/filebeat_conf.yml"))
    assertTrue(assertMethodCallContainsPattern('sh', 'run_filebeat.sh'))
    assertJobStatusSuccess()
  }

  @Test
  void testClosure() throws Exception {
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest_1"
    def config = "bar.xml"
    def image = "foo:latest"

    helper.registerAllowedMethod('fileExists', [String.class], { f ->
      if(f == "${workdir}/${config}"){
        return false
      } else {
        return true
      }
    })
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})

    printCallStack(){
      script.call(
        output: output,
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
    assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }

  @Test(expected = Exception.class)
  void testClosureError() throws Exception {
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})

    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest_1"
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
        output: output,
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
      assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
      assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
      assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    }
  }

  @Test
  void testConfigurationExists() throws Exception {
    printCallStack(){
      script.call()
    }
    assertTrue(assertMethodCallContainsPattern('sh', 'run_filebeat.sh'))
    assertFalse(assertMethodCallContainsPattern('writeFile', 'file=filebeat_conf.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void testStop() throws Exception {
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})

    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest"

    printCallStack(){
      script.stop(
        workdir: workdir,
      )
    }
    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${jsonConfig}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }

  @Test
  void testStopArchiveOnlyOnFailTrueSuccessBuild() throws Exception {
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest_2"
    helper.registerAllowedMethod('isBuildFailure', [], { false })

    printCallStack(){
      script.stop(
        workdir: workdir,
      )
    }
    assertFalse(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }

  @Test
  void testStopArchiveOnlyOnFailTrueFaulureBuild() throws Exception {
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest_2"
    helper.registerAllowedMethod('isBuildFailure', [], { true })

    printCallStack(){
      script.stop(
        workdir: workdir,
      )
    }
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }
}
