package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SchoolBlueDarkPrimary,
    secondary = SchoolOrangeDarkSecondary,
    tertiary = SchoolBlueDarkTertiary,
    background = CoolBgDark,
    surface = CoolSurfaceDark,
    onPrimary = Color(0xFF0D2554),
    onSecondary = Color(0xFF4E2600),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SchoolBluePrimary,
    secondary = SchoolOrangeSecondary,
    tertiary = SchoolBlueTertiary,
    background = CoolBgLight,
    surface = CoolSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force school branding color scheme by default rather than Android wallpaper dynamic colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = {
      androidx.compose.runtime.CompositionLocalProvider(
        content = content
      )
    }
  )
}
