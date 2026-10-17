package com.hamo.doortech.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

// HAMO DOOR TECH — dark industrial palette.
val Navy = Color(0xFF0A1220)
val NavyLight = Color(0xFF17263B)
val Orange = Color(0xFFFF922E)
val OrangeLight = Color(0xFFFFB86B)
val OrangeSoft = Color(0xFF3A281A)
val Surface = Color(0xFF0B1220)
val TextPrimary = Color(0xFFE8EEF7)
val TextSecondary = Color(0xFF9AAAC0)
val CardBorder = Color(0xFF26354A)
val OkGreen = Color(0xFF4ADE80)
val WarnRed = Color(0xFFF87171)
val WarnBg = Color(0xFF351D25)
val PanelSurface = Color(0xFF121D2D)

private val scheme = darkColorScheme(
    primary = Orange,
    onPrimary = Color(0xFF211306),
    primaryContainer = Color(0xFF553216),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFF8EBBFF),
    onSecondary = Color(0xFF09203E),
    secondaryContainer = Color(0xFF1C3554),
    onSecondaryContainer = Color(0xFFD7E6FF),
    tertiary = Color(0xFF73D7C4),
    background = Surface,
    onBackground = TextPrimary,
    surface = PanelSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF202D40),
    onSurfaceVariant = TextSecondary,
    error = WarnRed,
    outline = CardBorder,
)

private val typography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun HamoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
