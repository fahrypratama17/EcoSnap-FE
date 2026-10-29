package com.example.ecosnap.features.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Recycle
import com.composables.icons.lucide.ScanLine
import com.example.ecosnap.core.designsystem.components.EcoCard
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun HomeScanPromoCard(
  onScan: () -> Unit,
  modifier: Modifier = Modifier,
  @DrawableRes illustraionRes: Int? = null
) {
  EcoCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Sampah apa hari ini?",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 16.sp,
            lineHeight = 22.sp
          ),
          color = EcoColor.Green900
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = "Kenali jenisnya, temukan cara memilahnya.",
          fontSize = 12.sp,
          lineHeight = 16.sp,
          color = EcoColor.Green700
        )

        Spacer(Modifier.height(10.dp))

        Surface(
          onClick = onScan,
          shape = RoundedCornerShape(10.dp),
          color = EcoColor.Green700
        ) {
          Row(
            modifier = Modifier.padding(
              horizontal = 12.dp,
              vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Lucide.ScanLine,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = Color.White
            )

            Spacer(Modifier.width(6.dp))

            Text(
              text = "Scan Sampah",
              color = Color.White,
              fontSize = 12.sp
            )
          }
        }
      }

      Spacer(Modifier.width(8.dp))

      if (illustraionRes != null) {
        Image(
          painter = painterResource(
            illustraionRes
          ),
          contentDescription = null,
          modifier = Modifier.size(
            width = 96.dp,
            height = 114.dp
          ),
          contentScale = ContentScale.Fit
        )
      } else {
        Box(
          modifier = Modifier.size(
            width = 88.dp,
            height = 100.dp
          ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Lucide.Recycle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = EcoColor.Green500
          )
        }
      }
    }
  }
}