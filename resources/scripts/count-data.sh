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
