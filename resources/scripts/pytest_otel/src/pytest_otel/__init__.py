# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

import logging
import os
import sys
import traceback

import _pytest._code
import _pytest.skipping
import pytest
from opentelemetry import trace
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor, SimpleSpanProcessor
from opentelemetry.sdk.trace.export.in_memory_span_exporter import InMemorySpanExporter
from opentelemetry.trace.propagation.tracecontext import TraceContextTextMapPropagator
from opentelemetry.trace.status import Status, StatusCode

LOGGER = logging.getLogger("pytest_otel")
service_name = None
traceparent = None
session_name = None
tracer = None
insecure = None
in_memory_span_exporter = False
otel_span_file_output = None
otel_exporter = None
spans = {}
outcome = None


def pytest_addoption(parser):
    """Init command line arguments"""
    group = parser.getgroup("pytest-otel", "report OpenTelemetry traces for tests executed.")

    group.addoption(
        "--endpoint",
        dest="endpoint",
        help="URL for the APM server.(OTEL_EXPORTER_OTLP_ENDPOINT)",
    )
    group.addoption(
        "--headers",
        dest="headers",
        help="Additional headers to send (i.e.: key1=value1,key2=value2).(OTEL_EXPORTER_OTLP_HEADERS)",  # noqa: E501
    )
    group.addoption(
        "--service-name",
        dest="service_name",
        default="Pytest_Otel_reporter",
        help="Name of the service.(OTEL_SERVICE_NAME)",
    )
    group.addoption(
        "--session-name",
        dest="session_name",
        default="Test Suite",
        help="Name for the Main span reported.",
    )
    group.addoption(
        "--traceparent",
        dest="traceparent",
        help="Trace parent.(TRACEPARENT) see https://www.w3.org/TR/trace-context-1/#trace-context-http-headers-format",  # noqa: E501
    )
    group.addoption(
        "--insecure",
        dest="insecure",
        default=False,
        help="Disables TLS.(OTEL_EXPORTER_OTLP_INSECURE)",
    )
    group.addoption(
        "--otel-span-file-output",
        dest="otel_span_file_output",
        default="./otel-traces-file-output.json",
        help="If the Otel endpoint is not set, the spans will be saved to a file (./otel-traces-file-output.txt)",
    )


def init_otel():
    """Init the OpenTelemetry settings"""
    global tracer, session_name, service_name, insecure, otel_exporter
    LOGGER.debug("Init Otel : {}".format(service_name))
    trace.set_tracer_provider(
        TracerProvider(
            resource=Resource.create({SERVICE_NAME: service_name}),
        )
    )

    if in_memory_span_exporter:
        otel_exporter = InMemorySpanExporter()
        trace.get_tracer_provider().add_span_processor(SimpleSpanProcessor(otel_exporter))
        otel_exporter.clear()
    else:
        otel_exporter = OTLPSpanExporter()
        trace.get_tracer_provider().add_span_processor(BatchSpanProcessor(otel_exporter))

    tracer = trace.get_tracer(session_name)


def start_span(span_name, context=None, kind=None):
    """Starts a span with the name, context, and kind passed as parameters"""
    global tracer, spans
    spans[span_name] = tracer.start_span(
        span_name, context=context, record_exception=True, set_status_on_exception=True,
        kind=kind
    )
    LOGGER.debug("The {} transaction start_span.".format(span_name))
    return spans[span_name]


def end_span(span_name, outcome):
    """Ends a span identified by its name"""
    global spans
    status = convertOutcome(outcome)
    spans[span_name].set_status(status)
    spans[span_name].set_attribute("test.outcome", outcome)
    spans[span_name].end()
    LOGGER.debug("The {} transaction ends. -> {}".format(span_name, status))
    return spans[span_name]


def convertOutcome(outcome):
    """Convert from pytest outcome to OpenTelemetry status code"""
    if outcome == "passed":
        return Status(status_code=StatusCode.OK)
    elif (outcome == "failed"
            or outcome == "interrupted"
            or outcome == "internal_error"
            or outcome == "usage_error"
            or outcome == "no_tests_collected"
        ):
        return Status(status_code=StatusCode.ERROR)
    else:
        return Status(status_code=StatusCode.UNSET)


def exitCodeToOutcome(exit_code):
    """convert pytest ExitCode to outcome"""
    if exit_code == 0:
        return "passed"
    elif exit_code == 1:
        return "failed"
    elif exit_code == 2:
        return "interrupted"
    elif exit_code == 3:
        return "internal_error"
    elif exit_code == 4:
        return "usage_error"
    elif exit_code == 4:
        return "no_tests_collected"
    else:
        return "failed"

