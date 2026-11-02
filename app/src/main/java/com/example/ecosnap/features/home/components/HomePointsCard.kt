package com.example.ecosnap.features.home.components

import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.QrCode
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.util.Locale

@Composable
fun HomePointsCard(
  points: Int,
  monthlyDeposits: Int,
  onRedeem: () -> Unit,
  onSqanQr: () -> Unit,
  onFindPartner: () -> Unit,
  onHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = EcoColor.Green800,
    borderColor = EcoColor.Green800,
    shadowElevation = 2.dp,
    contentPadding = PaddingValues(16.dp)
  ) {
    Text(
      text = "Poin kebaikanmu",
      fontSize = 13.sp,
      color = EcoColor.Green200
    )

    Spacer(Modifier.height(2.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = NumberFormat
          .getNumberInstance(
            Locale.forLanguageTag("id-ID")
          )
          .format(points),
        style = MaterialTheme.typography.headlineLarge.copy(
          fontSize = 30.sp,
          lineHeight = 36.sp,
          fontWeight = FontWeight.Bold
        ),
        color = Color.White
      )

      Spacer(Modifier.width(5.dp))

      Surface(
        onClick = onRedeem,
        shape = RoundedCornerShape(14.dp),
        color = EcoColor.Green100
      ) {
        Row(
          modifier = Modifier.padding(
            horizontal = 12.dp,
            vertical = 11.dp
          ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Tukar poin",
            fontSize = 12.sp,
            color = EcoColor.Green900
          )

          Spacer(Modifier.width(5.dp))

          Icon(
            imageVector = Lucide.ArrowUpRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = EcoColor.Green900
          )
        }
      }
    }

    Spacer(Modifier.height(6.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Lucide.Leaf,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = EcoColor.Green200
      )

      Spacer(Modifier.width(5.dp))

      Text(
        text = "$monthlyDeposits setoran baik bulan ini. Teruskan ya!",
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = EcoColor.Green100
      )
    }

    Spacer(Modifier.height(14.dp))

    HorizontalDivider(color = Color.White.copy(alpha = 0.35f))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      HomeQuickAction(
        title = "Scan QR",
        icon = Lucide.QrCode,
        onClick = onSqanQr,
        modifier = Modifier.weight(1f)
      )
      HomeQuickAction(
        title = "Cari Mitra",
        icon = Lucide.MapPin,
        onClick = onFindPartner,
        modifier = Modifier.weight(1f)
      )
      HomeQuickAction(
        title = "Riwayat",
        icon = Lucide.History,
        onClick = onHistory,
        modifier = Modifier.weight(1f)
      )
    }
  }
}