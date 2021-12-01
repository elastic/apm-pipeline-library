# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

PYTEST_OTEL_VERSION = 0.0.1
PYTEST_OTEL_PKG = pytest-otel-$(PYTEST_OTEL_VERSION)
OTEL_EXPORTER_OTLP_ENDPOINT ?= http://127.0.0.1:4317
OTEL_SERVICE_NAME ?= "pytest_otel_test"
OTEL_EXPORTER_OTLP_INSECURE ?= True
OTEL_EXPORTER_OTLP_HEADERS ?=
TRACEPARENT ?=

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
	OTEL_EXPORTER_OTLP_ENDPOINT="$(OTEL_EXPORTER_OTLP_ENDPOINT)" \
	OTEL_SERVICE_NAME="$(OTEL_SERVICE_NAME)" \
	OTEL_EXPORTER_OTLP_INSECURE="$(OTEL_EXPORTER_OTLP_INSECURE)" \
	OTEL_EXPORTER_OTLP_HEADERS="$(OTEL_EXPORTER_OTLP_HEADERS)" \
	TRACEPARENT="$(TRACEPARENT)" \
	source $(VENV)/bin/activate;\
	pytest --capture=no;

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
	-@rm -fr *.egg-info build dist $(VENV) bin .tox .mypy_cache .pytest_cache otel-traces-file-output.json test_spans.json temp

package: virtualenv
	source $(VENV)/bin/activate;\
	$(PYTHON) setup.py sdist bdist_wheel

release: bin/gh package
	$(GH) release create $(PYTEST_OTEL_PKG) dist/$(PYTEST_OTEL_PKG).tar.gz \
		--repo elastic/apm-pipeline-library \
		--title $(PYTEST_OTEL_PKG) \
		--notes "$(PYTEST_OTEL_PKG)"

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
	set +xe\
	source $(VENV)/bin/activate;\
	python -m pip install --user twine;\
	python setup.py sdist;\
	echo "Uploading to $${REPO_URL} with user $${TWINE_USER}";\
	python -m twine upload --username "$${TWINE_USER}" --password "$${TWINE_PASSWORD}" --skip-existing --repository-url $${REPO_URL} dist/*.tar.gz;\
	python -m twine upload --username "$${TWINE_USER}" --password "$${TWINE_PASSWORD}" --skip-existing --repository-url $${REPO_URL} wheelhouse/*.whl
