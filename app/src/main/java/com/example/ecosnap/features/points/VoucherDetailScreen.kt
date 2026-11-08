package com.example.ecosnap.features.points

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Coins
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.example.ecosnap.core.designsystem.components.EcoAppHeader
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.features.points.data.LocalPointsData
import com.example.ecosnap.features.points.model.VoucherDetailUiModel

@Composable
fun VoucherDetailScreen(
  voucherDetail: VoucherDetailUiModel,
  onBack: () -> Unit,
  onConfirmRedeem: () -> Unit,
  onNotifications: () -> Unit,
  onProfile: () -> Unit,
  modifier: Modifier = Modifier,
  userPoints: Int = LocalPointsData.CURRENT_USER_POINTS
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
        vertical = 12.dp
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Row(
          modifier = Modifier
            .clickable(onClick = onBack)
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Lucide.ArrowLeft,
            contentDescription = null,
            tint = EcoColor.Green700,
            modifier = Modifier.size(14.dp)
          )

          Spacer(Modifier.width(6.dp))

          Text(
            text = "Kembali ke Reward",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 11.sp,
              lineHeight = 14.sp,
              fontWeight = FontWeight.Medium,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green700
          )
        }
      }

      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(164.dp),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF87C5A0),
          shadowElevation = 3.dp
        ) {
          Box(
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Lucide.Recycle,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(72.dp)
            )
          }
        }
      }

      item {
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = voucherDetail.title,
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 17.sp,
              lineHeight = 22.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green900
          )

          Spacer(Modifier.height(4.dp))

          Text(
            text = voucherDetail.subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 12.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.Normal,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green800
          )

          Spacer(Modifier.height(10.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Lucide.Coins,
              contentDescription = null,
              tint = EcoColor.Green800,
              modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
              text = "${LocalPointsData.formatPoints(voucherDetail.pointsCost)} poin",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
              ),
              color = EcoColor.Green800
            )
          }
        }
      }

      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = Color.White,
          border = BorderStroke(1.dp, EcoColor.Green900),
          shadowElevation = 2.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Text(
              text = "Ketentuan Reward",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
              ),
              color = EcoColor.Green900
            )

            Spacer(Modifier.height(8.dp))

            voucherDetail.termsParagraphs.forEachIndexed { index, paragraph ->
              if (index > 0) {
                Spacer(Modifier.height(8.dp))
              }

              Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontSize = 11.sp,
                  lineHeight = 16.sp,
                  fontWeight = FontWeight.Normal,
                  letterSpacing = 0.sp
                ),
                color = EcoColor.Green700
              )
            }
          }
        }
      }

      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          border = BorderStroke(1.dp, EcoColor.Green900),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                horizontal = 16.dp,
                vertical = 13.dp
              ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Saldo EcoPoints",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
              ),
              color = EcoColor.Green800
            )

            Text(
              text = "${LocalPointsData.formatPoints(userPoints)} poin",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
              ),
              color = EcoColor.Green900
            )
          }
        }
      }

      item {
        Surface(
          onClick = onConfirmRedeem,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(50),
          color = Color(0xFF3E6852),
          shadowElevation = 3.dp
        ) {
          Box(
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Konfirmasi Penukaran",
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
              ),
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@Preview(
  name = "Voucher Detail Screen",
  showBackground = true,
  widthDp = 388,
  heightDp = 760
)
@Composable
private fun VoucherDetailScreenPreview() {
  EcosnapTheme(
    darkTheme = false,
    dynamicColor = false
  ) {
    VoucherDetailScreen(
      voucherDetail = LocalPointsData.defaultVoucherDetail,
      onBack = {},
      onConfirmRedeem = {},
      onNotifications = {},
      onProfile = {}
    )
  }
}
