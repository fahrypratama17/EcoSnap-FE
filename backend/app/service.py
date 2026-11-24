import asyncio
import hashlib
import time
from dataclasses import dataclass

from starlette.concurrency import run_in_threadpool

from .errors import ScanError
from .images import optimize_image
from .provider import WasteAnalyzer
from .schema import ScanResponse


@dataclass
class Entry:
    digest: str
    task: asyncio.Task
    created: float


class ScanService:
    """Keep only validated responses briefly in RAM, never uploaded photos on disk."""

    def __init__(self, analyzer: WasteAnalyzer):
        self.analyzer = analyzer
        self.entries: dict[tuple[str, str], Entry] = {}
        self.lock = asyncio.Lock()
        self.slots = asyncio.Semaphore(2)

    async def analyze(self, image: bytes, client: str, scan_id: str) -> ScanResponse:
        key = (client, scan_id)
        digest = hashlib.sha256(image).hexdigest()
        async with self.lock:
            now = time.monotonic()
            self.entries = {
                k: entry for k, entry in self.entries.items()
                if not entry.task.done() or now - entry.created < 600
            }
            entry = self.entries.get(key)
            if entry and entry.digest != digest:
                raise ScanError(409, "scan_id_conflict", "Foto telah berubah. Buka pemindaian baru untuk melanjutkan.")
            if not entry:
                if len(self.entries) >= 64:
                    finished = next((k for k, e in self.entries.items() if e.task.done()), None)
                    if finished is None:
                        raise ScanError(503, "busy", "Layanan sedang sibuk. Coba lagi sebentar.", 5)
                    del self.entries[finished]
                entry = Entry(digest, asyncio.create_task(self._analyze(image)), now)
                entry.task.add_done_callback(self._observe_task)
                self.entries[key] = entry
        try:
            return await asyncio.shield(entry.task)
        except ScanError:
            async with self.lock:
                if self.entries.get(key) is entry:
                    del self.entries[key]
            raise

    @staticmethod
    def _observe_task(task):
        # A disconnected caller must not leave an unobserved provider exception in logs.
        if not task.cancelled():
            task.exception()

    async def _analyze(self, image: bytes) -> ScanResponse:
        try:
            async with asyncio.timeout(85):
                async with self.slots:
                    jpeg = await run_in_threadpool(optimize_image, image)
                    result = await self.analyzer.analyze(jpeg)
                    return ScanResponse.model_validate(result)
        except TimeoutError:
            raise ScanError(504, "provider_timeout", "Analisis membutuhkan waktu lebih lama. Silakan coba lagi.") from None

    async def close(self):
        tasks = [entry.task for entry in self.entries.values()]
        for task in tasks:
            if not task.done():
                task.cancel()
        await asyncio.gather(*tasks, return_exceptions=True)
        self.entries.clear()
