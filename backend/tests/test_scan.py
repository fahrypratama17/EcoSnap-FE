import asyncio
import base64
import copy
import io
import json
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from uuid import uuid4

import httpx
import pytest
from fastapi.testclient import TestClient
from PIL import Image
from pydantic import ValidationError

from app.images import MAX_IMAGE_BYTES, optimize_image
from app.main import create_app
from app.provider import GroqWasteAnalyzer
from app.schema import ScanResponse
from app.service import ScanService
from app.settings import Settings


@pytest.fixture
def bottle():
    path = Path(__file__).resolve().parents[2] / "app/src/test/resources/waste_scan_success.json"
    return json.loads(path.read_text(encoding="utf-8"))


@pytest.fixture
def photo():
    stream = io.BytesIO()
    Image.new("RGB", (80, 40), "green").save(stream, "PNG")
    return stream.getvalue()


def envelope(value, finish_reason="stop"):
    return {"choices": [{"finish_reason": finish_reason, "message": {"content": json.dumps(value, ensure_ascii=False)}}]}


async def no_sleep(seconds):
    pass


def provider(handler, key="test-key-not-a-credential"):
    return GroqWasteAnalyzer(Settings(api_key=key), httpx.AsyncClient(transport=httpx.MockTransport(handler)), sleep=no_sleep)


def upload(client, photo, scan_id=None):
    return client.post("/api/scan", files={"image": ("pretend.exe", photo, "application/octet-stream")}, headers={"X-Scan-Id": scan_id or str(uuid4())})


@pytest.mark.parametrize("object_name,material,category,recycling,hazardous", [
    ("Botol Plastik Minuman", "Plastik", "Anorganik", "conditionally_recyclable", False),
    ("Kotak Kardus", "Kertas", "Anorganik", "recyclable", False),
    ("Kaleng Minuman Aluminium", "Aluminium", "Anorganik", "recyclable", False),
    ("Kulit Pisang", "Organik", "Organik", "conditionally_recyclable", False),
    ("Tisu Bekas", "Tisu", "Residu", "non_recyclable", False),
    ("Baterai Rusak", "Baterai", "B3", "non_recyclable", True),
    ("Limbah Elektronik", "Campuran", "B3", "conditionally_recyclable", True),
    ("Kemasan Plastik Tidak Dikenal", None, "Tidak Diketahui", "unknown", False),
])
def test_waste_scenarios(bottle, photo, object_name, material, category, recycling, hazardous):
    value = copy.deepcopy(bottle)
    value["identification"].update(object_name=object_name, material=material, category=category,
        recycling_status=recycling, recyclable={"recyclable": True, "conditionally_recyclable": True, "non_recyclable": False, "unknown": None}[recycling], hazardous=hazardous)
    value["identification"].update(tags=[category], description=f"Objek {object_name} yang perlu dipilah dan ditangani sesuai kondisinya.")
    if object_name != "Botol Plastik Minuman":
        value["upcycling"] = {"possible": False, "reason": "Tidak ada rekomendasi penggunaan ulang yang aman untuk kondisi ini.", "ideas": []}
        value["handling_steps"] = ["Hindari kontak langsung dengan bagian yang berisiko.", "Pisahkan dari sampah lainnya dan simpan dengan aman.", "Hubungi fasilitas pengelolaan yang sesuai untuk petunjuk penerimaan."]
        value["industrial_recycling"].update(description="Pengolahan bergantung pada jenis material dan fasilitas yang tersedia.", potential_products=[])
        value["deposit_recommendation"].update(partner_type="Fasilitas Pengelolaan Limbah B3" if hazardous else "TPS3R", search_keyword="pengelolaan " + object_name, preparation_note="Pisahkan dan ikuti petunjuk fasilitas penerima.")
    if recycling in {"non_recyclable", "unknown"}:
        value["industrial_recycling"].update(possible=False, potential_products=[])
    calls = []

    def groq(request):
        body = json.loads(request.content)
        calls.append(body)
        assert request.url == "https://api.groq.com/openai/v1/chat/completions"
        assert request.headers["authorization"] == "Bearer test-key-not-a-credential"
        assert body["model"] == "qwen/qwen3.8-27b"
        assert body["temperature"] == 0.2 and body["max_completion_tokens"] == 4096
        assert body["reasoning_effort"] == "none" and body["stream"] is False
        assert body["response_format"] == {"type": "json_object"}
        encoded = body["messages"][1]["content"][1]["image_url"]["url"]
        with Image.open(io.BytesIO(base64.b64decode(encoded.split(",", 1)[1]))) as image:
            assert image.format == "JPEG"
            assert max(image.size) <= 1024
            assert not image.getexif()
        return httpx.Response(200, json=envelope(value))

    with TestClient(create_app(analyzer=provider(groq))) as client:
        response = upload(client, photo)
    assert response.status_code == 200, response.text
    assert response.json() == value
    assert len(calls) == 1


