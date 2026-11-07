package com.example.ecosnap.features.profile.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.features.profile.model.UserProfile

@Composable
fun ProfileSummaryCard(
  userProfile: UserProfile,
  onEditProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  ProfileCardContainer(
    modifier = modifier,
    containerColor = EcoColor.Green800
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(EcoColor.Green300),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = userProfile.name.firstOrNull()?.uppercase() ?: "U",
          style = MaterialTheme.typography.titleMedium,
          color = EcoColor.Green900
        )
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = userProfile.name,
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 16.sp
          ),
          color = Color.White
        )

        Spacer(Modifier.height(2.dp))

        Text(
          text = userProfile.email,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 11.sp,
            lineHeight = 16.sp
          ),
          color = EcoColor.Green100
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    Surface(
      onClick = onEditProfile,
      shape = RoundedCornerShape(999.dp),
      color = EcoColor.Green100
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Lucide.Pencil,
          contentDescription = null,
          tint = EcoColor.Green800,
          modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
          text = "Edit Profil",
          style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 13.sp
          ),
          color = EcoColor.Green800
        )
      }
    }
  }
}