# EcoSnap AI waste scanning

The Android app uses CameraX and the Android photo picker. Both sources lead to the same photo confirmation screen. Confirming calls the new FastAPI server, which preprocesses the image and calls Groq's vision chat-completions endpoint once in normal operation. The existing result screen renders the validated response and keeps guide expansion local.

## Local setup on Windows

Requires Python 3.11+ (tested with 3.13), the Android SDK, and JDK 25 used by this repository. From the repository root:

```powershell
python -m venv backend/.venv
backend/.venv/Scripts/python.exe -m pip install -r backend/requirements-dev.txt
Copy-Item .env.example .env
```

Edit `.env` on the server machine only:

```dotenv
GROQ_API_KEY=your_server_key_here
GROQ_MODEL=qwen/qwen3.8-27b
GROQ_BASE_URL=https://api.groq.com/openai/v1
```

The root `.env` is ignored by Git. Keep an already configured `.env` when running these commands again. Never put `GROQ_API_KEY` in Gradle properties, BuildConfig, Android resources, or a mobile environment file. The checked-in `.env.example` contains no credentials.

Start the API from the repository root:

```powershell
backend/.venv/Scripts/python.exe -m uvicorn app.main:app --app-dir backend --host 127.0.0.1 --port 8000 --workers 1 --no-access-log --no-proxy-headers
```

Health endpoint: `http://127.0.0.1:8000/health`. API documentation: `http://127.0.0.1:8000/docs`.

Build the Android app:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug '-PECOSNAP_API_BASE_URL=http://10.0.2.2:8000'
```

`ECOSNAP_API_BASE_URL` is a public server address, never a credential. The debug default is `http://10.0.2.2:8000` for the Android emulator. For a phone on the same LAN, start Uvicorn with `--host 0.0.0.0` and build with the computer's reachable LAN address. For a USB-connected phone with `adb reverse tcp:8000 tcp:8000`, use `http://127.0.0.1:8000`. Release requests require an explicitly configured HTTPS server URL:

```powershell
.\gradlew.bat assembleRelease '-PECOSNAP_API_BASE_URL=https://your-api.example.com'
```

Deployment requires HTTPS and a reverse proxy with body-size and connection limits. The in-process limiter is intended for one server worker. Multiple workers or replicas need a shared rate limiter and shared idempotency storage. The current repo's login is a local demo with no server authentication. This API applies IP and global admission limits, and provides an integration point for real authenticated user quotas when authentication is implemented.

## Manual test

1. Sign in as **Pengguna**, open **Scan**, and take a photo or choose a JPEG, PNG, or WebP from **Galeri**. HEIC and animated images are rejected with a helpful message.
2. Check the preview, then press **Analisis Foto**. A loading state stays visible until the request succeeds or an error is shown.
3. The existing result screen shows the actual photo, object, material, category, recycling state, handling steps, zero to three upcycling guides, safety warnings, and industrial processing advice. Conditional recycling uses an amber badge and an acceptance caveat. Unknown recycling is grey; non-recyclable is red.
4. Open and close guides, move to the partner map and return. These actions do not call Groq again. Results and scan IDs survive activity recreation through `SavedStateHandle`.
5. For an unclear or multiple-object image, retake the photo. Retry controls handle transport/provider failures and respect the server's retry delay.
6. Partner search receives the AI's search keyword and partner category. The existing map uses `DummyPartners`, not a live facility directory. A notice identifies this demo data. An unmatched query displays the existing empty state. Connect an actual partner service at `PartnerMapScreen`/`DummyPartners` before presenting facilities as verified.

An HTTP scan can also be tested without an Android device (this consumes real API quota):

```powershell
curl.exe -X POST http://127.0.0.1:8000/api/scan -H 'X-Scan-Id: 6e759542-b5b7-4361-83c9-15e1bdb591fc' -F 'image=@C:/path/to/waste.jpg'
```

Use a new UUID for each intentional new scan. Reuse it with the same file to retry a request whose result may already be available. Do not use the same UUID with a different image.

## Contract and request settings

`POST /api/scan` accepts one multipart file named `image`, with an optional UUID `X-Scan-Id` header. The response follows `app/schema.py`. Unsuccessful identification uses `invalid_image` or `multiple_objects` with a helpful Indonesian message, null identification/industry/deposit sections, and empty recommendation arrays. Operational errors use an HTTP error status and `{"error":{"code":"...","message":"..."}}`. Raw provider errors are never returned.

Server-side processing validates content, enforces a 10 MB file limit, rejects images over 25 megapixels, corrects EXIF orientation, preserves aspect ratio within 1024 pixels, composites transparency on white, and emits JPEG quality 83 without metadata. Uploaded images are processed in memory and temporary multipart files are closed immediately. The server does not persist uploads.

