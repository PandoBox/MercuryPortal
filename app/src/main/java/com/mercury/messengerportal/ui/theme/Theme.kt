package com.mercury.messengerportal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MercuryColorScheme = lightColorScheme(
    primary = MercuryBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MercurySurface,
    secondary = MercuryAmber,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = MercurySurface,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = MercuryOnSurface,
    onSurface = MercuryOnSurface,
)

@Composable
fun MercuryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MercuryColorScheme,
        typography = MercuryTypography,
        content = content
    )
}
