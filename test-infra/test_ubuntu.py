# [...]
# test.py::test_package[local-nginx-1.6] PASSED
# test.py::test_package[local-python-2.7] PASSED
# [...]
import pytest

def test_nginx_is_installed(host):
  assert host.file("${HUDSON_HOME}/.java/java10").exists
