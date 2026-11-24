package com.example.ecosnap.features.adminhome.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
internal fun MitraCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = EcoColor.White,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.dp, EcoColor.Green900),
    shadowElevation = 3.dp
  ) {
    Column(Modifier.padding(contentPadding), content = content)
  }
}

@Composable
internal fun MitraPageTitle(title: String, subtitle: String) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      title,
      style = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 23.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold
      ),
      color = EcoColor.Green900
    )
    Text(
      subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 17.sp),
      color = EcoColor.Green800
    )
  }
}
