const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  const user = core.getInput('user');
  const token = core.getInput('token');
  const octokit = new github.getOctokit(token);

  try {
    const { status } = await octokit.rest.orgs.checkMembershipForUser({
      org: "elastic",
      user,
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
