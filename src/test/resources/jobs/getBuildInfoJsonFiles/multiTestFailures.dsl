NAME = 'it/getBuildInfoJsonFiles/multiTestFailures'
DSL = '''pipeline {
  agent { label 'local' }
  stages {
    stage('unstable') {
      steps {
        writeFile file: 'junit.xml', text: """<?xml version="1.0" encoding="utf-8"?>
<testsuites><testsuite errors="0" failures="2" hostname="worker-c07l34n6dwym.build.macstadium.elasticnet.co" name="pytest" skipped="1" tests="4" time="305.571" timestamp="2020-11-23T05:43:39.974540">
<testcase classname="filebeat.tests.system.test_autodiscover.TestAutodiscover" file="filebeat/tests/system/test_autodiscover.py" line="44" name="test_default_settings" time="0.002"><skipped message="integration test not available on 2.x" type="pytest.skip">/private/var/lib/jenkins/workspace/Beats_beats_master/src/github.com/elastic/beats/filebeat/tests/system/test_autodiscover.py:45: integration test not available on 2.x</skipped></testcase>
<testcase classname="filebeat.tests.system.test_crawler.Test" file="filebeat/tests/system/test_crawler.py" line="208" name="test_file_disappear" time="1.225"></testcase>
<testcase classname="filebeat.tests.system.test_harvester.Test" file="filebeat/tests/system/test_harvester.py" line="824" name="test_debug_reader" time="5.176"><failure message="beat.beat.TimeoutError: Timeout waiting for &apos;cond&apos; to be true. Waited 5 seconds.">self = &lt;test_harvester.Test testMethod=test_debug_reader&gt;

    def test_debug_reader(self):
        &quot;&quot;&quot;
        Test that you can enable a debug reader.
        &quot;&quot;&quot;
        self.render_config_template(
            path=os.path.abspath(self.working_dir) + &quot;/log/*&quot;,
        )
    
        os.mkdir(self.working_dir + &quot;/log/&quot;)
    
        logfile = self.working_dir + &quot;/log/test.log&quot;
    
        lines = [
            b&quot;Hello World\\n&quot;,
        ]
        with open(logfile, &apos;wb&apos;) as f:
            for line in lines:
                f.write(line)
    
            # Write some more data to hit the 16k min buffer size.
            # Make it web safe.
            f.write(base64.b64encode(os.urandom(16 * 1024)))
    
        filebeat = self.start_beat()
    
        # 13 on unix, 14 on windows.
        self.wait_until(lambda: self.log_contains(re.compile(
&gt;           &apos;Matching null byte found at offset (13|14)&apos;)), max_timeout=5)

tests/system/test_harvester.py:859: 
_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 

self = &lt;test_harvester.Test testMethod=test_debug_reader&gt;
cond = &lt;function Test.test_debug_reader.&lt;locals&gt;.&lt;lambda&gt; at 0x10abc8b00&gt;
max_timeout = 5, poll_interval = 0.1, name = &apos;cond&apos;

    def wait_until(self, cond, max_timeout=10, poll_interval=0.1, name=&quot;cond&quot;):
        &quot;&quot;&quot;
        Waits until the cond function returns true,
        or until the max_timeout is reached. Calls the cond
        function every poll_interval seconds.
    
        If the max_timeout is reached before cond() returns
        true, an exception is raised.
        &quot;&quot;&quot;
        start = datetime.now()
        while not cond():
            if datetime.now() - start &gt; timedelta(seconds=max_timeout):
                raise TimeoutError(&quot;Timeout waiting for &apos;{}&apos; to be true. &quot;.format(name) +
&gt;                                  &quot;Waited {} seconds.&quot;.format(max_timeout))
E               beat.beat.TimeoutError: Timeout waiting for &apos;cond&apos; to be true. Waited 5 seconds.

../libbeat/tests/system/beat/beat.py:363: TimeoutError</failure></testcase>
<testcase classname="filebeat.tests.system.test_json.Test" file="filebeat/tests/system/test_json.py" line="154" name="test_timestamp_in_message" time="10.179"><failure message="beat.beat.TimeoutError: Timeout waiting for &apos;cond&apos; to be true. Waited 10 seconds.">self = &lt;test_json.Test testMethod=test_timestamp_in_message&gt;

    def test_timestamp_in_message(self):
        &quot;&quot;&quot;
        Should be able to make use of a `@timestamp` field if it exists in the
        message.
        &quot;&quot;&quot;
        self.render_config_template(
            path=os.path.abspath(self.working_dir) + &quot;/log/*&quot;,
            json=dict(
                keys_under_root=True,
                overwrite_keys=True,
                add_error_key=True,
            ),
        )
        os.mkdir(self.working_dir + &quot;/log/&quot;)
        self.copy_files([&quot;logs/json_timestamp.log&quot;],
                        target_dir=&quot;log&quot;)
    
        proc = self.start_beat()
        self.wait_until(
            lambda: self.output_has(lines=5),
&gt;           max_timeout=10)

tests/system/test_json.py:175: 
_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 

self = &lt;test_json.Test testMethod=test_timestamp_in_message&gt;
cond = &lt;function Test.test_timestamp_in_message.&lt;locals&gt;.&lt;lambda&gt; at 0x10a870950&gt;
max_timeout = 10, poll_interval = 0.1, name = &apos;cond&apos;

    def wait_until(self, cond, max_timeout=10, poll_interval=0.1, name=&quot;cond&quot;):
        &quot;&quot;&quot;
        Waits until the cond function returns true,
        or until the max_timeout is reached. Calls the cond
        function every poll_interval seconds.
    
        If the max_timeout is reached before cond() returns
        true, an exception is raised.
        &quot;&quot;&quot;
        start = datetime.now()
        while not cond():
            if datetime.now() - start &gt; timedelta(seconds=max_timeout):
                raise TimeoutError(&quot;Timeout waiting for &apos;{}&apos; to be true. &quot;.format(name) +
&gt;                                  &quot;Waited {} seconds.&quot;.format(max_timeout))
E               beat.beat.TimeoutError: Timeout waiting for &apos;cond&apos; to be true. Waited 10 seconds.

../libbeat/tests/system/beat/beat.py:363: TimeoutError</failure></testcase>
</testsuite>
</testsuites>
        """
        junit 'junit.xml'
      }
    }
  }
  post {
    cleanup {
      deleteDir()
      getBuildInfoJsonFiles(env.JOB_URL, env.BUILD_NUMBER)
      archiveArtifacts artifacts: '*.json'
      sh """#!/bin/bash -xe
      ## Assert json modifications
      jq '.build.result' build-report.json | grep 'UNSTABLE'
      jq '.build.state' build-report.json | grep 'FINISHED'
      jq '.test_summary.total' build-report.json | grep '4'
      ## Assert all the files are there
      [ -e 'artifacts-info.json' ] && echo yeah || exit 1
      [ -e 'changeSet-info.json' ] && echo yeah || exit 1
      [ -e 'job-info.json' ] && echo yeah || exit 1
      [ -e 'tests-summary.json' ] && echo yeah || exit 1
      [ -e 'tests-info.json' ] && echo yeah || exit 1
      """
    }
  }
}'''

pipelineJob(NAME) {
  definition {
    cps {
      script(DSL.stripIndent())
    }
  }
}
