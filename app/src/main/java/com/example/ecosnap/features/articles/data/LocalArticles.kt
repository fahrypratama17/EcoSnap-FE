package com.example.ecosnap.features.articles.data

import com.example.ecosnap.features.articles.components.Article
import com.example.ecosnap.features.articles.components.ArticleCategory
import com.example.ecosnap.features.articles.components.ArticleSection

object LocalArticles {
  val articles: List<Article> = listOf(
    Article(
      id = "mulai-memilah",
      title = "Mulai Memilah Sampah dari Rumah",
      summary = "Langkah sederhana memisahkan sampah agar lebih mudah dikelola dan disetorkan.",
      category = ArticleCategory.GUIDE,
      readingMinutes = 4,
      isFeatured = true,
      introduction = "Langkah sederhana memisahkan sampah agar lebih mudah dikelola dan disetorkan.",
      sections = listOf(
        ArticleSection(
          title = "Kenali kelompok sampah",
          body = "Pisahkan sisa makanan, kemasan plastik, kertas, logam, dan sampah yang memerlukan penanganan khusus. Gunakan wadah terpisah agar tidak bercampur.",
        ),
        ArticleSection(
          title = "Siapkan sebelum disetor",
          body = "Kosongkan kemasan dan bersihkan seperlunya. Keringkan material sebelum disimpan agar tidak mencemari sampah lainnya.",
        ),
        ArticleSection(
          title = "Temukan lokasi yang sesuai",
          body = "Periksa jenis material yang diterima mitra sebelum berkunjung. Tidak semua bank sampah menerima material yang sama.",
        )
      ),
      tip = "Ragu mengenali suatu sampah? Gunakan Scan AI dan periksa kembali informasi materialnya.",
    ),

    Article(
      id = "jenis-plastik",
      title = "Kenali Jenis Plastik Sebelum Didaur Ulang",
      summary = "Pelajari beberapa jenis plastik dan cara memilahnya dengan lebih tepat.",
      category = ArticleCategory.SORTING,
      readingMinutes = 5,
      introduction = "Kemasan plastik memiliki karakteristik berbeda. Mengetahui jenis material akan membantu proses pemilahan.",
      sections = listOf(
        ArticleSection(
          title = "Perhatikan kode material",
          body = "Sebagian kemasan memiliki kode identifikasi resin, seperti PET, HDPE, dan PP. Kode ini menunjukkan jenis plastik, bukan jaminan bahwa kemasan diterima untuk didaur ulang.",
        ),
        ArticleSection(
          title = "Pisahkan kemasan",
          body = "Pisahkan botol, tutup, dan kemasan lain jika diminta oleh mitra. Pastikan kemasan tidak berisi sisa makanan.",
        ),
        ArticleSection(
          title = "Periksa penerimaan mitra",
          body = "Jenis plastik yang diterima dapat berbeda di setiap tempat. Hubungi mitra sebelum menyetor.",
        )
      ),
      tip = "Mulailah dengan mengidentifikasi kemasan yang paling sering kamu gunakan."
    ),

    Article(
      id = "barang-bekas",
      title = "Ide Kreatif dari Barang Bekas",
      summary = "Beri kesempatan kedua bagi barang yang masih bisa dimanfaatkan.",
      category = ArticleCategory.RECYCLING,
      readingMinutes = 5,
      introduction = "Tidak semua barang bekas harus langsung dibuang. Sebagian dapat digunakan kembali dengan aman.",
      sections = listOf(
        ArticleSection(
          title = "Gunakan kembali wadah",
          body = "Wadah yang bersih dan masih layak dapat dimanfaatkan untuk menyimpan peralatan kecil.",
        ),
        ArticleSection(
          title = "Buat proyek sederhana",
          body = "Karton dan kertas bekas dapat dijadikan bahan kerajinan, model, atau media belajar.",
        ),
        ArticleSection(
          title = "Utamakan keamanan",
          body = "Hindari memanfaatkan kemasan bahan berbahaya atau material tajam tanpa perlindungan.",
        )
      ),
      tip = "Menggunakan kembali barang sering kali lebih sederhana daripada mengolahnya menjadi produk baru.",
    ),

    Article(
      id = "baterai-bekas",
      title = "Cara Aman Menangani Baterai Bekas",
      summary = "Kenali cara menyimpan dan menyalurkan baterai bekas secara aman.",
      category = ArticleCategory.SPECIAL_WASTE,
      readingMinutes = 4,
      introduction = "Baterai bekas memerlukan penanganan berbeda dari sampah rumah tangga biasa.",
      sections = listOf(
        ArticleSection(
          title = "Pisahkan dari sampah biasa",
          body = "Jangan mencampurkan baterai dengan sampah organik, kertas, atau kemasan plastik.",
        ),
        ArticleSection(
          title = "Simpan dengan aman",
          body = "Simpan baterai di tempat kering dan jauh dari panas. Lindungi terminal baterai dari kontak dengan benda logam.",
        ),
        ArticleSection(
          title = "Cari fasilitas penerima",
          body = "Serahkan ke fasilitas atau program pengumpulan limbah elektronik dan baterai yang sesuai.",
        )
      ),
      tip = "Baterai yang bocor, menggembung, atau rusak memerlukan perhatian khusus. Jangan dibongkar sendiri.",
    ),

    Article(
      id = "sebelum-setor",
      title = "Sebelum Menyetor Sampah ke Mitra",
      summary = "Persiapkan material agar proses penyetoran lebih mudah.",
      category = ArticleCategory.GUIDE,
      readingMinutes = 3,
      introduction = "Penyetoran akan lebih mudah jika material sudah dipilah sejak dari rumah.",
      sections = listOf(
        ArticleSection(
          title = "Pilah berdasarkan jenis",
          body = "Kelompokkan plastik, kertas, dan logam dalam wadah terpisah.",
        ),
        ArticleSection(
          title = "Bersihkan dan keringkan",
          body = "Kosongkan kemasan, bilas jika diperlukan, lalu keringkan.",
        ),
        ArticleSection(
          title = "Cek informasi mitra",
          body = "Periksa jam operasional, lokasi, dan material yang diterima.",
        )
      ),
      tip = "Gunakan fitur Peta EcoSnap untuk melihat informasi mitra."
    ),

    Article(
      id = "kabar-bank-sampah",
      title = "Kabar Hijau: Mengenal Peran Bank Sampah",
      summary = "Kenali bagaimana bank sampah membantu pengumpulan material terpilah di lingkungan sekitar.",
      category = ArticleCategory.NEWS,
      readingMinutes = 3,
      introduction = "Bank sampah merupakan salah satu pendekatan pengelolaan sampah berbasis partisipasi masyarakat.",
      sections = listOf(
        ArticleSection(
          title = "Apa itu bank sampah?",
          body = "Bank sampah membantu mengumpulkan dan mengelompokkan material yang mempunyai potensi pemanfaatan kembali atau daur ulang.",
        ),
        ArticleSection(
          title = "Mengapa pemilahan penting?",
          body = "Material terpilah lebih mudah diperiksa, ditimbang, dan disalurkan sesuai jenisnya.",
        ),
        ArticleSection(
          title = "Bagaimana cara berpartisipasi?",
          body = "Mulailah memilah dari rumah, kemudian periksa jenis material yang diterima fasilitas setempat.",
        )
      ),
      tip = "Cari informasi fasilitas pengumpulan terdekat melalui fitur Peta EcoSnap.",
    )
  )

  val featuredArticle: Article get() = articles.first { it.isFeatured }

  val latestNews: Article get() = articles.first { it.category == ArticleCategory.NEWS }

  fun findById(id: String): Article? = articles.firstOrNull { it.id == id }
}