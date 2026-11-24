package com.example.ecosnap.features.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoButton
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.home.components.HomeHeader
import com.example.ecosnap.features.home.components.HomeNewsCard
import com.example.ecosnap.features.home.components.HomePartnerCard
import com.example.ecosnap.features.home.components.HomePointsCard
import com.example.ecosnap.features.home.components.HomeScanPromoCard
import com.example.ecosnap.features.home.components.HomeSectionHeader
import com.example.ecosnap.features.home.components.HomeTipCard
import com.example.ecosnap.features.home.components.HomeWeeklyMissionCard
import com.example.ecosnap.features.home.model.HomeArticle
import com.example.ecosnap.features.home.model.HomeData
import com.example.ecosnap.features.home.model.HomeUiState
import com.example.ecosnap.features.home.model.Mission
import com.example.ecosnap.features.home.model.MissionBoard

@Composable
fun HomeScreen(
  uiState: HomeUiState,
  onRetry: () -> Unit,
  onNotification: () -> Unit,
  onProfile: () -> Unit,
  onRedeemPoints: () -> Unit,
  onScanQr: () -> Unit,
  onFindPartner: () -> Unit,
  onHistory: () -> Unit,
  onScanWaste: () -> Unit,
  onSeeAllMissions: () -> Unit,
  onSeeMap: () -> Unit,
  onPartnerClick: (String) -> Unit,
  onNewsClick: (String) -> Unit,
  onSeeAllArticles: () -> Unit,
  modifier: Modifier = Modifier,
  @DrawableRes promoIllustrationRes: Int? = null
) {
  val data = uiState.data

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    HomeHeader(
      onNotification = onNotification,
      onProfile = onProfile,
      unreadCount = data?.unreadCount ?: 0
    )

    if (data == null) {
      HomePlaceholder(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = onRetry
      )
      return@Column
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 14.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (uiState.error != null) {
        item {
          Column {
            Text(
              text = uiState.error,
              style = MaterialTheme.typography.bodyMedium,
              color = EcoColor.TextError
            )
            Spacer(Modifier.height(6.dp))
            EcoButton(text = "Muat ulang", onClick = onRetry, height = 40.dp)
          }
        }
      }

      item {
        Column {
          Text(
            text = "Selamat datang kembali!",
            fontSize = 12.sp,
            color = EcoColor.Green700
          )

          Spacer(Modifier.height(3.dp))

          Text(
            text = if (data.userName.isBlank()) "Halo!" else "Halo, ${data.userName}!",
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
        HomePointsCard(
          points = data.balance,
          monthlyDeposits = data.monthlyDeposits,
          onRedeem = onRedeemPoints,
          onSqanQr = onScanQr,
          onFindPartner = onFindPartner,
          onHistory = onHistory
        )
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
          onAction = onSeeAllMissions
        )
      }

      item {
        val mission = data.missions?.featured()

        if (mission != null) {
          HomeWeeklyMissionCard(mission = mission)
        } else {
          Text(
            text = "Belum ada misi minggu ini",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        HomeSectionHeader(
          title = "Mitra rekomendasi",
          actionText = "Lihat peta",
          onAction = onSeeMap
        )
      }

      item {
        val partner = data.partner

        if (partner != null) {
          HomePartnerCard(
            partner = partner,
            onClick = { onPartnerClick(partner.id) }
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
        HomeSectionHeader(
          title = "Berita & Edukasi",
          actionText = "Lihat Semua",
          onAction = onSeeAllArticles
        )
      }

      item {
        val article = data.article

        if (article != null) {
          HomeNewsCard(
            title = article.title,
            summary = article.summary,
            readingMinutes = article.readingMinutes,
            isDemo = article.isDemo,
            onClick = { onNewsClick(article.id) }
          )
        } else {
          Text(
            text = "Belum ada artikel yang ditampilkan",
            style = MaterialTheme.typography.bodyMedium,
            color = EcoColor.Green700
          )
        }
      }

      item {
        HomeTipCard(
          title = "Bilas, keringkan, lalu pilah.",
          description = "Kemasan bersih lebih mudah didaur ulang. Mulai dari botolmu hari ini.",
        )
      }
    }
  }
}

@Composable
private fun HomePlaceholder(
  isLoading: Boolean,
  error: String?,
  onRetry: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    if (isLoading) {
      CircularProgressIndicator(color = EcoColor.Green700)
    } else {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = error ?: "Beranda belum dapat dimuat.",
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green900,
          textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        EcoButton(
          text = "Coba lagi",
          onClick = onRetry,
          modifier = Modifier.fillMaxWidth()
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
  val previewData = HomeData(
    userName = "Contoh",
    balance = 1250,
    monthlyDeposits = 3,
    unreadCount = 2,
    missions = MissionBoard(
      periodStart = "2026-11-23T00:00:00+07:00",
      periodEnd = "2026-11-30T00:00:00+07:00",
      serverTime = "2026-11-24T10:00:00+07:00",
      weeklyXp = 0,
      totalXp = 0,
      missions = listOf(
        Mission(
          id = "three_kilograms",
          title = "Contoh misi",
          description = "Contoh deskripsi misi",
          unit = "gram",
          target = 3000,
          progress = 1200,
          rewardXp = 100,
          status = Mission.STATUS_IN_PROGRESS,
          claimedAt = null
        )
      )
    ),
    partner = null,
    article = HomeArticle(
      id = "00000000-0000-0000-0000-000000000000",
      title = "Contoh artikel",
      summary = "Contoh ringkasan artikel.",
      readingMinutes = 3,
      isDemo = true
    )
  )

  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    HomeScreen(
      uiState = HomeUiState(data = previewData, isLoading = false),
      onRetry = {},
      onNotification = {},
      onProfile = {},
      onRedeemPoints = {},
      onScanQr = {},
      onFindPartner = {},
      onHistory = {},
      onScanWaste = {},
      onSeeAllMissions = {},
      onSeeMap = {},
      onPartnerClick = {},
      onNewsClick = {},
      onSeeAllArticles = {}
    )
  }
}
