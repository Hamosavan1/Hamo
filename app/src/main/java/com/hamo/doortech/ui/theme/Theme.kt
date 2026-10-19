package com.hamo.doortech.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF14254A)
val NavyLight = Color(0xFF2B4C8C)
val Orange = Color(0xFFF97316)
val OrangeSoft = Color(0xFFFFEDD5)
val Surface = Color(0xFFF3F6FB)
val OkGreen = Color(0xFF16A34A)
val WarnRed = Color(0xFFB91C1C)
val WarnBg = Color(0xFFFEF2F2)

// ألوان مميزة لكل قسم: البرمجة أزرق، التوصيلات كهرماني، الريموت بنفسجي، التركيب أخضر مزرق.
val ProgBlue = Color(0xFF2563EB)
val ProgSoft = Color(0xFFDBEAFE)
val WireAmber = Color(0xFFD97706)
val WireSoft = Color(0xFFFEF3C7)
val RemoteViolet = Color(0xFF7C3AED)
val RemoteSoft = Color(0xFFEDE9FE)
val InstallTeal = Color(0xFF0D9488)
val InstallSoft = Color(0xFFCCFBF1)
val FaultRose = Color(0xFFE11D48)
val FaultSoft = Color(0xFFFFE4E6)
val MutedText = Color(0xFF64748B)

private val scheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F7),
    onPrimaryContainer = Navy,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = NavyLight,
    background = Surface,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF2F8),
    onSurfaceVariant = Color(0xFF475569),
    error = WarnRed,
    outline = Color(0xFFD5DDE8),
)

private val typography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
)

private val shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

/** سمة التطبيق: واجهة فاتحة عصرية بألوان كحلي/برتقالي ولون مميز لكل قسم، مع RTL حقيقي. */
@Composable
fun HamoTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes, content = content)
    }
}
