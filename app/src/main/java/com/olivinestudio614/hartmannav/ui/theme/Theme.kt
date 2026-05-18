package com.olivinestudio614.hartmannav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MilitaryColorScheme = darkColorScheme(
    primary = ArmyGreen,
    onPrimary = OffWhite,
    primaryContainer = ArmyGreenDark,
    secondary = AmberAlert,
    onSecondary = ArmyGreenDark,
    background = OliveDrab,
    onBackground = OffWhite,
    surface = ArmyGreenDark,
    onSurface = OffWhite,
    error = DangerRed,
    onError = OffWhite,
)

@Composable
fun HartmanNavTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MilitaryColorScheme,
        typography = MilitaryTypography,
        content = content
    )
}
