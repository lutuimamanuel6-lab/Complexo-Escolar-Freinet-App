package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.SchoolApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isSystemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(isSystemDark) }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        SchoolApp(
          onToggleDarkTheme = {
            isDarkTheme = !isDarkTheme
          }
        )
      }
    }
  }
}
