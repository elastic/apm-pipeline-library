import pytest

def test_nginx_is_installed(host):
  assert host.file("${HUDSON_HOME}/.java/java10").exists
