const core = require('@actions/core');
const exec = require('@actions/exec');
const os = require("os");

async function run() {
  try {
    const gcsClientEmail = core.getInput('gcsClientEmail');
    const gcsPrivateKey = core.getInput('gcsPrivateKey');
    const gcsPrivateKeyId = core.getInput('gcsPrivateKeyId');
    const gcsProject = core.getInput('gcsProject');
    const bucketName = core.getInput('bucketName');
    const config = core.getInput('config');

    const workDir = process.env.GITHUB_WORKSPACE;
    const userInfo = os.userInfo();

    const args = [
      'run', '--rm',
      '-v', `${workDir}:/app`,
      '-u', `${userInfo.uid}:${userInfo.gid}`,
      '-w', '/app',
      '-e', `GCS_CLIENT_EMAIL=${gcsClientEmail}`,
      '-e', `GCS_PRIVATE_KEY=${gcsPrivateKey}`,
      '-e', `GCS_PRIVATE_KEY_ID=${gcsPrivateKeyId}`,
      '-e', `GCS_PROJECT=${gcsProject}`
    ]
    Object.keys(process.env).forEach(function (key) {
      if (key.startsWith("GITHUB_") || key.startsWith("RUNNER_")) {
        let value = process.env[key];
        args.push('-e', `${key}=${value}`);
        core.setSecret(value);
      }
    });
    args.push('docker.elastic.co/observability-ci/snapshoty:v1', 'snapshoty');
    if (core.isDebug()) {
      args.push('--debug');
    }
    args.push('--config', config, 'upload', '--bucket-name', bucketName);

    return await exec.exec('docker', args);
  } catch (error) {
    core.setFailed(error.message);
  }
}

run();
