package com.example.ecosnap.features.games.data

import com.example.ecosnap.features.games.model.GameQuestion
import com.example.ecosnap.features.games.model.GameType
import com.example.ecosnap.features.games.model.GameVisual
import com.example.ecosnap.features.games.model.MiniGame

object LocalGames {
  private val sortingOptions = listOf(
    "Kertas",
    "Plastik",
    "Logam",
    "Organik"
  )

  val games: List<MiniGame> = listOf(
    MiniGame(
      id = "pilah-cepat",
      title = "Pilah Cepat",
      description = "Pilah sampah ke tempat yang benar.",
      about = "Kenali berbagai benda sehari-hari dan tentukan kategori sampah yang sesuai. Setiap jawaban benar menambah skor XP.",
      type = GameType.SORTING,
      instructions = listOf(
        "Perhatikan objek sampah yang ditampilkan.",
        "Pilih kategori sampah yang paling tepat.",
        "Dapatkan feedback dan lanjut ke objek berikutnya."
      ),
      questions = listOf(
        GameQuestion(
          id = "sort-1",
          title = "Botol plastik PET kosong",
          prompt = "Termasuk kategori sampah apa?",
          visual = GameVisual.BOTTLE,
          options = sortingOptions,
          correctAnswer = "Plastik",
          explanation = "Botol PET termasuk material plastik. Pastikan kosong dan ikuti ketentuan mitra sebelum menyetor.",
        ),
        GameQuestion(
          id = "sort-2",
          title = "Koran bekas bersih",
          prompt = "Termasuk kategori sampah apa?",
          visual = GameVisual.PAPER,
          options = sortingOptions,
          correctAnswer = "Kertas",
          explanation = "Koran merupakan material berbasis kertas dan dapat dikumpulkan terpisah jika kondisinya sesuai ketentuan mitra.",
        ),
        GameQuestion(
          id = "sort-3",
          title = "Kaleng minuman aluminium",
          prompt = "Termasuk kategori sampah apa?",
          visual = GameVisual.CAN,
          options = sortingOptions,
          correctAnswer = "Logam",
          explanation = "Aluminium termasuk logam. Kosongkan dan bersihkan kaleng sebelum dikumpulkan.",
        ),
        GameQuestion(
          id = "sort-4",
          title = "Kulit pisang",
          prompt = "Termasuk kategori sampah apa?",
          visual = GameVisual.ORGANIC,
          options = sortingOptions,
          correctAnswer = "Organik",
          explanation = "Kulit pisang merupakan sampah organik yang dapat terurai dan berpotensi diolah menjadi kompos.",
        ),
        GameQuestion(
          id = "sort-5",
          title = "Kardus bekas yang bersih",
          prompt = "Termasuk kategori sampah apa?",
          visual = GameVisual.BOX,
          options = sortingOptions,
          correctAnswer = "Kertas",
          explanation = "Kardus berasal dari serat kertas. Lipat dan jaga tetap kering agar lebih mudah dikumpulkan.",
        )
      )
    ),

    MiniGame(
      id = "eco-quiz",
      title = "Eco Quiz",
      description = "Uji pengetahuanmu tentang sampah.",
      about = "Jawab pertanyaan seputar pemilahan, pengurangan sampah, dan kebiasaan ramah lingkungan.",
      type = GameType.QUIZ,
      instructions = listOf(
        "Baca pertanyaan yang ditampilkan.",
        "Pilih satu jawaban yang paling tepat.",
        "Pelajari penjelasan dan kumpulkan XP."
      ),
      questions = listOf(
        GameQuestion(
          id = "quiz-1",
          title = "Apa langkah pertama sebelum menyetorkan botol plastik?",
          prompt = "Pilih tindakan yang paling tepat.",
          visual = GameVisual.RECYCLE,
          options = listOf(
            "Mencampurnya",
            "Mengosongkan botol",
            "Membakarnya",
            "Menguburnya"
          ),
          correctAnswer = "Mengosongkan botol",
          explanation = "Kosongkan botol terlebih dahulu. Bilas dan keringkan bila diperlukan sesuai ketentuan penerima.",
        ),
        GameQuestion(
          id = "quiz-2",
          title = "Mengapa sampah perlu dipilah?",
          prompt = "Pilih alasan yang paling tepat.",
          visual = GameVisual.BRAIN,
          options = listOf(
            "Supaya tercampur",
            "Menambah limbah",
            "Memudahkan pengolahan",
            "Menghilangkan semua sampah"
          ),
          correctAnswer = "Memudahkan pengolahan",
          explanation = "Pemilahan membantu material ditangani berdasarkan jenisnya dan mengurangi kontaminasi.",
        ),
        GameQuestion(
          id = "quiz-3",
          title = "Mana yang perlu penanganan terpisah dari sampah biasa?",
          prompt = "Pilih satu jawaban.",
          visual = GameVisual.RECYCLE,
          options = listOf(
            "Daun kering",
            "Baterai bekas",
            "Kardus bersih",
            "Kulit buah"
          ),
          correctAnswer = "Baterai bekas",
          explanation = "Baterai bekas memerlukan pengumpulan dan penanganan khusus, bukan dicampur dengan sampah biasa.",
        ),
        GameQuestion(
          id = "quiz-4",
          title = "Apa arti kode PET pada botol?",
          prompt = "Pilih jawaban yang tepat.",
          visual = GameVisual.BOTTLE,
          options = listOf(
            "Merek produk",
            "Tanggal produksi",
            "Jenis plastik",
            "Harga botol"
          ),
          correctAnswer = "Jenis plastik",
          explanation = "PET adalah salah satu jenis polimer plastik. Kode material tidak otomatis menjamin botol diterima setiap fasilitas daur ulang.",
        ),
        GameQuestion(
          id = "quiz-5",
          title = "Bagaimana menyiapkan kemasan untuk didaur ulang?",
          prompt = "Pilih kebiasaan yang tepat.",
          visual = GameVisual.BOX,
          options = listOf(
            "Campur makanan",
            "Buang ke sungai",
            "Bakar kemasan",
            "Kosongkan dan pilah"
          ),
          correctAnswer = "Kosongkan dan pilah",
          explanation = "Mengosongkan dan memilah kemasan membantu proses pengumpulan dan pengolahan.",
        )
      )
    )
  )

  fun findById(id: String): MiniGame? = games.firstOrNull { it.id == id }
}