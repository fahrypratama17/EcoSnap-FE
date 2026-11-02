package com.example.ecosnap.features.home.model

data class NearbyPartnerUi(
  val name: String,
  val location: String,
  val acceptedMaterials: List<String>
)

data class HomeUiState(
  val userName: String = "Pengguna",
  val points: Int = 0,
  val monthlyDeposits: Int = 0,

  val missionTitle: String = "Pahlawan Daur Ulang",
  val missionDescription: String = "Setorkan sampah sebanyak 3 kali",
  val missionProgress: Int = 0,
  val missionTarget: Int = 3,
  val missionReward: Int = 50,

  val nearbyPartner: NearbyPartnerUi? = null,
) {
  companion object {

    fun demo() = HomeUiState(
      userName = "Fahry",
      points = 1250,
      monthlyDeposits = 3,
      missionProgress = 1,
      missionTarget = 3,
      missionReward = 50,

      nearbyPartner = NearbyPartnerUi(
        name = "Bank Sampah Tunas",
        location = "Area Taman Hijau - Lokasi ilustrasi",
        acceptedMaterials = listOf(
          "Plastik", "Kertas", "Logam"
        )
      )
    )
  }
}
