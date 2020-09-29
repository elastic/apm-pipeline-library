PYTEST_APM_VERSION = 0.0.1
PYTEST_APM_PKG = pytest-apm-$(PYTEST_APM_VERSION)
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
test: virtualenv
	source $(VENV)/bin/activate;\
	pytest;

## @help:coverage:Report coverage.
.PHONY: coverage
coverage: virtualenv
	source $(VENV)/bin/activate;\
	coverage run --source=apm -m pytest; \
	coverage report -m;

## @help:clean:Remove Python file artifacts.
.PHONY: clean
clean:
	@echo "+ $@"
	@find . -type f -name "*.py[co]" -delete
	@find . -type d -name "__pycache__" -delete
	@find . -name '*~' -delete
		-@rm -fr *.egg-info build dist $(VENV) bin

package: virtualenv
	source $(VENV)/bin/activate;\
	$(PYTHON) setup.py sdist bdist_wheel

release: bin/gh package
	$(GH) release create $(PYTEST_APM_PKG) dist/$(PYTEST_APM_PKG).tar.gz \
		--repo elastic/apm-pipeline-library \
		--title $(PYTEST_APM_PKG) \
		--notes "$(PYTEST_APM_PKG)"
