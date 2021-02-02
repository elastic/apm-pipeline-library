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
import net.sf.json.JSONArray

class GithubReleaseCreateTests extends ApmBasePipelineTest {
  def script

  def shInterceptor = { return toJSON([
      "url": "https://api.github.com/repos/octocat/Hello-World/releases/1",
      "html_url": "https://github.com/octocat/Hello-World/releases/v1.0.0",
      "assets_url": "https://api.github.com/repos/octocat/Hello-World/releases/1/assets",
      "upload_url": "https://uploads.github.com/repos/octocat/Hello-World/releases/1/assets{?name,label}",
      "tarball_url": "https://api.github.com/repos/octocat/Hello-World/tarball/v1.0.0",
      "zipball_url": "https://api.github.com/repos/octocat/Hello-World/zipball/v1.0.0",
      "id": 1,
      "node_id": "MDc6UmVsZWFzZTE=",
      "tag_name": "v1.0.0",
      "target_commitish": "master",
      "name": "v1.0.0",
      "body": "Description of the release",
      "draft": false,
      "prerelease": false,
      "created_at": "2013-02-27T19:35:32Z",
      "published_at": "2013-02-27T19:35:32Z",
      "author": [
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "html_url": "https://github.com/octocat",
        "followers_url": "https://api.github.com/users/octocat/followers",
        "following_url": "https://api.github.com/users/octocat/following{/other_user}",
        "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
        "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
        "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
        "organizations_url": "https://api.github.com/users/octocat/orgs",
        "repos_url": "https://api.github.com/users/octocat/repos",
        "events_url": "https://api.github.com/users/octocat/events{/privacy}",
        "received_events_url": "https://api.github.com/users/octocat/received_events",
        "type": "User",
        "site_admin": false
      ],
      "assets": [
      ]
    ])
  }

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
    script = loadScript('vars/githubReleaseCreate.groovy')
    env.ORG_NAME = 'dummy_org'
    env.REPO_NAME = 'dummy_repo'
  }

  @Test
  void test() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], shInterceptor)
    script.BUILD_TAG = "dummy_tag"
    def ret = script.call(url: "dummy", token: "dummy")
    printCallStack()
    assertTrue(ret.tag_name == "v1.0.0")
    assertJobStatusSuccess()
  }

  @Test
  void testNoOrg() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], shInterceptor)
    env.remove("ORG_NAME")  // Will be reset by setUp()
    try {
      script.call(url: "dummy", token: "dummy")
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gitHubReleaseCreate: Environment not initialized. Try to call githubEnv step before'))
  }

  @Test
  void testNoRepo() throws Exception {
    helper.registerAllowedMethod("githubApiCall", [Map.class], shInterceptor)
    env.remove("REPO_NAME")  // Will be reset by setUp()
    try {
      script.call(url: "dummy", token: "dummy")
    } catch(e) {
      //NOOP
    }
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('error', 'gitHubReleaseCreate: Environment not initialized. Try to call githubEnv step before'))
  }

}
