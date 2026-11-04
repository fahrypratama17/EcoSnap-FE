package com.example.ecosnap.features.articles.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.example.ecosnap.core.designsystem.theme.EcoColor

@Composable
fun ArticleSearchField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    placeholder = {
      Text(
        text = "Cari artikel...",
        fontSize = 13.sp
      )
    },
    leadingIcon = {
      Icon(
        imageVector = Lucide.Search,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
      )
    },
    singleLine = true,
    shape = RoundedCornerShape(14.dp),
    textStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions = KeyboardOptions(
      imeAction = ImeAction.Search
    ),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = Color.White,
      unfocusedContainerColor = Color.White,
      focusedBorderColor = EcoColor.Green700,
      unfocusedBorderColor = EcoColor.Green400,
      focusedTextColor = EcoColor.Green900,
      unfocusedTextColor = EcoColor.Green900
    )
  )
}