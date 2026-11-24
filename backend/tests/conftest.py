import pytest


@pytest.fixture(autouse=True)
def disable_live_credentials(monkeypatch):
    monkeypatch.setenv("GROQ_API_KEY", "")
    monkeypatch.setenv("GROQ_BASE_URL", "https://groq-mock.invalid/openai/v1")