Provider configuration is isolated in `app/provider.py`. It sends the actual optimized image as a Base64 JPEG, the dedicated `app/system_prompt.txt`, and the Pydantic-generated JSON schema. Parameters are temperature `0.2`, `max_completion_tokens=4096`, `reasoning_effort="none"`, `stream=false`, and `response_format={"type":"json_object"}`. These are supported by the requested model. References: [model](https://console.groq.com/docs/model/qwen/qwen3.8-27b), [vision](https://console.groq.com/docs/vision), [JSON output mode](https://console.groq.com/docs/structured-outputs).

JSON mode does not guarantee schema adherence. Pydantic validates all fields, enums, size bounds, and semantic consistency before a result can reach Android. Hazardous objects cannot receive DIY guides. The validator also rejects guides for named high-risk waste and food/drink reuse of unverified material. This validation does not establish the scientific accuracy of arbitrary AI-generated advice; material verification and facility acceptance caveats remain visible.

Per scan ID and client address, concurrent requests share the same inference and validated result is retained for up to ten minutes (maximum 64 entries). Image hashes reject reuse of a scan ID with a different upload. Only explicit transient provider rejections or connection failures receive one bounded retry; `Retry-After` up to five seconds is awaited, and longer delays are returned to the client. Read timeouts, malformed JSON, schema violations, and truncated output are not automatically retried. There are at most two concurrent processing tasks, four admitted uploads, six API requests per client per minute, and thirty globally per minute. The total processing timeout is 85 seconds, upload deadline 20 seconds, and Android call timeout 100 seconds.

## Automated verification

```powershell
Push-Location backend
.\.venv\Scripts\python.exe -m pytest -q
Pop-Location
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug --console=plain --max-workers=1 '-Pkotlin.compiler.execution.strategy=in-process'
```

Python tests use `httpx.MockTransport` and clear live credentials automatically. They cover plastic, cardboard, aluminium, organics, tissue, batteries, electronics, unknown material, invalid images, multiple objects, malformed/truncated responses, key failures, timeouts, provider retries, metadata removal, upload limits, unsafe recommendations, rate limits, and duplicate requests. Android's JUnit/Robolectric tests mock the repository or HTTP interceptor to verify actual multipart bytes, confirmation, duplicate button presses, cached/restored results, and guide expansion without inference. No automated test intentionally calls Groq.

Live verification on 2026-11-24 used one uploaded reference image supplied by the user (a UI composition containing a bottle photograph). The real Groq request returned HTTP 200 and schema-valid `success`, identified **Botol Plastik Minuman**, category **Anorganik**, recycling status **conditionally_recyclable**, with **three** upcycling guides. The key's model-list request also returned HTTP 200 and included the configured model. This verifies the live API path with that image; physical-device camera behavior and a broad real-world image accuracy benchmark have not been tested.

## Implementation files

| Change | Path |
| --- | --- |
| Created | `.env.example` |
| Modified | `.gitignore` |
| Modified | `README.md` |
| Modified | `app/build.gradle.kts` |
| Created | `app/src/debug/AndroidManifest.xml` |
| Modified | `app/src/main/java/com/example/ecosnap/features/map/PartnerMapScreen.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/CameraScreen.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/PhotoResultScreen.kt` |
| Created | `app/src/main/java/com/example/ecosnap/features/photos/PhotoReviewScreen.kt` |
| Created | `app/src/main/java/com/example/ecosnap/features/photos/WasteScanViewModel.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/components/PhotoIdentificationCard.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/components/PhotoIndustryCard.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/components/PhotoMaterialChip.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/components/PhotoReuseIdeaCard.kt` |
| Created | `app/src/main/java/com/example/ecosnap/features/photos/data/ScanResponseParser.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/data/WastePhotosRepository.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/model/ReuseIdea.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/features/photos/model/WastePhotosResult.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/navigation/MainNavGraph.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/navigation/PhotoNavGraph.kt` |
| Modified | `app/src/main/java/com/example/ecosnap/navigation/Routes.kt` |
| Created | `app/src/test/java/com/example/ecosnap/features/photos/WasteScanTest.kt` |
| Created | `app/src/test/resources/waste_scan_success.json` |
| Created | `backend/README.md` |
| Created | `backend/app/__init__.py` |
| Created | `backend/app/errors.py` |
| Created | `backend/app/images.py` |
| Created | `backend/app/main.py` |
| Created | `backend/app/provider.py` |
| Created | `backend/app/schema.py` |
| Created | `backend/app/service.py` |
| Created | `backend/app/settings.py` |
| Created | `backend/app/system_prompt.txt` |
| Created | `backend/pytest.ini` |
| Created | `backend/requirements-dev.txt` |
| Created | `backend/requirements.txt` |
| Created | `backend/tests/conftest.py` |
| Created | `backend/tests/test_scan.py` |
| Modified | `gradle/libs.versions.toml` |
