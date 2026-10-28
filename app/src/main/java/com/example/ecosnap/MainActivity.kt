package com.example.ecosnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ecosnap.core.designsystem.theme.EcosnapTheme
import com.example.ecosnap.navigation.EcoSnapNavHost

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      EcosnapTheme(
        darkTheme = false,
        dynamicColor = false
      ) {
        EcoSnapNavHost()
      }
    }
  }
}
