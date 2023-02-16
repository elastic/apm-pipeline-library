const core = require('@actions/core');

async function run() {
  const username = core.getInput('username');
  const token = core.getInput('token');
  const octokit = new github.getOctokit(token);

  try {
    const { status } = await octokit.rest.orgs.checkMembershipForUser({
      org: "elastic",
      username,
    });

    if (status === 204) {
      core.setOutput("result", true);
    } else {
      core.warning('user not a member of elastic');
      core.setOutput("result", false);
    }
  } catch (error) {
    core.setFailed(error.message);
  }
}

run();
