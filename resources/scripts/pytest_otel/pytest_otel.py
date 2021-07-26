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
import traceback

import _pytest._code
import _pytest.skipping
import pytest
import logging
import sys
import os
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.trace.status import Status, StatusCode
from opentelemetry.sdk.trace.export import BatchSpanProcessor


LOGGER = logging.getLogger("pytest_otel")
service_name = None
traceparent = None
session_name = None
has_otel = True
tracer = None
insecure = None
spans = dict()


def pytest_addoption(parser):
    group = parser.getgroup(
        "pytest-otel", "report OpenTelemetry traces for tests executed."
    )

    group.addoption('--endpoint',
                    dest='endpoint',
                    help='URL for the APM server.(OTEL_EXPORTER_OTLP_ENDPOINT)')
    group.addoption('--headers',
                    dest='headers',
                    help='Additional headers to send (i.e.: key1=value1,key2=value2).(OTEL_EXPORTER_OTLP_HEADERS)')
    group.addoption('--service-name',
                    dest='service_name',
                    default='Pytest_Otel_reporter',
                    help='Name of the service.(OTEL_SERVICE_NAME)')
    group.addoption('--session-name',
                    dest='session_name',
                    default='Test Suit',
                    help='Name for the Main span reported.')
    group.addoption('--traceparent',
                    dest='traceparent',
                    help='Trace parent.(TRACEPARENT) see https://www.w3.org/TR/trace-context-1/#trace-context-http-headers-format')
    group.addoption('--insecure',
                    dest='insecure',
                    default=False,
                    help='Disables TLS.(OTEL_EXPORTER_OTLP_INSECURE)')


def init_otel():
    global tracer, session_name, service_name, insecure
    LOGGER.debug('Init Otel : {}'.format(service_name))
    trace.set_tracer_provider(
        TracerProvider(
            resource=Resource.create({SERVICE_NAME: service_name}),
        )
    )

    otel_exporter = OTLPSpanExporter(insecure=insecure)

    trace.get_tracer_provider().add_span_processor(
        BatchSpanProcessor(otel_exporter)
    )

    tracer = trace.get_tracer(session_name)


def start_span(span_name):
    global tracer, spans
    spans[span_name] = tracer.start_span(span_name,
                                         record_exception=True,
                                         set_status_on_exception=True)
    LOGGER.debug('The {} transaction start_span.'.format(span_name))
    return spans[span_name]


def end_span(span_name, outcome):
    global spans
    status = convertOutcome(outcome)
    spans[span_name].set_status(status)
    spans[span_name].set_attribute('test.outcome', outcome)
    spans[span_name].end()
    LOGGER.debug('The {} transaction ends. -> {}'.format(span_name, status))
    return spans[span_name]


def convertOutcome(outcome):
    if outcome == 'passed':
        return Status(status_code=StatusCode.OK)
    elif outcome == 'failed':
        return Status(status_code=StatusCode.ERROR)
    else:
        return Status(status_code=StatusCode.UNSET)


def pytest_sessionstart(session):
    global service_name, traceparent, session_name, insecure
    LOGGER.setLevel(logging.DEBUG)
    config = session.config
    service_name = config.getoption("service_name")
    session_name = config.getoption("session_name")
    traceparent = config.getoption("traceparent")
    endpoint = config.getoption("endpoint")
    headers = config.getoption("headers")
    insecure = config.getoption("insecure")
    if endpoint:
        os.environ['OTEL_EXPORTER_OTLP_ENDPOINT'] = endpoint
    if headers:
        os.environ['OTEL_EXPORTER_OTLP_HEADERS'] = headers
    if service_name:
        os.environ['OTEL_SERVICE_NAME'] = service_name
    if insecure:
        os.environ['OTEL_EXPORTER_OTLP_INSECURE'] = insecure
    if traceparent:
        if headers:
            os.environ['OTEL_EXPORTER_OTLP_HEADERS'] = "traceparent: {},{}".format(traceparent, headers)
        else:
            os.environ['OTEL_EXPORTER_OTLP_HEADERS'] = "traceparent: {}".format(traceparent)
    if has_otel:
        init_otel()
        start_span(session_name)


def pytest_runtest_setup(item):
    global outcome
    outcome = None


def pytest_report_teststatus(report):
    global outcome
    outcome = report.outcome


def pytest_sessionfinish(session, exitstatus):
    global has_otel, session_name
    if has_otel:
        LOGGER.debug('Session transaction Ends')
        end_span(session_name, outcome)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_call(item):
    global has_otel, outcome, session_name, spans
    if has_otel:
        with tracer.start_as_current_span('Running {}'.format(item.name),
                                          context=trace.set_span_in_context(spans[session_name]),
                                          record_exception=True,
                                          set_status_on_exception=True
                                          ) as span:
            LOGGER.debug('Test {} starts - {}'.format(item.name, span.get_span_context()))
            span.set_attribute('test.name', item.name)
            yield
            LOGGER.debug('Test {} ends - {}'.format(item.name, span.get_span_context()))

            if hasattr(sys, "last_value") and hasattr(sys, "last_traceback") and hasattr(sys, "last_type"):
                longrepr = ''
                outcome = ''
                if not isinstance(sys.last_value, _pytest._code.ExceptionInfo):
                    outcome = "failed"
                    longrepr = sys.last_value
                elif isinstance(sys.last_value, _pytest._code.skip.Exception):
                    outcome = "skipped"
                    r = sys.last_value._getreprcrash()
                    longrepr = (str(r.path), r.lineno, r.message)
                else:
                    outcome = "failed"
                    longrepr = item._repr_failure_py(
                        sys.last_value, style=item.config.getoption("tbstyle", "auto")
                    )
                LOGGER.debug('test.outcome {}'.format(outcome))
                LOGGER.debug('test.longrepr {}'.format(longrepr))
                LOGGER.debug('test.last_value {}'.format(sys.last_value))
                stack_trace = repr(traceback.format_exception(sys.last_type, sys.last_value, sys.last_traceback))
                span.set_attribute('test.stack_trace', "{}".format(stack_trace))
                span.set_attribute('test.error', "{}".format(sys.last_value.args[0]))
                span.set_attribute('test.last_value', "{}".format(sys.last_value))
                span.set_attribute('test.last_type', "{}".format(sys.last_type))

                xfailed = item._store.get(_pytest.skipping.xfailed_key, None)
                if xfailed:
                    span.set_attribute('test.xfailed', "{}".format(xfailed.reason))
            status = convertOutcome(outcome)
            span.set_status(status)
            span.set_attribute('test.outcome', "{}".format(outcome))
