package com.example.ecosnap.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ecosnap.core.designsystem.theme.EcoColor
import com.example.ecosnap.core.designsystem.theme.EcoShape

@Composable
fun EcoTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier : Modifier = Modifier,
  placeholder: String = "",
  keyboardType: KeyboardType = KeyboardType.Text,
  imeAction: ImeAction = ImeAction.Next,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailiingIcon: (@Composable () -> Unit)? = null,
  errorMessage: String? = null
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = EcoColor.Green900
    )

    Spacer(Modifier.height(6.dp))

    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodyMedium,
          color = EcoColor.Green700
        )
      },
      trailingIcon = trailiingIcon,
      isError = errorMessage != null,
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyMedium,
      shape = EcoShape.medium,
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboardType,
        imeAction = imeAction
      ),
      visualTransformation = visualTransformation,
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = EcoColor.White,
        unfocusedContainerColor = EcoColor.White,
        focusedBorderColor = EcoColor.Green700,
        unfocusedBorderColor = EcoColor.Green400,
        focusedTextColor = EcoColor.Green900,
        unfocusedTextColor = EcoColor.Green900,
        cursorColor = EcoColor.Green700
      )
    )

    if (errorMessage != null) {
      Spacer(Modifier.height(4.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.labelSmall,
        color = EcoColor.TextError
      )
    }
  }
}