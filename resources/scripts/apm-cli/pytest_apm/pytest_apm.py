import elasticapm
import pytest
import logging

from apm.ApmCli import ApmCli

LOGGER = logging.getLogger("pytest_apm")

apm_server_url = None
apm_token = None
apm_api_key = None
apm_service_name = None
apm_custom_context = None
apm_parent_id = None
apm_outcome = None

def pytest_addoption(parser):
    group = parser.getgroup(
        "pytest-apm", "report APM traces about test executed."
    )

    group.addoption('--apm-server-url',
                        dest='apm_server_url',
                        env_var='APM_CLI_SERVER_URL',
                        help='URL for the APM server.')
    group.addoption('--apm-token',
                        dest='apm_token',
                        env_var='APM_CLI_TOKEN',
                        help='Token to access to APM server.')
    group.addoption('--apm-api-key',
                        dest='apm_api_key',
                        env_var='APM_CLI_TOKEN_API_KEY',
                        help='API key to access to APM server.')
    group.addoption('--apm-service-name',
                        dest='apm_service_name',
                        env_var='APM_CLI_SERVICE_NAME',
                        help='Name of the service.')
    group.addoption('--apm-custom-context',
                        dest='apm_custom_context',
                        env_var='APM_CLI_CUSTOM_CONTEXT',
                        help='Custom context for the current transaction in a JSON string.')


def pytest_sessionstart(session):
    global apm_server_url, apm_token, apm_api_key, apm_service_name, apm_custom_context, apm_parent_id
    config = session.config
    apm_server_url = config.getoption("apm_server_url", default=None)
    apm_token = config.getoption("apm_token", default=None)
    apm_api_key = config.getoption("apm_api_key", default=None)
    apm_service_name = config.getoption("apm_service_name", default=None)
    apm_custom_context = config.getoption("apm_custom_context", default=None)
    apm_cli = init_apm_client()
    if apm_cli:
        apm_cli.args.custom_context = apm_custom_context
        apm_cli.args.transaction_name = 'Session'
        apm_cli.begin_transaction()
        apm_cli.begin_span()
        apm_cli.custom_context()
        with elasticapm.capture_span('parent'):
            apm_parent_id = apm_cli.get_parent_id()
        apm_cli.end_transaction()



def init_apm_client():
    global apm_server_url, apm_token, apm_api_key, apm_service_name, apm_custom_context, apm_parent_id
    print("init_apm_client")
    if apm_server_url:
        if not (apm_token or apm_api_key) or not apm_service_name:
            pytest.fail("""
            APM server URL, APM service name, and an TOKEN or API Key are required to connect to the APM service.
            --apm-server-url https://apm.example.com:8200 --apm-token a51bfe6c --apm-service-name my_service
            or
             --apm-server-url https://apm.example.com:8200 --apm-api-key 3398579f385ea51bfe6cb2183546931d --apm-service-name my_service
            """)  # noqa E501
            raise
        apmCliLogger = logging.getLogger('ApmCli')
        apmCliLogger.setLevel(logging.DEBUG)
        apm_cli = ApmCli(parse_cmd_args=False)
        apm_cli.init(
            server_url=apm_server_url,
            apm_token=apm_token,
            api_key=apm_api_key,
            service_name=apm_service_name
        )
        return apm_cli


def pytest_runtest_setup(item):
    global apm_parent_id, apm_outcome
    apm_outcome = None
    print("pytest_runtest_setup-{}-{}".format(item.name, apm_parent_id))
    apm_cli = init_apm_client()
    if apm_cli:
        apm_cli.args.transaction_name = item.name
        apm_cli.args.span_name = item.name + ' - BEGIN'
        apm_cli.args.apm_parent_id = apm_parent_id
        apm_cli.begin_transaction()
        apm_cli.begin_span()
        apm_cli.custom_context()
 #       apm_cli.end_transaction()


def pytest_report_teststatus(report):
    global apm_outcome
    apm_outcome = report.outcome


def pytest_runtest_teardown(item, nextitem):
    global apm_parent_id, apm_outcome
    print("pytest_runtest_teardown-{}-{}".format(apm_outcome, apm_parent_id))
    apm_cli = init_apm_client()
    if apm_cli:
        apm_cli.args.transaction_name = item.name
        apm_cli.args.span_name = item.name + ' - END'
        apm_cli.args.apm_parent_id = apm_parent_id
        apm_cli.args.transaction_result = apm_outcome
        #        apm_cli.begin_transaction()
        apm_cli.begin_span()
        apm_cli.custom_context()
        apm_cli.end_transaction()
