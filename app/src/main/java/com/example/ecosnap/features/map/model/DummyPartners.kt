package com.example.ecosnap.features.map.model

object DummyPartners {
  val partners = listOf(
    WastePartner(
      id = "tunas",
      name = "Bank Sampah Tunas",
      area = "Kawasan Malang",
      address = "Alamat Simulasi, Malang",
      phone = "0896-5162-2100",
      openingHours = "Senin-Sabtu, 08.00-16.00",
      location = PartnerLocation(
        latitude = -7.9775,
        longitude = 112.6304
      ),
      acceptedMaterials = setOf(
        WasteMaterial.PLASTIC,
        WasteMaterial.PAPER,
        WasteMaterial.METAL
      )
    ),
    WastePartner(
      id = "lestari",
      name = "Bank Sampah Lestari",
      area = "Kawasan Malang",
      address = "Alamat simulasi, Malang",
      phone = "0812-0000-0002",
      openingHours = "Senin–Jumat, 09.00–16.00",
      location = PartnerLocation(
        latitude = -7.9732,
        longitude = 112.6367
      ),
      acceptedMaterials = setOf(
        WasteMaterial.PLASTIC,
        WasteMaterial.PAPER
      )
    ),
    WastePartner(
      id = "hijau",
      name = "Mitra Daur Ulang Hijau",
      area = "Kawasan Malang",
      address = "Alamat simulasi, Malang",
      phone = "0812-0000-0003",
      openingHours = "Senin–Sabtu, 08.00–15.00",
      location = PartnerLocation(
        latitude = -7.9823,
        longitude = 112.6258
      ),
      acceptedMaterials = setOf(
        WasteMaterial.METAL,
        WasteMaterial.GLASS,
        WasteMaterial.PLASTIC
      )
    ),
    WastePartner(
      id = "berseri",
      name = "Bank Sampah Berseri",
      area = "Kawasan Malang",
      address = "Alamat simulasi, Malang",
      phone = "0812-0000-0004",
      openingHours = "Selasa–Minggu, 08.00–14.00",
      location = PartnerLocation(
        latitude = -7.9870,
        longitude = 112.6388
      ),
      acceptedMaterials = setOf(
        WasteMaterial.PAPER,
        WasteMaterial.GLASS
      )
    )
  )

  fun findById(id: String): WastePartner? =
    partners.firstOrNull {it.id == id}
}