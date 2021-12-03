# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

OTEL_EXPORTER_OTLP_ENDPOINT ?= http://127.0.0.1:4317
OTEL_SERVICE_NAME ?= "pytest_otel_test"
OTEL_EXPORTER_OTLP_INSECURE ?= True
OTEL_EXPORTER_OTLP_HEADERS ?=
TRACEPARENT ?=
REPO_URL ?= https://upload.pypi.org/legacy/

VENV ?= .venv
PYTHON ?= python3
PIP ?= pip3
GH_VERSION = 1.0.0

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	OS_FLAG := linux
endif
ifeq ($(UNAME_S),Darwin)
	OS_FLAG := macOS
endif
UNAME_P := $(shell uname -m)
ifeq ($(UNAME_P),x86_64)
	ARCH_FLAG := amd64
endif
ifneq ($(filter %86,$(UNAME_P)),)
	ARCH_FLAG := i386
endif
GH_BINARY = gh_$(GH_VERSION)_$(OS_FLAG)_$(ARCH_FLAG)
GH = $(CURDIR)/bin/gh

.SILENT:

.PHONY: help
help:
	@echo "Targets:"
	@echo ""
	@grep '^## @help' Makefile|cut -d ":" -f 2-3|( (sort|column -s ":" -t) || (sort|tr ":" "\t") || (tr ":" "\t"))

bin:
	mkdir bin

bin/gh: bin
	curl -sSfL https://github.com/cli/cli/releases/download/v$(GH_VERSION)/$(GH_BINARY).tar.gz|tar xz
	mv $(GH_BINARY)/bin/gh bin/gh
	rm -fr $(GH_BINARY)

## @help:virtualenv:Create a Python virtual environment.
.PHONY: virtualenv
virtualenv:
	$(PYTHON) --version
	test -d $(VENV) || virtualenv -q --python=$(PYTHON) $(VENV);\
	source $(VENV)/bin/activate;\
	$(PIP) install -q -r requirements.txt;

## @help:install:Install APM CLI in a Python virtual environment.
.PHONY: install
install: virtualenv
	source $(VENV)/bin/activate;\
	$(PIP) install .;

## @help:test:Run the test.
.PHONY: test
test: virtualenv install
	source $(VENV)/bin/activate;\
	pytest --capture=no -p pytester --runpytest=subprocess \
		--junitxml $(CURDIR)/junit-test_pytest_otel.xml \
		tests/test_pytest_otel.py;

## @help:test:Run the test.
.PHONY: test
it-test: virtualenv install
	set -e;\
	source $(VENV)/bin/activate;\
	for test in tests/it/test_*.py; \
	do \
		pytest --capture=no -p pytester --runpytest=subprocess \
			--junitxml $(CURDIR)/junit-$$(basename $${test}).xml \
			$${test}; \
	done;
	#pytest --capture=no -p pytester --runpytest=subprocess tests/it/test_*.py;

## @help:coverage:Report coverage.
.PHONY: coverage
coverage: virtualenv
	source $(VENV)/bin/activate;\
	coverage run --source=otel -m pytest; \
	coverage report -m;

## @precomit:pre-commit:Run precommit hooks.
lint: virtualenv
	source $(VENV)/bin/activate;\
	pre-commit run; \
	mypy src/pytest_otel; \
	mypy tests;

## @help:clean:Remove Python file artifacts.
.PHONY: clean
clean:
	@echo "+ $@"
	@find . -type f -name "*.py[co]" -delete
	@find . -type d -name "__pycache__" -delete
	@find . -name '*~' -delete
	-@rm -fr *.egg-info build dist $(VENV) bin .tox .mypy_cache .pytest_cache otel-traces-file-output.json test_spans.json temp junit-*.xml

package: virtualenv
	source $(VENV)/bin/activate;\
	set +xe; \
	$(PYTHON) setup.py sdist bdist_wheel

## @help:run-otel-collector:Run OpenTelemetry collector in debug mode.
.PHONY: run-otel-collector
run-otel-collector:
	mkdir -p "$(CURDIR)/temp"
	docker run --rm -p 4317:4317 -u "$(id -u):$(id -g)" \
	-v "$(CURDIR)/temp:/tmp" \
	-v "$(CURDIR)/tests/otel-collector.yaml":/otel-config.yaml \
	--name otelcol otel/opentelemetry-collector \
	--config otel-config.yaml; \

#https://upload.pypi.org/legacy/
#https://test.pypi.org/legacy/
#secret/apm-team/ci/apm-agent-python-pypi-test
#secret/apm-team/ci/apm-agent-python-pypi-prod
## @help:publish REPO_URL=${REPO_URL} TWINE_USER=${TWINE_USER} TWINE_PASSWORD=${TWINE_PASSWORD}:Publish the Python project in a PyPI repository.
.PHONY: publish
publish: package
	set +xe; \
	source $(VENV)/bin/activate;\
	$(PYTHON) -m pip install twine;\
	echo "Uploading to $${REPO_URL} with user $${TWINE_USER}";\
	python -m twine upload --username "$${TWINE_USER}" --password "$${TWINE_PASSWORD}" --skip-existing --repository-url $${REPO_URL} dist/*.tar.gz;\
	python -m twine upload --username "$${TWINE_USER}" --password "$${TWINE_PASSWORD}" --skip-existing --repository-url $${REPO_URL} dist/*.whl
