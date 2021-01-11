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
  String scriptName = 'vars/filebeat.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'filebeat_conf.yml:/usr/share/filebeat/filebeat.yml'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'file=filebeat_conf.yml'))
    assertTrue(assertMethodCallContainsPattern('writeFile', 'filename: docker_logs.log'))
    assertTrue(assertMethodCallContainsPattern('sh', 'docker.elastic.co/beats/filebeat'))
    assertJobStatusSuccess()
  }

  @Test
  void testClosure() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})
    def stepConfig = "filebeat_container_config.json"
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest"
    def config = "bar.xml"
    def image = "foo:latest"
    def script = loadScript(scriptName)

    script.call(
      output: output,
      config: config,
      image: image,
      workdir: workdir,
      ){
      print("OK")
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "${config}:/usr/share/filebeat/filebeat.yml"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${config}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "filename: ${output}"))
    assertTrue(assertMethodCallContainsPattern('sh', "${image}"))

    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${stepConfig}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }

  @Test
  void testClosureError() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})
    def stepConfig = "filebeat_container_config.json"
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest"
    def config = "bar.xml"
    def image = "foo:latest"
    def script = loadScript(scriptName)

    try {
      script.call(
        output: output,
        config: config,
        image: image,
        workdir: workdir,
        ){
        throw new Exception('Ooops!!')
      }
    } catch(e){
      //NOOP
    } finally {
      printCallStack()
      assertTrue(assertMethodCallContainsPattern('sh', "${config}:/usr/share/filebeat/filebeat.yml"))
      assertTrue(assertMethodCallContainsPattern('writeFile', "file=${config}"))
      assertTrue(assertMethodCallContainsPattern('writeFile', "filename: ${output}"))
      assertTrue(assertMethodCallContainsPattern('sh', "${image}"))

      assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${stepConfig}"))
      assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
      assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
      assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    }
  }

  @Test
  void testConfigurationExists() throws Exception {
    def script = loadScript(scriptName)
    script.call()
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'filebeat_conf.yml:/usr/share/filebeat/filebeat.yml'))
    assertTrue(assertMethodCallContainsPattern('sh', 'docker.elastic.co/beats/filebeat'))
    assertFalse(assertMethodCallContainsPattern('writeFile', 'file=filebeat_conf.yml'))
    assertJobStatusSuccess()
  }

  @Test
  void testArguments() throws Exception {
    helper.registerAllowedMethod('fileExists', [String.class], { false })
    def output = "foo.log"
    def config = "bar.xml"
    def image = "foo:latest"
    def workdir = "fooDir"

    def script = loadScript(scriptName)
    script.call(
      output: output,
      config: config,
      image: image,
      workdir: workdir,
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', "${config}:/usr/share/filebeat/filebeat.yml"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "file=${config}"))
    assertTrue(assertMethodCallContainsPattern('writeFile', "filename: ${output}"))
    assertTrue(assertMethodCallContainsPattern('sh', "${image}"))
    assertTrue(assertMethodCallContainsPattern('sh', "${workdir}:/output"))
    assertJobStatusSuccess()
  }

  @Test
  void testStop() throws Exception {
    helper.registerAllowedMethod('archiveArtifacts', [Map.class], { m -> return m.artifacts})
    def config = "filebeat_container_config.json"
    def id = "fooID"
    def output = "foo.log"
    def workdir = "filebeatTest"

    def script = loadScript(scriptName)
    script.stop(
      workdir: workdir,
    )
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('readJSON', "file=${workdir}/${config}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker exec -t ${id}"))
    assertTrue(assertMethodCallContainsPattern('sh', "docker stop --time 30 ${id}"))
    assertTrue(assertMethodCallContainsPattern('archiveArtifacts', "artifacts=**/${output}*"))
    assertJobStatusSuccess()
  }
}
