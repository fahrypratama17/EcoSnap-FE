package com.example.ecosnap.features.points.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.R
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun PointsPromoBannerCard(
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFFA4D2B5),
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          start = 16.dp,
          end = 10.dp,
          top = 12.dp,
          bottom = 12.dp
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Tukar poin,\ndapatkan manfaat nyata!",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
          ),
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(6.dp))

        Text(
          text = "Pilih berbagai voucher dan dukung hidup yang lebih berkelanjutan",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
          ),
          color = Color(0xFF254031)
        )
      }

      Spacer(Modifier.width(8.dp))

      Image(
        painter = painterResource(R.drawable.points_promo_illustration),
        contentDescription = null,
        modifier = Modifier.size(
          width = 110.dp,
          height = 92.dp
        ),
        contentScale = ContentScale.Fit
      )
    }
  }
}
