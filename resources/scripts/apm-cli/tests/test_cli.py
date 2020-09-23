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

# coding=utf-8
"""CLI feature tests."""
import os

import pytest
from apm.ApmCli import ApmCli
from pytest_bdd import scenario, given, when, then, parsers
from unittest.mock import patch, MagicMock

@pytest.fixture
def apm_cli(cmd_args):
    return ApmCli(cmd_args)


@pytest.fixture
def cmd_args():
    return []

@pytest.fixture
def parent_id_file(tmpdir):
    d = tmpdir.mkdir("apmCli")
    fh = d.join("parent_id.txt")
    return fh


@scenario('features/cli_params.feature', 'Basic transaction')
def test_basic_transaction():
    """Basic transaction."""
    pass


@scenario('features/cli_params.feature', 'Basic transaction and span')
def test_basic_transaction_and_span():
    """Basic transaction and span."""
    pass


@scenario('features/cli_params.feature', 'Basic transaction with api key')
def test_basic_transaction_with_api_key():
    """Basic transaction with api key."""
    pass


@scenario('features/cli_params.feature', 'Basic transaction with custom context')
def test_basic_transaction_with_custom_context():
    """Basic transaction with custom context."""
    pass


@scenario('features/cli_params.feature', 'Basic transaction with a transaction result')
def test_basic_transaction_with_transaction_result():
    """Basic transaction with a transaction result."""
    pass


@pytest.mark.xfail
@scenario('features/cli_params.feature', 'Missing APM config Command line parameter')
def test_missing_apm_config_command_line_parameter():
    """Missing APM config Command line parameter."""
    pass


@pytest.mark.xfail
@scenario('features/cli_params.feature', 'Missing service name Command line parameter')
def test_missing_service_name_command_line_parameter():
    """Missing service name Command line parameter."""
    pass


@pytest.mark.xfail
@scenario('features/cli_params.feature', 'Missing transaction Command line parameter')
def test_missing_transaction_command_line_parameter():
    """Missing transaction Command line parameter."""
    pass


@pytest.mark.xfail
@scenario('features/cli_params.feature', 'Api Key and Token Command line parameters passed')
def test_api_key_and_token_command_line_parameter_passed():
    """Api Key and Token Command line parameters passed."""
    pass


@scenario('features/cli_params.feature', 'Set parent transaction ID')
def test_set_parent_id():
    """Set parent transaction ID."""
    pass


@scenario('features/cli_params.feature', 'Load parent transaction ID')
def testload_parent_id():
    """Load parent transaction ID."""
    pass


@scenario('features/cli_params.feature', 'Save parent transaction ID')
def test_save_parent_id():
    """Save parent transaction ID."""
    pass


@given("an APM server URL")
def set_apm_url(cmd_args):
    cmd_args.append('--apm-server-url')
    cmd_args.append('https://apm.example.com:8200')


@given("a token")
def set_apm_token(cmd_args):
    cmd_args.append('--apm-token')
    cmd_args.append('token_example')


@given("a api key")
def set_apm_token(cmd_args):
    cmd_args.append('--apm-api-key')
    cmd_args.append('api_key_example')


@given("a service name")
def set_service_name(cmd_args):
    cmd_args.append('--service-name')
    cmd_args.append('example_svc')


@given("a transaction name")
def set_transaction_name(cmd_args):
    cmd_args.append('--transaction-name')
    cmd_args.append('transaction_test')


@given("a span name")
def set_spann_name(cmd_args):
    cmd_args.append('--span-name')
    cmd_args.append('span_test')


@given("a span command")
def set_span_command(cmd_args):
    cmd_args.append('--span-command')
    cmd_args.append('echo hello')


@given("a span type")
def set_span_type(cmd_args):
    cmd_args.append('--span-type')
    cmd_args.append('span_type')


@given("a span subtype")
def set_span_subtype(cmd_args):
    cmd_args.append('--span-subtype')
    cmd_args.append('span_subtype')


@given("a span action")
def set_span_action(cmd_args):
    cmd_args.append('--span-action')
    cmd_args.append('span_action')


@given("a span label")
def set_span_label(cmd_args):
    cmd_args.append('--span-labels')
    cmd_args.append('{"label": "foo"}')


@given("a custom context")
def set_span_command(cmd_args):
    cmd_args.append('--custom-context')
    cmd_args.append('{"var": "foo"}')


@given(parsers.parse("a transaction result {result:S}"))
def set_span_command(cmd_args, result):
    cmd_args.append('--transaction-result')
    cmd_args.append(result)


@given("a file to save the parent transaction ID")
def set_save_parent_id(cmd_args, parent_id_file):
    cmd_args.append('--parent-transaction-save')
    filename = os.path.join(parent_id_file.dirname, parent_id_file.basename)
    cmd_args.append(filename)


@given("a file to load the parent transaction ID")
def set_save_parent_id(cmd_args, parent_id_file):
    cmd_args.append('--parent-transaction-load')
    filename = os.path.join(parent_id_file.dirname, parent_id_file.basename)
    cmd_args.append(filename)
    parent_id_file.write('01-1234567890-00')


@given("a parent transaction ID")
def set_save_parent_id(cmd_args):
    cmd_args.append('--parent-transaction')
    cmd_args.append('01-1234567890-00')


@when("I launch the apm-cly.py")
def launch_cli(apm_cli):
    # maybe we can use ELASTIC_APM_DISABLE_SEND instead
    mock_urlopen_patcher = patch('elasticapm.transport.http.Transport.send')
    mock_urlopen = mock_urlopen_patcher.start()
    mock_urlopen.return_value.status = 200
    mock_urlopen.return_value.read = 'body'

    apm_cli.run()


@then("a transaction is reported")
def check_transaction(apm_cli):
    assert apm_cli.transaction


@then("a span is reported")
def check_span(apm_cli):
    assert apm_cli.span
    assert apm_cli.span.name == 'span_test'
    assert apm_cli.span.type == 'span_type'
    assert apm_cli.span.subtype == 'span_subtype'
    assert apm_cli.span.action == 'span_action'
    assert apm_cli.span.labels['label'] == 'foo'


@then("a parent ID is set")
def check_parent(apm_cli):
    assert apm_cli.transaction.trace_parent


@then('it fails to start')
def fails_to_start(apm_cli):
    assert apm_cli is None


@then("the context is set")
def the_context_is_set(apm_cli):
    assert apm_cli.transaction.context['custom']['var'] == 'foo'


@then(parsers.parse("the transaction result is {result:S}"))
def the_context_is_set(apm_cli, result):
    assert apm_cli.transaction.result == result

@then("the file with the parent transaction ID exits")
def check_parent_id_file_exists(parent_id_file):
    id = parent_id_file.read()
    assert len(id) > 0
