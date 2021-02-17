# Cloud

We are using the Google Compute Jenkins plugins.

See https://github.com/jenkinsci/google-compute-engine-plugin/#configuration-as-code-support for further examples.

## How to

### Add a new AMI

You can add new VMs, for such you need to:

1. Create a new template in the [Google Cloud](https://console.cloud.google.com/compute/instanceTemplates/list?project=elastic-observability)
1. Add a new entry in `local/configs/google.yaml`, that's the JCasC for the cloud provider.
1. If you prefer you can use the Jenkins UI. Go to the [configureClouds](http://localhost:18080/configureClouds/) menu.

Once you are happy with the configuration then you can export to a JCasC format, for such you just need to go to
[JCasC](http://localhost:18080/configuration-as-code/) and click on the `View Configuration` button.


### Modify the existing credentials for the google service account

If for any reason you'd like to update the existing credentials then you need to follow the below steps:

1. Go to [Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts?project=elastic-observability)
1. Select the `jenkins-gce` one.
1. Create a new JSON key type.
1. Transform to base64 and update the existing vault entry

```bash
$ base64 -i elastic-observability-*********.json -o base64.json
$ vault write secret/observability-team/ci/service-account/jenkins-gce-elastic-observability google_cloud_secret=@base64.json ticket=https://github.com/elastic/apm-pipeline-library/pull/356
```
