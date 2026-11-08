package com.example.ecosnap.features.points.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.points.data.LocalPointsData
import com.example.ecosnap.features.points.model.VoucherItem

@Composable
fun PointsVoucherCard(
  voucher: VoucherItem,
  userPoints: Int,
  onRedeemClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val canRedeem = userPoints >= voucher.pointsCost
  val pointsShortfall = (voucher.pointsCost - userPoints).coerceAtLeast(0)

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    border = BorderStroke(1.dp, Color(0xFFD5DCD8)),
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .background(
            color = Color(0xFF4E4EEE),
            shape = RoundedCornerShape(8.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.HandCoins,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(34.dp)
        )
      }

      Spacer(Modifier.height(8.dp))

      Text(
        text = voucher.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 11.sp,
          lineHeight = 15.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.sp
        ),
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(2.dp))

      Text(
        text = voucher.subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 10.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.sp
        ),
        color = EcoColor.Green900
      )

      Spacer(Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Lucide.Leaf,
          contentDescription = null,
          tint = EcoColor.Green700,
          modifier = Modifier.size(12.dp)
        )

        Spacer(Modifier.width(4.dp))

        Text(
          text = "${LocalPointsData.formatPoints(voucher.pointsCost)} poin",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green900
        )
      }

      Spacer(Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(23.dp)
          .clip(RoundedCornerShape(50))
          .background(
            color = if (canRedeem) {
              Color(0xFF3E6852)
            } else {
              Color(0xFFC4D1CA)
            }
          )
          .clickable(onClick = onRedeemClick),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Lihat & Tukar",
          style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
          ),
          color = if (canRedeem) {
            Color.White
          } else {
            Color(0xFF6A7A72)
          }
        )
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(18.dp)
          .padding(top = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        if (!canRedeem) {
          Text(
            text = "Kurang ${LocalPointsData.formatPoints(pointsShortfall)} poin",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 9.sp,
              lineHeight = 12.sp,
              fontWeight = FontWeight.Normal,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green900
          )
        }
      }
    }
  }
}
