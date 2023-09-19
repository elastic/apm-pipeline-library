
## About

GitHub Action to publish daily snapshots

___

* [Customizing](#customizing)
  * [inputs](#inputs)

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `config`          | String  |                             | Path to configuration file |
| `bucketName`      | String  |                             | Name of the bucket to use |
| `gcsClientEmail`  | String  |                             | Google Cloud email of the service account |
| `gcsPrivateKey`   | String  |                             | Google Cloud private key of the service account |
| `gcsPrivateKeyId` | String  |                             | Google Cloud private key id of the service account |
| `gcsProject`      | String  |                             | Google Cloud project id of the service account |


In addition, you can pass env variables with the prefix `SNAPSHOTY_`, the prefix will be removed and the name
of the variable will be passed.

### Usage


```yaml
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/snapshoty@current
        with:
          config: snapshoty.yml
          bucketName: 'my-bucket'
          gcsClientEmail: 'my-email@acme.org'
          gcsPrivateKey: 'my-secret-key'
          gcsPrivateKeyId: 'my-private-key'
          gcsProject: 'my-gcs-project'
        env:
          SNAPSHOTY_DATE: "2023-09-20"
```
