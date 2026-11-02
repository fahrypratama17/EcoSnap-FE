package com.example.ecosnap.features.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Store
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.home.model.NearbyPartnerUi

@Composable
fun HomePartnerCard(
  partner: NearbyPartnerUi,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = Color.White,
    border = BorderStroke(
      1.dp,
      EcoColor.Green300
    ),
    shadowElevation = 2.dp
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .background(
            EcoColor.Green100,
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Lucide.Store,
          contentDescription = null,
          modifier = Modifier.size(25.dp),
          tint = EcoColor.Green800
        )
      }

      Spacer(Modifier.width(10.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = partner.name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp
          ),
          color = EcoColor.Green900,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = partner.location,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = EcoColor.Green700,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(5.dp))

        Row(
          modifier = Modifier.horizontalScroll(
            rememberScrollState()
          ),
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          partner.acceptedMaterials.forEach { material ->
            Surface(
              color = EcoColor.Green100,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = material,
                color = EcoColor.Green800,
                fontSize = 10.sp,
                modifier = Modifier.padding(
                  horizontal = 7.dp,
                  vertical = 3.dp
                )
              )
            }
          }
        }
      }
    }
  }
}