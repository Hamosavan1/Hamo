package com.hamo.doortech.ui.theme

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

// HAMO DOOR TECH — هوية "عدة أدوات فني": غرافيت صناعي + أمبر تحذيري،
// خلفية رمادية باردة (مش كريمي) عشان وضوح القراءة في الشمس بالموقع.
val Navy = Color(0xFF16232F)           // غرافيت صناعي داكن (الهيدر، النصوص الأساسية)
val NavyLight = Color(0xFF2B3F52)      // ستيل بلو (تدرجات، حالات ضغط)
val Orange = Color(0xFFE2820A)         // أمبر تحذيري (لون لافتات السلامة الصناعية)
val OrangeLight = Color(0xFFF2A73D)    // أمبر فاتح (تمييز ثانوي)
val OrangeSoft = Color(0xFFFBEBD2)     // خلفية أمبر فاتحة جدًا (شارات، تنبيهات خفيفة)
val Surface = Color(0xFFEEF1F4)        // رمادي بارد (خلفية التطبيق)
val TextPrimary = Color(0xFF16232F)
val TextSecondary = Color(0xFF5B6B78)
val CardBorder = Color(0xFFDCE3EA)
val OkGreen = Color(0xFF1E7B4D)
val WarnRed = Color(0xFFB3261E)
val WarnBg = Color(0xFFFBEAE8)
val PanelSurface = Color(0xFFFFFFFF)

private val scheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3EA),
    onPrimaryContainer = Navy,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Color(0xFF7A4A05),
    tertiary = NavyLight,
    background = Surface,
    onBackground = TextPrimary,
    surface = PanelSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF2F4F6),
    onSurfaceVariant = TextSecondary,
    error = WarnRed,
    outline = CardBorder,
)

private val typography = Typography(
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
)

/** سمة التطبيق: هوية "عدة أدوات فني" — غرافيت + أمبر، واجهة RTL حقيقية. */
@Composable
fun HamoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