def traceparent_context(traceparent):
    """Extracts the trace context from the TRACEPARENT passed"""
    carrier = {}
    carrier["traceparent"] = traceparent
    return TraceContextTextMapPropagator().extract(carrier=carrier)


def pytest_sessionstart(session):
    """Uses the commandline parameter to define the environment variables used by OpenTelemetry"""
    global service_name, traceparent, session_name, insecure, in_memory_span_exporter, otel_span_file_output
    LOGGER.setLevel(logging.DEBUG)
    config = session.config
    service_name = config.getoption("service_name")
    session_name = config.getoption("session_name")
    traceparent = config.getoption("traceparent")
    endpoint = config.getoption("endpoint")
    headers = config.getoption("headers")
    insecure = config.getoption("insecure")
    if endpoint is not None:
        os.environ["OTEL_EXPORTER_OTLP_ENDPOINT"] = endpoint
    if headers is not None:
        os.environ["OTEL_EXPORTER_OTLP_HEADERS"] = headers
    if service_name is not None:
        os.environ["OTEL_SERVICE_NAME"] = service_name
    if insecure is not None:
        os.environ["OTEL_EXPORTER_OTLP_INSECURE"] = f'{insecure}'
    if traceparent is None:
        traceparent = os.getenv("TRACEPARENT", None)
    if len(os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "")) == 0:
        in_memory_span_exporter = True
        otel_span_file_output = config.getoption("otel_span_file_output")
    init_otel()
    span = start_span(session_name, traceparent_context(traceparent), trace.SpanKind.SERVER)


def pytest_runtest_setup(item):  # noqa: U100
    """Clean the global outcome on every test"""
    global outcome
    outcome = None


def pytest_report_teststatus(report):
    """Set the final outcome to the reported outcome"""
    global outcome
    outcome = report.outcome


def pytest_sessionfinish(session, exitstatus):  # noqa: U100
    """Ends the parent Opentelemetry span with the session outcome"""
    global session_name, outcome, in_memory_span_exporter, otel_exporter
    LOGGER.debug("Session transaction Ends")
    end_span(session_name, exitCodeToOutcome(exitstatus))
    LOGGER.debug("in_memory_span_exporter {}".format(in_memory_span_exporter))
    if in_memory_span_exporter:
        print()
        print("Using on memory OpenTelemetry exporter")
        span_list = otel_exporter.get_finished_spans()
        print("Number of spans: {}".format(len(span_list)))
        json = "[\n"
        for i in range(len(span_list)):
            if i > 0:
                json += ","
            json += span_list[i].to_json()
        json += "\n]\n"
        with open(otel_span_file_output, 'w', encoding='utf-8') as output:
            output.write(json)
        print(json)

@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_call(item):
    global outcome, session_name, spans
    with tracer.start_as_current_span(
        "Running {}".format(item.name),
        context=trace.set_span_in_context(spans[session_name]),
        record_exception=True,
        set_status_on_exception=True,
    ) as span:
        LOGGER.debug("Test {} starts - {}".format(item.name, span.get_span_context()))
        span.set_attribute("test.name", item.name)
        yield
        LOGGER.debug("Test {} ends - {}".format(item.name, span.get_span_context()))

        if hasattr(sys, "last_value") and hasattr(sys, "last_traceback") and hasattr(sys, "last_type"):
            longrepr = ""
            outcome = ""
            if not isinstance(sys.last_value, _pytest._code.ExceptionInfo):
                outcome = "failed"
                longrepr = sys.last_value
            elif isinstance(sys.last_value, _pytest._code.skip.Exception):
                outcome = "skipped"
                r = sys.last_value._getreprcrash()
                longrepr = (str(r.path), r.lineno, r.message)
            else:
                outcome = "failed"
                style = item.config.getoption("tbstyle", "auto")
                longrepr = item._repr_failure_py(sys.last_value, style=style)
            LOGGER.debug("test.outcome {}".format(outcome))
            LOGGER.debug("test.longrepr {}".format(longrepr))
            LOGGER.debug("test.last_value {}".format(sys.last_value))
            stack_trace = repr(traceback.format_exception(sys.last_type, sys.last_value, sys.last_traceback))
            span.set_attribute("test.stack_trace", "{}".format(stack_trace))
            span.set_attribute("test.error", "{}".format(sys.last_value.args[0]))
            span.set_attribute("test.last_value", "{}".format(sys.last_value))
            span.set_attribute("test.last_type", "{}".format(sys.last_type))

            xfailed = item._store.get(_pytest.skipping.xfailed_key, None)
            if xfailed:
                span.set_attribute("test.xfailed", "{}".format(xfailed.reason))
        status = convertOutcome(outcome)
        span.set_status(status)
        span.set_attribute("test.outcome", "{}".format(outcome))
