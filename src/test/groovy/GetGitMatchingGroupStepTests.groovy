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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class GetGitMatchingGroupStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/getGitMatchingGroup.groovy'

  def realData = '''CHANGELOG.next.asciidoc
metricbeat/docs/modules/zookeeper.asciidoc
metricbeat/docs/modules/zookeeper/connection.asciidoc
metricbeat/docs/modules_list.asciidoc
metricbeat/module/zookeeper/_meta/docs.asciidoc
metricbeat/module/zookeeper/connection/_meta/docs.asciidoc
metricbeat/module/zookeeper/connection/_meta/fields.yml
metricbeat/module/zookeeper/connection/connection.go
metricbeat/module/zookeeper/fields.go
metricbeat/module/zookeeper/mntr/_meta/docs.asciidoc
metricbeat/module/zookeeper/server/_meta/docs.asciidoc'''.stripMargin().stripIndent()

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    env.CHANGE_TARGET = 'foo'
    env.GIT_BASE_COMMIT = 'bar'
  }

  @Test
  void test_without_pattern_parameter() throws Exception {
    def script = loadScript(scriptName)
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'Missing pattern argument.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_env_variables() throws Exception {
    def script = loadScript(scriptName)
    def result = true
    env.remove('CHANGE_TARGET')
    def module = script.call(pattern: 'foo')
    printCallStack()
    assertEquals('', module)
    assertTrue(assertMethodCallContainsPattern('log', 'CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/bar/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    assertEquals('foo', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_match_with_previous_commit_env_variable() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'foo/bar/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    printCallStack()
    assertEquals('foo', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = '''foo/bar/file.txt
foo/bar/subfolder'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    assertEquals('foo', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_match_2() throws Exception {
    def script = loadScript(scriptName)
    def changeset = '''filebeat/README.md
filebeat/Dockerfile
filebeat/docs/faq.asciidoc
filebeat/autodiscover/builder/hints/config.go'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    assertEquals('filebeat', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_without_match() throws Exception {
    def script = loadScript(scriptName)
    def changeset = '''foo/bar/file.txt
bar/foo/subfolder'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    assertEquals('', module)
    assertTrue(assertMethodCallContainsPattern('log', 'not found'))
    assertJobStatusSuccess()
  }

  @Test
  void test_simple_unmatch() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '^unknown.txt')
    printCallStack()
    assertEquals('', module)
    assertTrue(assertMethodCallContainsPattern('log', 'not found with regex ^unknown.txt'))
    assertJobStatusSuccess()
  }

  @Test
  void test_windows() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('isUnix', [], { false })
    try {
      script.call()
    } catch(e){
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'windows is not supported yet.'))
    assertJobStatusFailure()
  }

  @Test
  void test_without_change_request_env_variable() throws Exception {
    env.GIT_PREVIOUS_COMMIT = "foo-1"
    env.remove('CHANGE_TARGET')
    def script = loadScript(scriptName)
    def changeset = 'foo/bar/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      assertFalse(m.script.contains('origin/'))
    })
    def module = script.call(pattern: '([^\\/]+)\\/.*')
    printCallStack()
    assertEquals('foo', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_change_target_env_variable() throws Exception {
    env.CHANGE_TARGET = " "
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      assertFalse(m.script.contains('origin/'))
    })
    def module = script.call(pattern: 'foo')
    printCallStack()
    assertEquals('', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_with_from_parameter() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/anotherfolder/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    def module = script.call(pattern: '([^\\/]+)\\/.*', from: 'something')
    printCallStack()
    assertEquals('foo', module)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...bar'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_from_and_to_parameters() throws Exception {
    def script = loadScript(scriptName)
    def changeset = 'foo/bar/file.txt'
    helper.registerAllowedMethod('readFile', [String.class], { return changeset })
    helper.registerAllowedMethod('sh', [Map.class], { m ->
      assertFalse(m.script.contains('origin/'))
    })
    def module = script.call(pattern: '([^\\/]+)\\/.*', from: 'something', to: 'else')
    printCallStack()
    assertEquals('foo', module)
    assertTrue(assertMethodCallContainsPattern('sh', 'something...else'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_values_for_from_and_to_parameters() throws Exception {
    def script = loadScript(scriptName)
    def module = script.call(pattern: '^foo/.*/file.txt', from: '', to: '')
    printCallStack()
    assertEquals('', module)
    assertTrue(assertMethodCallContainsPattern('log', 'CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_with_empty_value_for_to_parameter() throws Exception {
    def script = loadScript(scriptName)
    def module = script.call(pattern: '^foo/.*/file.txt', to: '')
    printCallStack()
    assertEquals('', module)
    assertTrue(assertMethodCallContainsPattern('log', 'CHANGE_TARGET or GIT_PREVIOUS_COMMIT and GIT_BASE_COMMIT env variables are required to evaluate the changes.'))
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_match_with_real_data_with_exclude() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*', exclude: '(.*\\/docs\\/.*|.*\\.asciidoc)' )
    assertEquals('zookeeper', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_multiple_match_with_real_data_without_exclude() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*')
    assertEquals('', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_is_excluded() throws Exception {
    def script = loadScript(scriptName)
    assertFalse(script.isExcluded('', ''))
    assertTrue(script.isExcluded('metricbeat/docs/modules/zookeeper.asciidoc', '(.*\\/docs\\/.*|.*\\.asciidoc)'))
    assertFalse(script.isExcluded('metricbeat/zookeeper.asciido', '(.*\\/docs\\/.*|.*\\.asciidoc)'))
  }

  @Test
  void test_match_in_beats_pr18369() throws Exception {
    def script = loadScript(scriptName)
    def realData = '''metricbeat/docs/fields.asciidoc
metricbeat/docs/images/metricbeat-googlecloud-load-balancing-https-overview.png
metricbeat/docs/images/metricbeat-googlecloud-load-balancing-l3-overview.png
metricbeat/docs/modules/googlecloud.asciidoc
x-pack/metricbeat/module/googlecloud/_meta/docs.asciidoc
x-pack/metricbeat/module/googlecloud/_meta/kibana/7/dashboard/Metricbeat-googlecloud-load-balancing-https-overview.json
x-pack/metricbeat/module/googlecloud/_meta/kibana/7/dashboard/Metricbeat-googlecloud-loadbalancing-l3-overview.json
x-pack/metricbeat/module/googlecloud/fields.go
x-pack/metricbeat/module/googlecloud/loadbalancing/_meta/fields.yml
x-pack/metricbeat/module/googlecloud/stackdriver/metrics_requester.go
x-pack/metricbeat/module/googlecloud/stackdriver/metricset.go'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*', exclude: '(.*\\/docs\\/.*|.*\\.asciidoc)' )
    assertEquals('googlecloud', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_match_in_beats_pr18609() throws Exception {
    def script = loadScript(scriptName)
    def realData = '''libbeat/esleg/eslegclient/bulkapi.go
metricbeat/module/elasticsearch/elasticsearch.go
metricbeat/module/elasticsearch/ml_job/ml_job.go'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*', exclude: '(.*\\/docs\\/.*|.*\\.asciidoc|^libbeat.*)' )
    assertEquals('elasticsearch', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_match_in_beats_pr18608() throws Exception {
    def script = loadScript(scriptName)
    def realData = '''CHANGELOG.next.asciidoc
libbeat/cmd/instance/beat.go
libbeat/docs/monitoring/monitoring-beats.asciidoc
libbeat/docs/monitoring/monitoring-internal-collection-legacy.asciidoc
libbeat/docs/monitoring/shared-monitor-config-legacy.asciidoc
libbeat/esleg/eslegclient/bulkapi.go
libbeat/monitoring/monitoring.go
libbeat/monitoring/report/elasticsearch/client.go
libbeat/monitoring/report/elasticsearch/config.go
libbeat/monitoring/report/elasticsearch/elasticsearch.go
libbeat/monitoring/report/report.go
libbeat/tests/system/config/mockbeat.yml.j2
libbeat/tests/system/test_monitoring.py'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*', exclude: '(.*\\/docs\\/.*|.*\\.asciidoc|^libbeat.*)' )
    assertEquals('', module)
    assertJobStatusSuccess()
  }

  @Test
  void test_match_in_beats_pr18541() throws Exception {
    def script = loadScript(scriptName)
    def realData = '''libbeat/dashboards/config.go
libbeat/dashboards/dashboards.go
libbeat/dashboards/decode.go
libbeat/dashboards/importer.go
libbeat/dashboards/kibana_loader.go
libbeat/dashboards/modify_json.go'''.stripMargin().stripIndent()
    helper.registerAllowedMethod('readFile', [String.class], { return realData })
    def module = script.call(pattern: '.*\\/module\\/([^\\/]+)\\/.*', exclude: '(.*\\/docs\\/.*|.*\\.asciidoc|^libbeat.*)' )
    assertEquals('', module)
    assertJobStatusSuccess()
  }
}
