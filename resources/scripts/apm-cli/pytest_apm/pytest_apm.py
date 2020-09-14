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

import pytest
import logging
import json
import elasticapm

LOGGER = logging.getLogger("pytest_apm")

# change the way transactions are generates
# 'dt' -> use distributing tracing to set the a Session parane transaction and every test has his own transaction and span.
# None -> There is only one transaction at Session level and every test is a span.
apm_mode = None
apm_cli = None
apm_server_url = None
apm_token = None
apm_api_key = None
apm_service_name = None
apm_custom_context = None
apm_parent_id = None
apm_outcome = None
apm_session_name = None
apm_labels = None


def pytest_addoption(parser):
    group = parser.getgroup(
        "pytest-apm", "report APM traces about test executed."
    )

    group.addoption('--apm-server-url',
                    dest='apm_server_url',
                    help='URL for the APM server.')
    group.addoption('--apm-token',
                    dest='apm_token',
                    help='Token to access to APM server.')
    group.addoption('--apm-api-key',
                    dest='apm_api_key',
                    help='API key to access to APM server.')
    group.addoption('--apm-service-name',
                    dest='apm_service_name',
                    help='Name of the service.')
    group.addoption('--apm-custom-context',
                    dest='apm_custom_context',
                    help='Custom context for the current transaction in a JSON string.')
    group.addoption('--apm-labels',
                    dest='apm_labels',
                    default='{}',
                    help='Labels to add to every transaction and span.')
    group.addoption('--apm-session-name',
                    dest='apm_session_name',
                    default='Session',
                    help='Name for the Main transaction reported to APM.')


def begin_transaction(transaction_name):
    global apm_cli, apm_parent_id
    if apm_parent_id:
        parent = elasticapm.trace_parent_from_string(apm_parent_id)
        transaction = apm_cli.begin_transaction(transaction_name, trace_parent=parent)
        LOGGER.debug('Parent transaction : {}'.format(apm_parent_id))
    else:
        transaction = apm_cli.begin_transaction(transaction_name)
    LOGGER.debug('The {} transaction begins.'.format(transaction_name))
    return transaction


def end_transaction(transaction_name, transaction_result='success'):
    global apm_cli
    transaction = apm_cli.end_transaction(transaction_name, transaction_result)
    LOGGER.debug('The {} transaction ends. -> {}'.format(transaction_name, transaction_result))
    return transaction


def begin_span(span_name, span_type, labels=None):
    with elasticapm.capture_span(span_name,
                                 span_type=span_type,
                                 span_action='begin',
                                 labels=labels) as span:
        LOGGER.debug('The {}/{} span begins.'.format(span_name, span_type))
        LOGGER.debug('The {}/{} span ends.'.format(span_name, span_type))
        return span


def set_context(data, key="custom"):
    if data:
        elasticapm.set_context(json.loads(data), key)


def get_parent_id():
    """
    :return: returns the parent ID of the current transaction.
    """
    return "{:02x}-{}-{}-{:02x}".format(0, elasticapm.get_trace_id(), elasticapm.get_span_id(), 1)


def pytest_sessionstart(session):
    global apm_cli, apm_server_url, apm_token, apm_api_key, apm_service_name, apm_custom_context, \
        apm_parent_id, apm_session_name, apm_labels
    LOGGER.setLevel(logging.DEBUG)
    config = session.config
    apm_server_url = config.getoption("apm_server_url", default=None)
    apm_token = config.getoption("apm_token", default=None)
    apm_api_key = config.getoption("apm_api_key", default=None)
    apm_service_name = config.getoption("apm_service_name", default=None)
    apm_custom_context = config.getoption("apm_custom_context", default=None)
    apm_session_name = config.getoption("apm_session_name")
    apm_labels = json.loads(config.getoption("apm_labels"))
    apm_cli = init_apm_client()
    if apm_cli:
        LOGGER.debug("Session transaction starts.")
        elasticapm.instrument()
        begin_transaction(apm_session_name)
        elasticapm.label(**apm_labels)
        set_context(apm_custom_context)
        if apm_mode == 'dt':
            with elasticapm.capture_span('Start session', labels=apm_labels):
                apm_parent_id = get_parent_id()
            # FIXME it is need to end the transaction to allow child transaction,
            #  if we do not end it the session transaction is not show in the UI.
            end_transaction(apm_session_name)


def init_apm_client():
    global apm_server_url, apm_token, apm_api_key, apm_service_name, apm_custom_context, apm_parent_id
    LOGGER.debug("init_apm_client")
    if apm_server_url:
        if not (apm_token or apm_api_key) or not apm_service_name:
            pytest.fail("""
            APM server URL, APM service name, and an TOKEN or API Key are required to connect to the APM service.
            --apm-server-url https://apm.example.com:8200 --apm-token a51bfe6c --apm-service-name my_service
            or
             --apm-server-url https://apm.example.com:8200 --apm-api-key 3398579f385ea51bfe6cb2183546931d --apm-service-name my_service
            """)  # noqa E501
            raise
        apm_client_local = None
        if apm_token:
            apm_client_local = elasticapm.Client(service_name=apm_service_name,
                                                 server_url=apm_server_url,
                                                 verify_server_cert=False,
                                                 secret_token=apm_token,
                                                 use_elastic_traceparent_header=True,
                                                 debug=True)
        elif apm_api_key:
            apm_client_local = elasticapm.lient(service_name=apm_service_name,
                                                server_url=apm_server_url,
                                                verify_server_cert=False,
                                                api_key=apm_api_key,
                                                use_elastic_traceparent_header=True,
                                                debug=True)
        return apm_client_local


def pytest_runtest_setup(item):
    global apm_cli, apm_mode, apm_parent_id, apm_outcome
    apm_outcome = None
    if apm_cli and apm_mode == 'dt':
        LOGGER.debug("pytest_runtest_setup-{}-{}".format(item.name, apm_parent_id))
        begin_transaction(item.name)


def pytest_report_teststatus(report):
    global apm_outcome
    apm_outcome = report.outcome


def pytest_runtest_teardown(item, nextitem):
    global apm_cli, apm_mode, apm_parent_id, apm_outcome
    if apm_cli and apm_mode == 'dt':
        LOGGER.debug("pytest_runtest_teardown-{}-{}".format(apm_outcome, apm_parent_id))
        end_transaction(item.name, apm_outcome)


def pytest_sessionfinish(session, exitstatus):
    # FIXME the session does not have the duration of all test
    #  because it is not possible to end the transaction at this point.
    global apm_cli, apm_session_name
    if apm_cli:
        LOGGER.debug('Session transaction Ends')
        end_transaction(apm_session_name, apm_outcome)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_call(item):
    global apm_cli, apm_labels
    if apm_cli:
        with elasticapm.capture_span('Running {}'.format(item.name), labels=apm_labels):
            LOGGER.debug('Test {} begins'.format(item.name))
            yield
            LOGGER.debug('Test {} ends'.format(item.name))
