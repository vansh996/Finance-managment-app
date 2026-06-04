package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = FinanceMint,
    secondary = FinanceElectricBlue,
    tertiary = FinanceAmber,
    background = FinanceSlateDarkBg,
    surface = FinanceSlateCard,
    error = FinanceCoral,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4), // Royal deep violet
    secondary = Color(0xFFEADDFF), // Rich lavenderpurple container background
    tertiary = Color(0xFF006E1C),  // Deep emerald positive green
    background = Color(0xFFFDFBFF), // Clean high density backdrop
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    onBackground = Color(0xFF1B1B1F),
    error = Color(0xFFBA1A1A), // Beautiful cherry red for budget alerts & expenses
    outline = Color(0xFFC7C6CA), // Subtle border outline
    onSecondary = Color(0xFF21005D) // Intense deep purple for contrasting text
  )


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default to preserve the rich High Density branding scheme
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
