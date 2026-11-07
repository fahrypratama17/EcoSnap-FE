package com.example.ecosnap.features.photos.data

import android.net.Uri
import com.example.ecosnap.features.photos.model.ReuseIdea
import com.example.ecosnap.features.photos.model.WastePhotosResult
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

interface WastePhotosRepository {
  suspend fun analyze(imageURL: Uri): WastePhotosResult
}

class DemoWastePhotosRepository : WastePhotosRepository {
  override suspend fun analyze(imageURL: Uri): WastePhotosResult {
    delay(400L.milliseconds)

    return WastePhotosResult(
      objectName = "Botol Plastik Minuman",
      category = "Plastik",
      material = "PET",
      tags = listOf(
        "Plastik",
        "Anorganik",
        "Potensi Daur Ulang"
      ),
      description = "Botol minuman berbahan plastik yang berpotensi didaur ulang. Pastikan jenis material dan kondisi kemasan sesuai ketentuan mitra penyetoran.",
      handlingSteps = listOf(
        "Kosongkan sisa isi botol minuman.",
        "Bilas jika diperlukan, kemudian keringkan.",
        "Pisahkan botol sesuai ketentuan mitra penyetoran.",
      ),

      reuseIdeas = listOf(
        ReuseIdea(
          id = "pot-tanaman",
          title = "Pot Tanaman Mini",
          description = "Ubah botol bekas menjadi wadah tanaman sederhana.",
          toolsAndMaterials = listOf(
            "Botol bekas",
            "Gunting",
            "Tanah atau media tanam",
            "Bibit tanaman"
          ),
          steps = listOf(
            "Bersihkan dan keringkan botol.",
            "Buat bukaan dan lubang drainase dengan bantuan orang dewasa.",
            "Isi dengan media tanam.",
            "Tanam bibit dan siram secukupnya."
          )
        ),

        ReuseIdea(
          id = "tempat-pensil",
          title = "Tempat Pensil",
          description = "Manfaatkan botol sebagai organizer meja belajar.",
          toolsAndMaterials = listOf(
            "Botol bekas",
            "Gunting",
            "Pelindung tepi",
            "Alat dekorasi"
          ),
          steps = listOf(
            "Bersihkan botol bekas.",
            "Potong bagian atas dengan aman.",
            "Haluskan atau lindungi tepi potongan.",
            "Hias dan gunakan sebagai tempat pensil.",
          )
        ),

        ReuseIdea(
          id = "penyiram",
          title = "Penyiram Tanaman",
          description = "Gunakan botol untuk menyiram tanaman di rumah.",
          toolsAndMaterials = listOf(
            "Botol bekas",
            "Tutup botol",
            "Air"
          ),
          steps = listOf(
            "Bersihkan botol.",
            "Buat beberapa lubang kecil pada tutup dengan bantuan orang dewasa.",
            "Isi botol dengan air.",
            "Pasang tutup dan gunakan untuk menyiram tanaman.",
          )
        )
      ),

      industryPotential = "Plastik PET dapat diproses menjadi bahan baku produk baru melalui fasilitas daur ulang yang sesuai. Penerimaannya bergantung pada kondisi material dan ketentuan fasilitas.",
    )
  }
}