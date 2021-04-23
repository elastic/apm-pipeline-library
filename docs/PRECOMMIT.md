# Precommit

This particular process will help to evaluate some linting before committing any changes. Therefore you need the pre-commit.

## Installation

Follow <https://pre-commit.com/#install> and `pre-commit install`

Some hooks might require some extra tools such as:

* [shellcheck](https://github.com/koalaman/shellcheck#installing)
* [yamllint](https://yamllint.readthedocs.io/en/stable/quickstart.html)

## Enabled hooks

* Check case conflict
* Check executables have shebangs
* Check merge conflicts
* Check json
* Check yaml
* Check xml
* Check bash syntax
* Check broken links in markdown
* End-of-file-fixer
* Ensure neither abstract classes nor traits are used in the shared library.
* Ensure JsonSlurperClassic is used instead of non-serializable JsonSlurper.
* Jenkinsfile linter.
* yamllint
* shellcheck
* Detect unicode non-breaking space character U+00A0 aka M-BM-
* Remove unicode non-breaking space character U+00A0 aka M-BM-
* Detect the EXTREMELY confusing unicode character U+2013
* Remove the EXTREMELY confusing unicode character U+2013

### Validate JJBB files

If the local jenkins instance has been enabled then it's possible to validate whether the JJBB
files are healthy enough.

Prepare test environment by first changing to the local/ directory and running:

```bash
  make start
```

Logs for the running Jenkins instance can then be viewed if you wish by running:

```bash
  make logs
```

To run the JJBB locally you must ensure that you have an /etc/hosts entry which maps
`jenkins` to `localhost`.

To prepare to test most pipelines, you must first set up the APM jobs folder:

```bash
  sh local/test-jjbb.sh -j .ci/jobs/apm-shared.yml
```

```bash
  sh local/test-jjbb.sh -j .ci/jobs/apm-docker-images-pipeline.yml
```

Then open http://localhost:18080

Debugging can be made easier by passing `-ldebug` to `test-jbb.sh`.

## pre-commit-hooks

Observability robots hooks for <http://pre-commit.com/>

### Using these hooks

Add this to your `.pre-commit-config.yaml`

```yaml
    - repo: https://github.com/elastic/apm-pipeline-library
      rev: current
      hooks:
      -   id: check-bash-syntax
      -   id: check-abstract-classes-and-trait
      -   id: check-jsonslurper-class
      -   id: check-jenkins-pipelines
      -   id: check-unicode-non-breaking-spaces
      -   id: remove-unicode-non-breaking-spaces
      -   id: check-en-dashes
      -   id: remove-en-dashes
      -   id: check-gherkin-lint
```

### Available hooks

* check-bash-syntax - Check Shell scripts syntax correctness, requires bash
* check-abstract-classes-and-trait - Ensure neither abstract classes nor traits are used
* check-jsonslurper-class - Ensure JsonSlurperClassic is used instead of non-serializable JsonSlurper
* check-jenkins-pipelines - Check the syntax of the Jenkinsfiles, requires docker and jenkins up and running.
* check-unicode-non-breaking-spaces - Detect unicode non-breaking space character U+00A0 aka M-BM-
* remove-unicode-non-breaking-spaces - Remove unicode non-breaking space character U+00A0 aka M-BM-
* check-en-dashes - Detect the EXTREMELY confusing unicode character U+2013
* remove-en-dashes - Remove the EXTREMELY confusing unicode character U+2013
* check-gherkin-lint - Check Gherkin feature syntax correctness, requires docker.
* check-markdown-lint - Check markdown links targeting public URLs are not broken, requires docker.
