# Guidance on how to contribute

**Table of contents:**

1. [Using the issue tracker](#using-the-issue-tracker)
1. [Changing the code-base](#changing-the-code-base)
1. [Code Style](#code-style)

There are two primary ways to help:

* Using the issue tracker
* Changing the codebase

## Using the issue tracker

Use the issue tracker to suggest feature requests, report bugs, and ask questions. This is also a great way to connect with the developers of the project as well as others who are interested in this solution.

Use the issue tracker to find ways to contribute. Find a bug or a feature, mention in the issue that you will take on that effort, then follow the "Changing the codebase" guidance below.

## Changing the code-base

Generally speaking, you should fork this repository, make changes in your own fork, and then submit a pull-request. All new code should have been thoroughly tested end-to-end in order to validate implemented features and the presence or lack of defects.

### Working with forks

* [Configure this repository as a remote for your own fork](https://help.github.com/articles/configuring-a-remote-for-a-fork/)
* [Sync your fork with this repository](https://help.github.com/articles/syncing-a-fork/) before beginning to work on a new pull-request.

### Submitting your changes

Generally, we require that you test any code you are adding or modifying.
Once your changes are ready to submit for review:

1. Sign the Contributor License Agreement

   Please make sure you have signed our [Contributor License Agreement](https://www.elastic.co/contributor-agreement/).
   We are not asking you to assign copyright to us,
   but to give us the right to distribute your code without restriction.
   We ask this of all contributors in order to assure our users of the origin and continuing existence of the code.
   You only need to sign the CLA once.

2. Code your changes

   See [development guidelines of this project][apm-pipeline-library-development] for details.

3. Test your changes

   Run the test suite to make sure that nothing is broken.
   See [testing][apm-pipeline-library-testing] for details.

4. Rebase your changes

   Update your local repository with the most recent code from the main repo,
   and rebase your branch on top of the latest main branch.
   We prefer your initial changes to be squashed into a single commit.
   Later,
   if we ask you to make changes,
   add them as separate commits.
   This makes them easier to review.
   As a final step before merging we will either ask you to squash all commits yourself or we'll do it for you.
   Don't forget to correctly format your commit messages.

5. Submit a pull request

   Push your local changes to your forked copy of the repository and [submit a pull request](https://help.github.com/articles/using-pull-requests).
   In the pull request,
   choose a title which sums up the changes that you have made,
   and in the body provide more details about what your changes do.
   Also mention the number of the issue where discussion has taken place,
   eg "Closes #123".

6. Be patient

   We might not be able to review your code as fast as we would like to,
   but we'll do our best to dedicate it the attention it deserves.
   Your effort is much appreciated!

### Coding pattern

See [coding guidelines of this project][apm-pipeline-library-guidelines]


[apm-pipeline-library-guidelines]: GUIDELINES.md
[apm-pipeline-library-development]: DEVELOPMENT.md
[apm-pipeline-library-testing]: DEVELOPMENT.md#testing
