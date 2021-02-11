// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
// jenkins-test-harness does not handle detached plugins properly
// see https://issues.jenkins.io/browse/JENKINS-60295?focusedCommentId=400912&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-400912
package hudson
import org.jvnet.hudson.test.JenkinsRule
import java.util.logging.Logger
/**
 * JenkinsRule to use in Job DSL scripts tests
 */
class CustomJenkinsRule extends JenkinsRule {
  private static final Logger LOGGER = Logger.getLogger( CustomJenkinsRule.name )
    CustomJenkinsRule() {
        super()
        LOGGER.info( 'Loading CustomJenkinsRule')
        this.pluginManager = new CustomPluginManager()
    }
}
