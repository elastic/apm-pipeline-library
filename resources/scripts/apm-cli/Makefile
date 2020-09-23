VENV ?= .venv
PYTHON ?= python3
PIP ?= pip3

.SILENT:

.PHONY: help
help:
	@echo "Targets:"
	@echo ""
	@grep '^## @help' Makefile|cut -d ":" -f 2-3|( (sort|column -s ":" -t) || (sort|tr ":" "\t") || (tr ":" "\t"))

## @help:virtualenv:Create a Python virtual environment.
.PHONY: virtualenv
virtualenv:
	$(PYTHON) --version
	test -d $(VENV) || virtualenv -q --python=$(PYTHON) $(VENV);\
	source $(VENV)/bin/activate;\
	$(PIP) install -r requirements.txt;

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
clean-pyc:
	@echo "+ $@"
	@find . -type f -name "*.py[co]" -delete
	@find . -type d -name "__pycache__" -delete
	@find . -name '*~' -delete
