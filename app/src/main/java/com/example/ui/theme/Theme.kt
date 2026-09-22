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

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenOnPrimary,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = Color(0xFFD3EEDF),
    secondary = Color(0xFFFDBA74),
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFF95D5B2),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF1E2822),
    onBackground = Color(0xFFE2E3DE),
    onSurface = Color(0xFFE2E3DE),
)

private val LightColorScheme = lightColorScheme(
    primary = BasilGreenPrimary,
    onPrimary = BasilGreenOnPrimary,
    primaryContainer = BasilGreenContainer,
    onPrimaryContainer = BasilGreenOnContainer,
    secondary = WarmAmberSecondary,
    onSecondary = WarmAmberOnSecondary,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = WarmAmberOnContainer,
    tertiary = SageTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SageTertiaryContainer,
    background = CreamBackground,
    surface = CardSurface,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF191C1B),
    onSurface = Color(0xFF191C1B),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFC0C9C2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our culinary palette by default for rich branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
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
        content = content
    )
}
