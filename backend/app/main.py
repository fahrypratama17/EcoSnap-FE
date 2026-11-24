import asyncio
import time
from collections import deque
from contextlib import asynccontextmanager
from uuid import UUID, uuid4

import httpx
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from starlette.datastructures import UploadFile
from starlette.exceptions import HTTPException
from starlette.responses import JSONResponse

from .errors import ScanError
from .images import MAX_IMAGE_BYTES
from .provider import GroqWasteAnalyzer, WasteAnalyzer
from .schema import ScanResponse
from .service import ScanService
from .settings import Settings


def error_response(error: ScanError):
    headers = {"Cache-Control": "no-store"}
    if error.retry_after:
        headers["Retry-After"] = str(error.retry_after)
    return JSONResponse({"error": {"code": error.code, "message": error.message}}, error.status, headers=headers)


class ScanProtection:
    """Single-worker admission control, before multipart parsing or image decoding."""

    def __init__(self, app, requests_per_minute=6):
        self.app = app
        self.limit = requests_per_minute
        self.clients = {}
        self.global_requests = deque()
        self.active = 0

    async def __call__(self, scope, receive, send):
        if scope["type"] != "http" or scope["path"] != "/api/scan" or scope["method"] != "POST":
            return await self.app(scope, receive, send)
        now = time.monotonic()
        self.clients = {ip: times for ip, times in self.clients.items() if times and times[-1] > now - 60}
        ip = (scope.get("client") or ("unknown", 0))[0]
        times = self.clients.setdefault(ip, deque())
        while times and times[0] <= now - 60:
            times.popleft()
        while self.global_requests and self.global_requests[0] <= now - 60:
            self.global_requests.popleft()
        if len(times) >= self.limit or len(self.global_requests) >= 30 or len(self.clients) > 4096:
            return await error_response(ScanError(429, "rate_limit", "Terlalu banyak pemindaian. Tunggu satu menit lalu coba lagi.", 60))(scope, receive, send)
        if self.active >= 4:
            return await error_response(ScanError(503, "busy", "Layanan sedang sibuk. Coba lagi sebentar.", 5))(scope, receive, send)
        times.append(now)
        self.global_requests.append(now)
        self.active += 1
        try:
            limit = MAX_IMAGE_BYTES + 65536
            headers = dict(scope.get("headers", []))
            try:
                length = int(headers.get(b"content-length", b"0"))
            except ValueError:
                return await error_response(ScanError(400, "invalid_upload", "Unggahan gambar tidak valid."))(scope, receive, send)
            if length > limit:
                return await error_response(ScanError(413, "image_too_large", "Gunakan gambar berukuran maksimal 10 MB."))(scope, receive, send)
            body = bytearray()
            deadline = time.monotonic() + 20
            while True:
                try:
                    message = await asyncio.wait_for(receive(), timeout=max(0, deadline - time.monotonic()))
                except TimeoutError:
                    return await error_response(ScanError(408, "upload_timeout", "Unggahan terlalu lama. Periksa koneksi lalu coba lagi."))(scope, receive, send)
                if message["type"] == "http.disconnect":
                    return
                body.extend(message.get("body", b""))
                if len(body) > limit:
                    return await error_response(ScanError(413, "image_too_large", "Gunakan gambar berukuran maksimal 10 MB."))(scope, receive, send)
                if not message.get("more_body", False):
                    break
            delivered = False

            async def bounded_receive():
                nonlocal delivered
                if not delivered:
                    delivered = True
                    return {"type": "http.request", "body": bytes(body), "more_body": False}
                return await receive()

            await self.app(scope, bounded_receive, send)
        finally:
            self.active -= 1


def create_app(settings: Settings | None = None, analyzer: WasteAnalyzer | None = None, requests_per_minute: int = 6):
    settings = settings or Settings.from_env()

    @asynccontextmanager
    async def lifespan(app):
        async with httpx.AsyncClient(timeout=httpx.Timeout(65, connect=10), follow_redirects=False) as client:
            app.state.scans = ScanService(analyzer or GroqWasteAnalyzer(settings, client))
            yield
            await app.state.scans.close()

    app = FastAPI(title="EcoSnap Waste Analysis API", lifespan=lifespan)
    app.add_middleware(ScanProtection, requests_per_minute=requests_per_minute)

    @app.exception_handler(ScanError)
    async def scan_error(request, error):
        return error_response(error)

    @app.exception_handler(RequestValidationError)
    @app.exception_handler(HTTPException)
    async def invalid_request(request, error):
        return error_response(ScanError(400, "invalid_upload", "Kirim satu foto pada field image menggunakan multipart/form-data."))

    @app.exception_handler(Exception)
    async def unexpected_error(request, error):
        return error_response(ScanError(500, "internal_error", "Pemindaian belum dapat diproses. Silakan coba lagi."))

    @app.get("/health")
    async def health():
        return {"status": "ok"}

    @app.post("/api/scan", response_model=ScanResponse)
    async def scan(request: Request):
        if not request.headers.get("content-type", "").startswith("multipart/form-data"):
            raise ScanError(415, "unsupported_upload", "Unggah satu foto menggunakan multipart/form-data.")
        try:
            scan_id = str(UUID(request.headers.get("x-scan-id", str(uuid4()))))
        except ValueError:
            raise ScanError(400, "invalid_scan_id", "Identitas pemindaian tidak valid. Mulai pemindaian baru.") from None
        async with request.form(max_files=1, max_fields=0, max_part_size=MAX_IMAGE_BYTES) as form:
            image = form.get("image")
            if len(form) != 1 or not isinstance(image, UploadFile):
                raise ScanError(400, "missing_image", "Pilih satu foto sampah terlebih dahulu.")
            data = await image.read(MAX_IMAGE_BYTES + 1)
        if not data:
            raise ScanError(415, "unsupported_image", "File gambar kosong. Pilih foto yang valid.")
        if len(data) > MAX_IMAGE_BYTES:
            raise ScanError(413, "image_too_large", "Gunakan gambar berukuran maksimal 10 MB.")
        result = await request.app.state.scans.analyze(data, request.client.host if request.client else "unknown", scan_id)
        return JSONResponse(result.model_dump(mode="json"), headers={"Cache-Control": "no-store"})

    return app


app = create_app()
