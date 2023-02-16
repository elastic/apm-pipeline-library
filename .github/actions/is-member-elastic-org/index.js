const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  const user = core.getInput('user');
  const token = core.getInput('token');

  try {
    // Validate action inputs
    if (!user) {
      throw new Error('user required');
    }
    if (!token) {
      throw new Error('enrollmentToken required');
    }

    const octokit = new github.getOctokit(token);
    core.info(`Validated Succeeded!`);
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
    core.warning('something went wrong');
    core.setFailed(error.message);
  }
}

run();
