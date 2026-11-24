import asyncio
import base64
import json
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from pathlib import Path
from typing import Protocol

import httpx
from pydantic import ValidationError

from .errors import ScanError
from .schema import ScanResponse
from .settings import Settings


class WasteAnalyzer(Protocol):
    async def analyze(self, jpeg: bytes) -> ScanResponse: ...


def retry_seconds(value: str | None) -> int:
    try:
        seconds = float(value or "2")
    except ValueError:
        try:
            seconds = (parsedate_to_datetime(value) - datetime.now(timezone.utc)).total_seconds()
        except (TypeError, ValueError, OverflowError):
            seconds = 2
    return max(1, min(3600, int(seconds + 0.999)))


class GroqWasteAnalyzer:
    def __init__(self, settings: Settings, client: httpx.AsyncClient, sleep=asyncio.sleep):
        self.settings = settings
        self.client = client
        self.sleep = sleep
        prompt = Path(__file__).with_name("system_prompt.txt").read_text(encoding="utf-8")
        self.prompt = prompt + "\nRequired JSON schema:\n" + json.dumps(ScanResponse.model_json_schema(), ensure_ascii=False, separators=(",", ":"))

    async def analyze(self, jpeg: bytes) -> ScanResponse:
        if not self.settings.api_key:
            raise ScanError(503, "missing_api_key", "Layanan analisis belum dikonfigurasi. Hubungi pengelola EcoSnap.")
        payload = {
            "model": self.settings.model,
            "messages": [
                {"role": "system", "content": self.prompt},
                {"role": "user", "content": [
                    {"type": "text", "text": "Analisis foto sampah ini sesuai instruksi. Kembalikan satu objek JSON dalam Bahasa Indonesia."},
                    {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64," + base64.b64encode(jpeg).decode("ascii")}},
                ]},
            ],
            "temperature": 0.2,
            "max_completion_tokens": 4096,
            "reasoning_effort": "none",
            "stream": False,
            "response_format": {"type": "json_object"},
        }
        response = None
        # A single bounded retry for explicit transient rejections. Read timeouts may have
        # completed inference upstream, so those are returned for an explicit user retry.
        for attempt in range(2):
            try:
                response = await self.client.post(
                    self.settings.base_url + "/chat/completions",
                    headers={"Authorization": "Bearer " + self.settings.api_key},
                    json=payload,
                )
            except httpx.ConnectError:
                if attempt == 0:
                    await self.sleep(1)
                    continue
                raise ScanError(503, "provider_network", "Layanan AI belum dapat dihubungi. Coba lagi sebentar.") from None
            except httpx.TimeoutException:
                raise ScanError(504, "provider_timeout", "Analisis membutuhkan waktu lebih lama. Silakan coba lagi.") from None
            except httpx.RequestError:
                raise ScanError(503, "provider_network", "Koneksi ke layanan AI terputus. Silakan coba lagi.") from None
            wait = retry_seconds(response.headers.get("retry-after"))
            if response.status_code in {429, 502, 503, 504} and attempt == 0 and wait <= 5:
                await self.sleep(wait)
                continue
            break
        if response.status_code in {401, 403}:
            raise ScanError(503, "invalid_api_key", "Layanan AI belum dapat digunakan. Hubungi pengelola EcoSnap.")
        if response.status_code == 429:
            raise ScanError(429, "provider_rate_limit", "Batas layanan AI sedang tercapai. Coba lagi setelah beberapa saat.", retry_seconds(response.headers.get("retry-after")))
        if response.status_code in {400, 404, 422}:
            raise ScanError(503, "model_unavailable", "Model analisis belum tersedia atau konfigurasinya perlu diperiksa oleh pengelola.")
        if not response.is_success:
            raise ScanError(502, "provider_error", "Layanan AI sedang mengalami gangguan. Silakan coba lagi.")
        try:
            if len(response.content) > 256_000:
                raise ValueError("oversized response")
            choice = response.json()["choices"][0]
            if choice.get("finish_reason") == "length":
                raise ScanError(502, "truncated_response", "Hasil analisis terpotong. Silakan coba lagi.")
            if choice.get("finish_reason") != "stop":
                raise ValueError("incomplete response")
            return ScanResponse.model_validate_json(choice["message"]["content"])
        except ScanError:
            raise
        except (ValidationError, ValueError, KeyError, IndexError, TypeError):
            raise ScanError(502, "invalid_ai_response", "Hasil analisis belum valid. Silakan coba lagi dengan foto yang jelas.") from None
