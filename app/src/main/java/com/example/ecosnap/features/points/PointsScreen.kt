package com.example.ecosnap.features.points

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.points.components.PointsCurrentBalanceCard
import com.example.ecosnap.features.points.components.PointsPromoBannerCard
import com.example.ecosnap.features.points.components.PointsQuickActionsCard
import com.example.ecosnap.features.points.components.PointsSectionHeader
import com.example.ecosnap.features.points.components.PointsVoucherCard
import com.example.ecosnap.features.points.data.LocalPointsData
import com.example.ecosnap.features.points.model.VoucherCategorySection

@Composable
fun PointsScreen(
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  onScanQr: () -> Unit,
  onCalculator: () -> Unit,
  onVoucherClick: (String) -> Unit,
  onSeeAllCategory: (String) -> Unit,
  modifier: Modifier = Modifier,
  currentPoints: Int = LocalPointsData.CURRENT_USER_POINTS,
  sections: List<VoucherCategorySection> = LocalPointsData.voucherSections
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    EcoAppHeader(
      onNotifications = onNotifications,
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
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Tukar Poin",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = 22.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green900,
            textAlign = TextAlign.Center
          )

          Spacer(Modifier.height(2.dp))

          Text(
            text = "Tukarkan EcoPoints dengan reward pilihanmu.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 11.sp,
              lineHeight = 15.sp,
              fontWeight = FontWeight.Normal,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green700,
            textAlign = TextAlign.Center
          )
        }
      }

      item {
        PointsCurrentBalanceCard(
          points = currentPoints
        )
      }

      item {
        PointsPromoBannerCard()
      }

      item {
        PointsQuickActionsCard(
          onScanQr = onScanQr,
          onCalculator = onCalculator
        )
      }

      sections.forEach { section ->
        item(key = "header_${section.id}") {
          Column {
            Spacer(Modifier.height(4.dp))
            PointsSectionHeader(
              title = section.title,
              onSeeAll = { onSeeAllCategory(section.id) }
            )
          }
        }

        item(key = "vouchers_${section.id}") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            section.vouchers.forEach { voucher ->
              PointsVoucherCard(
                voucher = voucher,
                userPoints = currentPoints,
                onRedeemClick = { onVoucherClick(voucher.id) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }
}

@Preview(
  name = "Points Screen",
  showBackground = true,
  widthDp = 388,
  heightDp = 1350
)
@Composable
private fun PointsScreenPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    PointsScreen(
      onNotifications = {},
      onProfile = {},
      onScanQr = {},
      onCalculator = {},
      onVoucherClick = {},
      onSeeAllCategory = {}
    )
  }
}
