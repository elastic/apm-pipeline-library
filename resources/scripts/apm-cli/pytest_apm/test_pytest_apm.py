import time
import random

def test_basic():
    """Basic."""
    time.sleep(random.randint(3, 10))
    pass

def test_success():
    """Success."""
    time.sleep(random.randint(3, 10))
    assert True

def test_failure():
    """Failure."""
    time.sleep(random.randint(3, 10))
    assert False
