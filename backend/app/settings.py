import os
from dataclasses import dataclass, field
from pathlib import Path

from dotenv import load_dotenv


@dataclass(frozen=True)
class Settings:
    api_key: str = field(default="", repr=False)
    model: str = "qwen/qwen3.8-27b"
    base_url: str = "https://api.groq.com/openai/v1"

    @classmethod
    def from_env(cls):
        load_dotenv(Path(__file__).resolve().parents[2] / ".env")
        return cls(
            api_key=os.getenv("GROQ_API_KEY", "").strip(),
            model=os.getenv("GROQ_MODEL", "qwen/qwen3.8-27b").strip(),
            base_url=os.getenv("GROQ_BASE_URL", "https://api.groq.com/openai/v1").rstrip("/"),
        )
