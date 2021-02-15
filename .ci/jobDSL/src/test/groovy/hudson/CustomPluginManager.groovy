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

// jenkins-test-harness does not handle detached plugins properly
// see https://issues.jenkins.io/browse/JENKINS-60295?focusedCommentId=400912&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-400912
package hudson
import java.util.logging.Logger
import org.jvnet.hudson.test.TestPluginManager
/**
 * Modify plugin load behavior for jenkins for test
 */
class CustomPluginManager extends TestPluginManager {
    private static final Logger LOGGER = Logger.getLogger( CustomPluginManager.name )

    // Skip loading all detached plugins as they conflict with our explicit build.gradle jenkinsPlugins dependencies
    @Override
    void considerDetachedPlugin( String shortName ) {
        LOGGER.info( 'Skipping load of detached plugin: ' + shortName )
    }
}
