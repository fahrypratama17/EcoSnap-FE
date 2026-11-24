import io
import warnings

from PIL import Image, ImageOps, UnidentifiedImageError

from .errors import ScanError

MAX_IMAGE_BYTES = 10 * 1024 * 1024
MAX_IMAGE_PIXELS = 25_000_000
ALLOWED_FORMATS = {"JPEG", "PNG", "WEBP"}


def optimize_image(data: bytes) -> bytes:
    if not data or len(data) > MAX_IMAGE_BYTES:
        raise ScanError(413, "image_too_large", "Gunakan gambar berukuran maksimal 10 MB.")
    try:
        with warnings.catch_warnings():
            warnings.simplefilter("error", Image.DecompressionBombWarning)
            with Image.open(io.BytesIO(data)) as source:
                if source.format not in ALLOWED_FORMATS:
                    raise ScanError(415, "unsupported_image", "Gunakan gambar JPEG, PNG, atau WebP.")
                if source.width * source.height > MAX_IMAGE_PIXELS:
                    raise ScanError(413, "image_too_large", "Resolusi gambar terlalu besar. Gunakan foto maksimal 25 megapiksel.")
                if getattr(source, "is_animated", False):
                    raise ScanError(415, "unsupported_image", "Gunakan satu foto statis, bukan gambar bergerak.")
                source.verify()
            with Image.open(io.BytesIO(data)) as source:
                source = ImageOps.exif_transpose(source)
                source.thumbnail((1024, 1024), Image.Resampling.LANCZOS)
                # A fresh RGB image drops EXIF, GPS, comments, profiles and other metadata.
                rgba = source.convert("RGBA")
                clean = Image.new("RGB", source.size, "white")
                clean.paste(rgba, mask=rgba.getchannel("A"))
                output = io.BytesIO()
                clean.save(output, format="JPEG", quality=83, optimize=True)
                return output.getvalue()
    except ScanError:
        raise
    except (Image.DecompressionBombError, Image.DecompressionBombWarning):
        raise ScanError(413, "image_too_large", "Resolusi gambar terlalu besar. Perkecil gambar lalu coba lagi.") from None
    except (UnidentifiedImageError, OSError, ValueError, SyntaxError):
        raise ScanError(415, "unsupported_image", "File gambar tidak valid atau rusak. Pilih foto JPEG, PNG, atau WebP.") from None
