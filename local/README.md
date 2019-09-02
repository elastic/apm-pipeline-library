# Jenkins linting

You can configure this jenkins instance as you wish, if so please change:

* configs/jenkins.yaml using the [JCasC](https://jenkins.io/projects/jcasc/)
* plugins.txt


The current baseline was based on what the @infra provides us.

You can use the [plugins.txt](https://github.com/elastic/infra/blob/master/docker/jenkins/configs/plugins.txt) as a reference.


## APM Pipeline shared library

This particular Jenkins instance got the shared library loaded by default.

## Enable worker

As simple as opening http://localhost:18080/computer/local/ then download http://localhost:18080/jnlpJars/agent.jar
and `java -jar agent.jar -jnlpUrl http://localhost:18080/computer/local/slave-agent.jnlp `


## Usage

### Quickstart

0. Build docker image by running:

   ```
   make build
   ```

1. Start the local Jenkins master service by running:

   ```
   make start
   ```

2. Specify the PAT user

   > Setting the `$PAT_USER` environment variable with your desired
   > username ahead of time.
   > For example:
   >
   > ```
   > env PAT_USER=v1v make start
   > ```

3. Browse to <http://localhost:18080> in your web browser.

When you're done, you can shut down all services by running:

    make stop

Run `make help` for information on all available commands.
