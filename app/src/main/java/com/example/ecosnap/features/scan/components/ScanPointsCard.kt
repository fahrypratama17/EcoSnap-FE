package com.example.ecosnap.features.scan.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor
import java.text.NumberFormat
import java.util.Locale

private val IndonesianLocale: Locale = Locale.forLanguageTag("id-ID")

@Composable
fun ScanPointsCard(
  points: Int,
  weightKg: Double,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = EcoColor.Green800,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "Poin Diperoleh",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green100
        )

        Text(
          text = "+${NumberFormat.getIntegerInstance(IndonesianLocale).format(points)} Poin",
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 30.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.sp
          ),
          color = Color.White
        )
      }

      HorizontalDivider(
        thickness = 0.5.dp,
        color = Color.White
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(R.drawable.scan_weight_icon),
          contentDescription = null,
          modifier = Modifier.size(36.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Berat Sampah",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 12.sp,
              lineHeight = 15.sp,
              letterSpacing = 0.sp
            ),
            color = EcoColor.Green100
          )

          Text(
            text = String.format(IndonesianLocale, "%.2f Kg", weightKg),
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = 16.sp,
              lineHeight = 20.sp,
              letterSpacing = 0.sp
            ),
            color = Color.White
          )
        }
      }
    }
  }
}
