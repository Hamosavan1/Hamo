package com.hamo.doortech.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

// لوحة الألوان الرسمية لهوية HAMO DOOR TECH (حسب الماستر-برومبت: كحلي صناعي + برتقالي).
val Navy = Color(0xFF071A31)
val NavyLight = Color(0xFF12365D)
val Orange = Color(0xFFFF8A1D)
val OrangeLight = Color(0xFFFFB36B)
val OrangeSoft = Color(0xFFFFEDD5)
val Surface = Color(0xFFF3F6FB)
val TextPrimary = Color(0xFF10233F)
val TextSecondary = Color(0xFF64748B)
val CardBorder = Color(0xFFE2E8F0)
val OkGreen = Color(0xFF15803D)
val WarnRed = Color(0xFFB91C1C)
val WarnBg = Color(0xFFFEF2F2)

private val scheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F5),
    onPrimaryContainer = Navy,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = NavyLight,
    background = Surface,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEFF3F8),
    onSurfaceVariant = TextSecondary,
    error = WarnRed,
    outline = CardBorder,
)

private val typography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
)

/** سمة التطبيق: ألوان كحلي/برتقالي وواجهة RTL حقيقية. */
@Composable
fun HamoTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
