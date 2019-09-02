VAULT_TOKEN ?= $(shell cat $${HOME}/.vault-token)
PAT_USER ?= pat
LOG_LEVEL ?= CRITICAL
.DEFAULT_GOAL := help

.PHONY: build
build:  ## Build the jenkins image
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			build jenkins

.PHONY: clean
clean:  ## Reset the environment; stop Jenkins
clean:
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			down --volumes

.PHONY: logs
logs:  ## Follow the log output for all Jenkins-related processes
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			logs --follow $${SERVICE}

# Start the local Jenkins master (used as dependency target)
.PHONY: master
master:
	@env \
		VAULT_TOKEN=${VAULT_TOKEN} \
		PAT_USER=${PAT_USER} \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			up --detach --no-recreate jenkins

.PHONY: pull
pull:  ## Pull required images for all Jenkins-related processes
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			pull $${SERVICE}

.PHONY: restart
restart:  ## Restart the local Jenkins master and related processes
	@env \
		VAULT_TOKEN=${VAULT_TOKEN} \
		PAT_USER=${PAT_USER} \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			restart $${SERVICE}

.PHONY: shell
shell:  ## Open a Bash shell on the local Jenkins master
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			exec $${SERVICE:-jenkins} sh

.PHONY: start
start:  ## Start the local Jenkins master
start: master
	@>&2 printf '\n'
	@>&2 printf 'Jenkins master now running at http://localhost:18080\n'

.PHONY: status
status:  ## Display the current status of Jenkins-related processes
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			ps $${SERVICE}

.PHONY: stop
stop:  ## Stop the local Jenkins master and related processes
	@env \
		docker-compose \
			--log-level ${LOG_LEVEL} \
			--file docker-compose.yml \
			stop $${SERVICE}

.PHONY: help
help:  ## Show this help message
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make \033[36m<target>\033[0m\n\nTargets:\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)
