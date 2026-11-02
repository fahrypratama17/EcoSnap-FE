package com.example.ecosnap.features.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.home.components.HomeHeader
import com.example.ecosnap.features.home.components.HomePartnerCard
import com.example.ecosnap.features.home.components.HomeScanPromoCard
import com.example.ecosnap.features.home.components.HomeSectionHeader
import com.example.ecosnap.features.home.components.HomeTipCard
import com.example.ecosnap.features.home.components.HomeWeeklyMissionCard
import com.example.ecosnap.features.home.model.HomeUiState

@Composable
fun HomeScreen(
  uiState: HomeUiState,
  onNotification: () -> Unit,
  onProfile: () -> Unit,
  onRedeemPoints: () -> Unit,
  onScanQr: () -> Unit,
  onFindPartner: () -> Unit,
  onHistory: () -> Unit,
  onScanWaste: () -> Unit,
  onSeeALlMission: () -> Unit,
  onSeeMap: () -> Unit,
  onPartnerClick: () -> Unit,
  modifier: Modifier = Modifier,
  @DrawableRes promoIllustrationRes: Int? = null
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    HomeHeader(
      onNotification = onNotification,
      onProfile = onProfile
    )

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Column {
          Text(
            text = "Selamat datang kembali!",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )

          Spacer(Modifier.height(3.dp))

          Text(
            text = "Halo, ${uiState.userName}!",
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 23.sp,
              lineHeight = 29.sp
            ),
            color = EcoColor.Green900
          )

          Text(
            text = "Langkah kecilmu, berarti untuk bumi.",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )
        }
      }

      item {
        HomeScanPromoCard(
          onScan = onScanWaste,
          illustraionRes = promoIllustrationRes
        )
      }

      item {
        HomeSectionHeader(
          title = "Misi Mingguan",
          actionText = "Lihat Semua",
          onAction = onSeeALlMission
        )
      }

      item {
        HomeWeeklyMissionCard(
          title = uiState.missionTitle,
          description = uiState.missionDescription,
          progress = uiState.missionProgress,
          target = uiState.missionTarget,
          reward = uiState.missionReward
        )
      }

      item {
        HomeSectionHeader(
          title = "Mitra di sekitarmu",
          actionText = "Lihat peta",
          onAction = onSeeMap
        )
      }

      item {
        val partner = uiState.nearbyPartner

        if (partner != null) {
          HomePartnerCard(
            partner = partner,
            onClick = onPartnerClick
          )
        } else {
          Text(
            text = "Belum ada mitra yang ditampilkan",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        HomeTipCard(
          title = "Bilas, keringkan, lalu pilah.",
          description = "Kemasan bersih lebih mudah " +
                  "didaur ulang. Mulai dari botolmu hari ini."
        )
      }
    }
  }
}

@Preview(
  name = "Home EcoSnap",
  showBackground = true,
  widthDp = 360,
  heightDp = 780
)
@Composable
private fun HomeScreenPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    HomeScreen(
      uiState = HomeUiState.demo(),
      onNotification = {},
      onProfile = {},
      onRedeemPoints = {},
      onScanQr = {},
      onFindPartner = {},
      onHistory = {},
      onScanWaste = {},
      onSeeALlMission = {},
      onSeeMap = {},
      onPartnerClick = {}
    )
  }
}