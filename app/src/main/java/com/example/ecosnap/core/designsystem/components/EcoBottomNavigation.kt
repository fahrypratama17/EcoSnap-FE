package com.example.ecosnap.core.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor

data class EcoBottomItem(
  val route: String,
  val label: String,
  val icon: ImageVector
)

@Composable
fun EcoBottomNavigation(
  items: List<EcoBottomItem>,
  selectedRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier,
    containerColor = Color(0xFFF5F6F0),
    tonalElevation = 0.dp
  ) {
    items.forEach { item ->
      val selected = selectedRoute == item.route

      NavigationBarItem(
        selected = selected,
        onClick = {
          if (!selected) {
            onNavigate(item.route)
          }
        },
        icon = {
          Icon(
            imageVector = item.icon,
            contentDescription = null
          )
        },
        label = {
          Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall
          )
        },

        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = EcoColor.Green800,
          selectedTextColor = EcoColor.Green900,
          indicatorColor = EcoColor.Green100,
          unselectedIconColor = EcoColor.Green700,
          unselectedTextColor = EcoColor.Green700
        )
      )
    }
  }
}