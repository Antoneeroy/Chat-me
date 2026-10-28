package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChatMeDarkColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = OnPrimaryEmerald,
    primaryContainer = PrimaryContainerEmerald,
    onPrimaryContainer = PrimaryEmerald,
    secondary = SecondaryCyan,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryContainerCyan,
    onSecondaryContainer = SecondaryCyan,
    tertiary = AccentAmber,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent stealth aesthetic for secure messenger
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChatMeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
