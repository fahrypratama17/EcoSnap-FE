package com.example.ecosnap.features.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Store
import com.composables.icons.lucide.UserRound
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape
import com.example.ecosnap.features.auth.model.AuthRole

@Composable
fun AuthRoleSelector(
  selectedRole: AuthRole,
  onRoleSelected: (AuthRole) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
      text = "Daftar sebagai",
      style = MaterialTheme.typography.labelLarge,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(8.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AuthRoleCard(
      role = AuthRole.USER,
      selected = selectedRole == AuthRole.USER,
      onClick = {
        onRoleSelected(AuthRole.USER)
      },
      modifier = Modifier.weight(1f)
    )

      AuthRoleCard(
        role = AuthRole.PARTNER,
        selected = selectedRole == AuthRole.PARTNER,
        onClick = {
          onRoleSelected(AuthRole.PARTNER)
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun AuthRoleCard(
  role: AuthRole,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    modifier = modifier.height(76.dp),
    shape = EcoShape.medium,
    color = if (selected) {
      EcoColor.Green100
    } else {
      EcoColor.White
    },
    border = BorderStroke(
      1.dp,
      if (selected) EcoColor.Green700 else EcoColor.Green400
    )
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (role == AuthRole.USER) {
          Lucide.UserRound
        } else {
          Lucide.Store
        },
        contentDescription = null,
        tint = EcoColor.Green700,
        modifier = Modifier.size(18.dp)
      )

      Spacer(Modifier.width(6.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = if (role == AuthRole.USER) "Pengguna" else "Mitra",
          fontSize = 12.sp,
          color = EcoColor.Green900
        )

        Text(
          text = if (role == AuthRole.USER) {
            "Kebutuhan pribadi"
          } else {
            "Pemilik Usaha"
          },
          fontSize = 10.sp,
          lineHeight = 13.sp,
          color = EcoColor.Green700
        )
      }

      Box(
        modifier = modifier
          .size(16.dp)
          .border(
            1.dp,
            EcoColor.Green700,
            CircleShape
          )
          .padding(3.dp),
        contentAlignment = Alignment.Center
      ) {
        if (selected) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                EcoColor.Green700,
                CircleShape
              )
          )
        }
      }
    }
  }
}