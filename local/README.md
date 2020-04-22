# Local Development for Jenkins CI

## System Requirements

- Docker >= 19.x.x
- Docker Compose >= 1.25.0
- Vagrant >= 2.2.4
- VirtualBox >= 6
- Vagrant plugins (`vagrant plugin install vagrant-vbguest vagrant-share vagrant-disksize`)
- Vault

## Jenkins linting

You can configure this jenkins instance as you wish, if so please change:

* configs/jenkins.yaml using the [JCasC](https://jenkins.io/projects/jcasc/)
* plugins.txt

The current baseline was based on what the @infra provides us.

### Bump jenkins core version

Please monitor https://github.com/elastic/infra/blob/master/docker/jenkins/docker-compose.yml#L5 if new changes then apply them in the Dockerfile.


### Add new plugins

Infra already provides the docker image with the installed plugins. If you need more plugins please change the local/configs/plugins.txt as you wish.


### Prerequisites

You'll need the following software installed and configured on your machine in
order to utilize the local Jenkins master:

- [Docker Compose](https://docs.docker.com/compose/install/)

- [HashiCorp Vault](https://www.vaultproject.io/docs/install/)

  After installation, authenticate with our secrets server by following the
  [directions](https://github.com/elastic/infra/blob/master/docs/vault/README.md#github-auth).

  You may also need to authenticate to the Elastic Docker repo. You can do so by visiting
  the [registry authentication page](https://github.com/elastic/infra/blob/master/docs/vault/README.md#github-auth).

- You can login at https://docker.elastic.co:7000 using GitHub OAuth to sign in. The [docker registry authentication page](https://github.com/elastic/infra/blob/master/docs/container-registry/accessing-the-docker-registry.md)

### APM Pipeline shared library

This particular Jenkins instance got the shared library loaded by default.

### Enable local worker

As simple as opening http://localhost:18080/computer/local/ then download http://localhost:18080/jnlpJars/agent.jar
and `java -jar agent.jar -jnlpUrl http://localhost:18080/computer/local/slave-agent.jnlp `

### Enable linux vagrant worker

As simple as caching the infra vagrant images, see https://github.com/elastic/infra/blob/master/docs/jenkins/testing-demo-builds-locally.md#adding-a-second-larger-disk-to-the-vagrant-vms
and

```bash
make start-linux-worker
open http://localhost:18080
```

### Enable windows vagrant worker

#### Windows 2019

It does require to open the UI and login.

```bash
# cd local/windows/windows-2019
vagrant up --provision

# wait for a few minutes...
open http://localhost:18080
```

#### Windows 2016

```bash
# cd local/windows/windows-2016
vagrant up --provision

# wait for a few minutes...
open http://localhost:18080
```

## Enable macosx vagrant worker

```bash
# cd local/macosx
vagrant up --provision
# wait for a few minutes...
open http://localhost:18080
```

> In the case `OpenSSL SSL_read: SSL_ERROR_SYSCALL` error appears, please run:
```bash
# add the vagrant box without SSL certificates
vagrant box add AndrewDryga/vagrant-box-osx --insecure
vagrant up --provision
# wait for a few minutes...
open http://localhost:18080
```

### Usage

#### Quickstart

1. Build docker image by running:

```bash
make build
```

2. Ensure you have access to Elastic's secrets infrastructure with Vault:
```bash
export VAULT_ADDR="https://secrets.elastic.co:8200"
export ELASTIC_SECRETS_SERVICE_TOKEN="<Your GitHub token for Vault>"
vault login -method github token="${ELASTIC_SECRETS_SERVICE_TOKEN}"
```

3. Start the local Jenkins master service by running:

```bash
make start
```

3. Browse to <http://localhost:18080> in your web browser.

When you're done, you can shut down all services by running:

```bash
make stop
```

Run `make help` for information on all available commands.

## FAQ

**Adding and validating a new JJBB job or folder to the local instance**

```shell
$ cd <PIPELINE_ROOT_DIR>
$ ./local/test-jjbb.sh -j .ci/jobs/file-to-add.yml
```
