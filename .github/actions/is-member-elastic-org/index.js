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

    core.info(`Checking if ${user} is a member of elastic`);
    const { status } = await octokit.rest.orgs.checkMembershipForUser({
      org: 'elastic',
      username,
    });

    if (status === 204) {
      core.info(`${user} is an organization member`);
      core.setOutput("result", true);
    } else {
      core.warning(`${username} is not an organization member`);
      core.setOutput("result", false);
    }

  } catch (error) {
    core.warning(error);
    core.setFailed(error.message);
  }
}

run();
