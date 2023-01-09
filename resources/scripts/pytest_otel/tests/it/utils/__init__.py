import pytest
import json
import time
import os
import socket
import subprocess

SPAN_KIND_INTERNAL = 1
SPAN_KIND_SERVER = 2

STATUS_CODE_OK = 1
STATUS_CODE_ERROR = 2


def is_portListening(host, port):
    """Check a port in a host is liostening"""
    a_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    location = (host, port)
    result_of_check = a_socket.connect_ex(location)
    if result_of_check == 0:
        return True
    else:
        return False


def getSize(filename):
    """return the size of a file"""
    if os.path.isfile(filename):
        st = os.stat(filename)
        return st.st_size
    else:
        return 0


def waitForFileContent(filename):
    """wait for a file has content"""
    while getSize(filename) < 1:
        time.sleep(5)
        subprocess.check_output(f"docker cp $(docker ps -q --filter expose=4317):/tmp/tests.json {filename}",
                                stderr=subprocess.STDOUT, shell=True)
    with open(filename, encoding='utf-8') as input:
        print(input.read())


def assertAttrKeyValue(attributes, key, value):
    """check the value of a key in attributes"""
    realValue = ''
    for attr in attributes:
        if attr['key'] == key:
            realValue = attr["value"]["stringValue"]
    assert realValue == value, f'attribute {key} is not {value}: {realValue}'


def assertTestSuit(span, outcome, status):
    """check attributes of a test suit span"""
    assert span["kind"] == SPAN_KIND_SERVER, f'span kind is not server: {span["kind"]}'
    assert span["status"]["code"] == status, f'status code is not {status}: {span["status"]["code"]}'
    if outcome is not None:
        assertAttrKeyValue(span["attributes"], 'tests.status', outcome)
    assert len(span["parentSpanId"]) == 0, f'parent span id is not empty: {span["parentSpanId"]}'
    return True


def assertSpan(span, name, outcome, status):
    """check attributes of a span"""
    assert span["kind"] == SPAN_KIND_INTERNAL, f'span kind is not internal: {span["kind"]}'
    assert span["status"]["code"] == status, f'status code is not {status}: {span["status"]["code"]}'
    assertAttrKeyValue(span["attributes"], 'tests.name', name)
    if outcome is not None:
        assertAttrKeyValue(span["attributes"], 'tests.status', outcome)
    assert len(span["parentSpanId"]) > 0, f'parent span id is empty: {span["parentSpanId"]}'
    return True


def assertTest(pytester, name, ts_outcome, ts_status, outcome, status):
    """check a test results are correct"""
    pytester.runpytest("--otel-endpoint=http://127.0.0.1:4317", "--otel-service-name=pytest_otel", "--otel-debug=True", "-rsx")
    filename = "./tests.json"
    waitForFileContent(filename)
    foundTest = False
    foundTestSuit = False
    with open(filename, encoding='utf-8') as input:
        spans_output = json.loads(input.readline())
        print(f"""
            spans_output {spans_output}
            resourceSpans {spans_output['resourceSpans']}
        """)
        for resourceSpan in spans_output['resourceSpans']:
            for instrumentationLibrarySpan in resourceSpan['scopeSpans']:
                for span in instrumentationLibrarySpan['spans']:
                    if span["name"] == f"Running {name}":
                        foundTest = assertSpan(span, name, outcome, status)
                    if span["name"] == "Test Suite":
                        foundTestSuit = assertTestSuit(span, ts_outcome, ts_status)
    assert foundTest or name is None, f'test {name} not found'
    assert foundTestSuit, 'test suit not found'
    os.remove(filename)
