build:
	mkdir -p build/test-results

.PHONY: test
test: build
	gotestsum --format short-verbose --junitfile $(PWD)/build/test-results/unit-tests.xml -- -coverprofile=cover.txt -count=1 ./...
