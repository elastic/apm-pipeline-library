# Storage

We are using the Google Storage Jenkins plugins but it is disabled for the time being because it has some issues with some parallelisation.

In order to test this `locally`, we have configured some vault credentials. See below if you need to change them:

## How to

### Modify the existing credentials for the google service account

If for any reason you'd like to update the existing credentials then you need to follow the below steps:

1. Go to [Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts?project=elastic-observability)
1. Select the `test-google-storage-plugin-download` one.
1. Create a new JSON key type.
1. Transform to base64 and update the existing vault entry

```bash
$ base64 -i elastic-observability-*********.json -o base64.json
$ vault write secret/observability-team/ci/service-account/jenkins-google-storage-elastic-observability google_cloud_bucket_secret=@base64.json ticket=https://github.com/elastic/apm-pipeline-library/pull/867
```