@pytest.mark.parametrize("status,reason", [
    ("invalid_image", "Foto terlalu buram. Ambil foto ulang dengan cahaya cukup."),
    ("invalid_image", "Tidak terlihat objek sampah. Foto satu objek sampah."),
    ("multiple_objects", "Ada beberapa objek berbeda. Foto satu objek utama."),
])
def test_unsuccessful_identification(photo, status, reason):
    result = {"status": status, "message": reason, "identification": None, "handling_steps": [],
              "upcycling": {"possible": False, "reason": reason, "ideas": []}, "industrial_recycling": None, "deposit_recommendation": None}
    with TestClient(create_app(analyzer=provider(lambda request: httpx.Response(200, json=envelope(result))))) as client:
        response = upload(client, photo)
    assert response.status_code == 200
    assert response.json()["status"] == status


@pytest.mark.parametrize("fault,expected", [("json", "invalid_ai_response"), ("schema", "invalid_ai_response"), ("truncated", "truncated_response"), ("hazardous", "invalid_ai_response")])
def test_invalid_model_output_is_rejected(bottle, photo, fault, expected):
    result = envelope(bottle)
    if fault == "json":
        result["choices"][0]["message"]["content"] = "{broken"
    if fault == "schema":
        result["choices"][0]["message"]["content"] = '{"status":"success"}'
    if fault == "truncated":
        result["choices"][0]["finish_reason"] = "length"
    if fault == "hazardous":
        bottle["identification"]["hazardous"] = True
        result = envelope(bottle)
    calls = []

    def groq(request):
        calls.append(1)
        return httpx.Response(200, json=result)

    with TestClient(create_app(analyzer=provider(groq))) as client:
        response = upload(client, photo)
    assert response.status_code == 502
    assert response.json()["error"]["code"] == expected
    assert len(calls) == 1


@pytest.mark.parametrize("http_status,code", [(401, "invalid_api_key"), (403, "invalid_api_key"), (404, "model_unavailable"), (400, "model_unavailable"), (429, "provider_rate_limit"), (500, "provider_error")])
def test_provider_errors_are_sanitized(photo, http_status, code):
    with TestClient(create_app(analyzer=provider(lambda request: httpx.Response(http_status, headers={"Retry-After": "120"}, json={"error": {"message": "private provider information"}})))) as client:
        response = upload(client, photo)
    assert response.json()["error"]["code"] == code
    assert "private provider" not in response.text
    if http_status == 429:
        assert response.headers["retry-after"] == "120"


def test_missing_api_key_does_not_call_provider(photo):
    def unexpected(request):
        pytest.fail("Missing key must not call Groq")
    with TestClient(create_app(analyzer=provider(unexpected, key=""))) as client:
        response = upload(client, photo)
    assert response.status_code == 503
    assert response.json()["error"]["code"] == "missing_api_key"


def test_network_timeout_is_not_automatically_retried(photo):
    calls = []
    def groq(request):
        calls.append(1)
        raise httpx.ReadTimeout("private timeout detail", request=request)
    with TestClient(create_app(analyzer=provider(groq))) as client:
        response = upload(client, photo)
    assert response.status_code == 504
    assert response.json()["error"]["code"] == "provider_timeout"
    assert len(calls) == 1


