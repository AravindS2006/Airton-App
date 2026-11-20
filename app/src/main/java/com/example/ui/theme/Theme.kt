package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.domain.model.TextScalePreset
import com.example.domain.model.ThemePreference

@Immutable
data class AirTonSemanticColors(
    val odSeries: Color,
    val osSeries: Color,
    val demoBannerBg: Color,
    val demoBannerContent: Color,
    val demoBannerBorder: Color,
    val statusOkBg: Color,
    val statusOkContent: Color,
    val statusErrorBg: Color,
    val statusErrorContent: Color,
    val statusNeutralBg: Color,
    val statusNeutralContent: Color,
    val cardBorder: Color
)

private val LightSemanticColors = AirTonSemanticColors(
    odSeries = AirTonBlue,
    osSeries = ClinicalTeal,
    demoBannerBg = DemoAmberBg,
    demoBannerContent = DemoAmberText,
    demoBannerBorder = DemoAmberBorder,
    statusOkBg = OperationalOkBg,
    statusOkContent = OperationalOkText,
    statusErrorBg = OperationalErrorBg,
    statusErrorContent = OperationalErrorText,
    statusNeutralBg = NeutralSlateBg,
    statusNeutralContent = NeutralSlateText,
    cardBorder = SoftOutline
)

private val DarkSemanticColors = AirTonSemanticColors(
    odSeries = AirTonBlueDark,
    osSeries = ClinicalTealDark,
    demoBannerBg = DemoAmberBgDark,
    demoBannerContent = DemoAmberTextDark,
    demoBannerBorder = DemoAmberBorder,
    statusOkBg = OperationalOkBgDark,
    statusOkContent = OperationalOkTextDark,
    statusErrorBg = OperationalErrorBgDark,
    statusErrorContent = OperationalErrorTextDark,
    statusNeutralBg = NeutralSlateBgDark,
    statusNeutralContent = NeutralSlateTextDark,
    cardBorder = DarkOutline
)

val LocalAirTonColors = staticCompositionLocalOf { LightSemanticColors }

private val AirTonLightColorScheme = lightColorScheme(
    primary = AirTonBlue,
    onPrimary = Color.White,
    primaryContainer = AirTonBlueContainer,
    onPrimaryContainer = OnAirTonBlueContainer,
    secondary = ClinicalTeal,
    onSecondary = Color.White,
    secondaryContainer = ClinicalTealContainer,
    onSecondaryContainer = OnClinicalTealContainer,
    background = WarmCanvas,
    onBackground = DeepNavyInk,
    surface = PureSurface,
    onSurface = DeepNavyInk,
    surfaceVariant = MistContainer,
    onSurfaceVariant = MutedSlateInk,
    outline = SoftOutline,
    outlineVariant = SubtleOutlineVariant,
    error = OperationalErrorText,
    onError = Color.White,
    errorContainer = OperationalErrorBg,
    onErrorContainer = OperationalErrorText
)

private val AirTonDarkColorScheme = darkColorScheme(
    primary = AirTonBlueDark,
    onPrimary = Color(0xFF062342),
    primaryContainer = AirTonBlueContainerDark,
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = ClinicalTealDark,
    onSecondary = Color(0xFF03302E),
    secondaryContainer = ClinicalTealContainerDark,
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = DarkCanvas,
    onBackground = DarkPrimaryInk,
    surface = DarkSurface,
    onSurface = DarkPrimaryInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryInk,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    error = OperationalErrorTextDark,
    onError = Color(0xFF4C0D10),
    errorContainer = OperationalErrorBgDark,
    onErrorContainer = OperationalErrorTextDark
)

val AirTonShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun AirTonTheme(
    themePreference: ThemePreference = ThemePreference.LIGHT,
    textScalePreset: TextScalePreset = TextScalePreset.STANDARD,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) AirTonDarkColorScheme else AirTonLightColorScheme
    val semanticColors = if (isDark) DarkSemanticColors else LightSemanticColors
    val currentDensity = LocalDensity.current
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = (currentDensity.fontScale * textScalePreset.multiplier).coerceIn(0.85f, 2.0f)
    )

    CompositionLocalProvider(
        LocalAirTonColors provides semanticColors,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AirTonTypography,
            shapes = AirTonShapes,
            content = content
        )
    }
}
