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

/**
  Wrap the credentials and entrypoints as environment variables that are masked
  for the Cloud deployments, aka clusters.

  withClusterEnv(cluster: 'test-cluster-azure') {
    // block
  }
*/

def call(Map args = [:], Closure body) {
  log(level: 'INFO', text: 'withClusterEnv')

  // For backward compatibilites, let's keep the previous behaviour,
  // and keep Elasticsearch enable by default .
  withElasticsearch(args, args.get('elasticsearch', true)) {
    // For backward compatibilites, let's keep the previous behaviour,
    // and enable the kibana or fleet env variables context explicitly.
    withKibana(args, args.get('kibana', false)) {
      withFleet(args, args.get('fleet', false)) {
        body()
      }
    }
  }
}

def withElasticsearch(args, enabled, body) {
  if (enabled) {
    withElasticsearchDeploymentEnv(args) {
      body()
    }
  } else {
    body()
  }
}

def withKibana(args, enabled, body) {
  if (enabled) {
    withKibanaDeploymentEnv(args) {
      body()
    }
  } else {
    body()
  }
}

def withFleet(args, enabled, body) {
  if (enabled) {
    withFleetDeploymentEnv(args) {
      body()
    }
  } else {
    body()
  }
}