@pytest.mark.parametrize("status", [429, 502, 503, 504])
def test_transient_retry_is_bounded_and_honors_delay(bottle, photo, status):
    calls, delays = [], []
    async def sleep(seconds):
        delays.append(seconds)
    def groq(request):
        calls.append(1)
        return httpx.Response(status, headers={"Retry-After": "3"}) if len(calls) == 1 else httpx.Response(200, json=envelope(bottle))
    analyzer = provider(groq)
    analyzer.sleep = sleep
    with TestClient(create_app(analyzer=analyzer)) as client:
        assert upload(client, photo).status_code == 200
    assert len(calls) == 2 and delays == [3]


@pytest.mark.parametrize("data", [b"<svg></svg>", b"not-an-image", b"\x89PNG\r\n\x1a\nbroken"])
def test_content_not_filename_validated(data):
    with TestClient(create_app(analyzer=provider(lambda request: pytest.fail("invalid image reached provider")))) as client:
        response = client.post("/api/scan", files={"image": ("safe.jpg", data, "image/jpeg")})
    assert response.status_code == 415


def test_size_limit_precedes_processing(photo, monkeypatch):
    with TestClient(create_app()) as client:
        response = client.post("/api/scan", content=b"unused", headers={"content-length": str(MAX_IMAGE_BYTES + 65537)})
    assert response.status_code == 413
    monkeypatch.setattr("app.images.MAX_IMAGE_PIXELS", 100)
    with pytest.raises(Exception) as error:
        optimize_image(photo)
    assert error.value.status == 413


def test_metadata_removed_orientation_and_aspect_preserved():
    image = Image.new("RGB", (2048, 1024), "green")
    exif = Image.Exif()
    exif[270] = "private metadata"
    exif[274] = 6
    source = io.BytesIO()
    image.save(source, "JPEG", exif=exif)
    with Image.open(io.BytesIO(optimize_image(source.getvalue()))) as output:
        assert output.size == (512, 1024)
        assert not output.getexif()
        assert "exif" not in output.info


def test_duplicate_scan_id_reuses_success_and_concurrent_inference(bottle, photo):
    class Analyzer:
        calls = 0
        async def analyze(self, jpeg):
            self.calls += 1
            await asyncio.sleep(0.05)
            return ScanResponse.model_validate(bottle)
    analyzer = Analyzer()
    scan_id = str(uuid4())
    with TestClient(create_app(analyzer=analyzer)) as client:
        with ThreadPoolExecutor(max_workers=2) as pool:
            responses = list(pool.map(lambda _: upload(client, photo, scan_id), range(2)))
        again = upload(client, photo, scan_id)
        assert all(r.status_code == 200 for r in responses) and again.status_code == 200
        assert analyzer.calls == 1
        assert upload(client, photo + b"changed", scan_id).status_code == 409


def test_rate_limit_prevents_extra_provider_calls(bottle, photo):
    calls = []
    def groq(request):
        calls.append(1)
        return httpx.Response(200, json=envelope(bottle))
    with TestClient(create_app(analyzer=provider(groq), requests_per_minute=1)) as client:
        assert upload(client, photo).status_code == 200
        response = upload(client, photo)
    assert response.status_code == 429 and response.headers["retry-after"] == "60"
    assert len(calls) == 1


@pytest.mark.parametrize("mutate", [
    lambda r: r["identification"].update(recyclable=False),
    lambda r: r["identification"].update(category="B3"),
    lambda r: r["identification"].update(requires_verification=True, verification_note=None),
    lambda r: r["upcycling"].update(possible=False),
    lambda r: r["industrial_recycling"].update(possible=False),
    lambda r: r["deposit_recommendation"].update(search_keyword=None),
])
def test_semantic_inconsistency_rejected(bottle, mutate):
    mutate(bottle)
    with pytest.raises(ValidationError):
        ScanResponse.model_validate(bottle)


@pytest.mark.parametrize("object_name", ["Baterai Rusak", "Limbah Elektronik", "Jarum Suntik", "Botol Pestisida"])
def test_risky_objects_cannot_bypass_diy_guard(bottle, object_name):
    bottle["identification"]["object_name"] = object_name
    with pytest.raises(ValidationError):
        ScanResponse.model_validate(bottle)


def test_uncertain_material_cannot_recommend_food_use(bottle):
    bottle["upcycling"]["ideas"][0]["title"] = "Wadah Makanan"
    with pytest.raises(ValidationError):
        ScanResponse.model_validate(bottle)
