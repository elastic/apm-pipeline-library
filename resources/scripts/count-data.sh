# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

cat data-test_conc_req_rails_foobar.json|jq '.hits.hits[]._source.processor.event'|sort|uniq -c

```
2456 "span"
1726 "transaction"
```

```
Error
AssertionError: queried for [{'processor.event': 'transaction'}], expected 1820, got 1726
Stacktrace
es = <tests.fixtures.es.es.<locals>.Elasticsearch object at 0x7ff8047877f0>
apm_server = <tests.fixtures.apm_server.apm_server.<locals>.APMServer object at 0x7ff804787668>
rails = <tests.fixtures.agents.Agent object at 0x7ff802144fd0>
```
