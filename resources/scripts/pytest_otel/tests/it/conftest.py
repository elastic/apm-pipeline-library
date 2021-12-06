import pytest
from utils import is_portListening


@pytest.fixture(scope="session")
def otel_service(docker_ip, docker_services):
    """Ensure that port is listening."""

    # `port_for` takes a container port and returns the corresponding host port
    port = docker_services.port_for("otel-collector", 4317)
    docker_services.wait_until_responsive(
        timeout=30.0, pause=5, check=lambda: is_portListening(docker_ip, port)
    )
    return True
