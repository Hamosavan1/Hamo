package com.hamo.doortech.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamo.doortech.ui.theme.DeepNavy
import com.hamo.doortech.ui.theme.Orange

/** شعار HAMO: ترس برتقالي وبداخله باب جراج. */
@Composable
fun HamoMark(size: Dp) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Settings, null, tint = Orange, modifier = Modifier.size(size))
        Box(
            Modifier.size(size * 0.46f).clip(RoundedCornerShape(size * 0.08f)).background(DeepNavy),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Garage, null, tint = Color.White, modifier = Modifier.size(size * 0.36f)) }
    }
}

/** الشعار كاملًا: العلامة + HAMO + DOOR TECH، أفقي (للشريط العلوي) أو رأسي (لشاشة البداية). */
@Composable
fun HamoLogo(markSize: Dp, vertical: Boolean = false, titleSize: Int = 20) {
    val texts: @Composable () -> Unit = {
        Column(horizontalAlignment = if (vertical) Alignment.CenterHorizontally else Alignment.Start) {
            Text("HAMO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = titleSize.sp, lineHeight = (titleSize * 1.05f).sp)
            Text("DOOR TECH", color = Orange, fontWeight = FontWeight.Bold, fontSize = (titleSize * 0.52f).sp, lineHeight = (titleSize * 0.6f).sp)
        }
    }
    if (vertical) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HamoMark(markSize); texts()
        }
    } else {
        // في RTL يظهر النص يمين العلامة؛ نعكس الترتيب حتى تبقى العلامة يسار الكلمة كما في الهوية.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            texts(); HamoMark(markSize)
        }
    }
}

private data class LogoStyle(val text: String, val color: Color, val bg: Color = Color.White, val italic: Boolean = false)

private val logoStyles = mapOf(
    "NICE" to LogoStyle("Nice", Color.White, Color(0xFF0057B8)),
    "FAAC" to LogoStyle("FAAC", Color(0xFF1F2937), italic = true),
    "BFT" to LogoStyle("BFT", Color.White, Color(0xFFE30613), italic = true),
    "CAME" to LogoStyle("CAME", Color(0xFF111827)),
    "BENINCA" to LogoStyle("BENINCA", Color(0xFF1F2937)),
    "SOMMER" to LogoStyle("SOMMER", Color(0xFFD7141A)),
    "KEY" to LogoStyle("KEY", Color(0xFF111827)),
    "ALLMATIC" to LogoStyle("ALLMATIC", Color(0xFF1E3A8A), italic = true),
    "DEA" to LogoStyle("DEA", Color(0xFF1D4ED8)),
    "HÖRMANN" to LogoStyle("HÖRMANN", Color.White, Color(0xFF00529F)),
    "HORMANN" to LogoStyle("HÖRMANN", Color.White, Color(0xFF00529F)),
    "LIFTMASTER" to LogoStyle("LiftMaster", Color(0xFFD7141A), italic = true),
    "V2" to LogoStyle("V2", Color(0xFFD7141A), italic = true),
    "LIFE" to LogoStyle("LIFE", Color(0xFF16A34A)),
    "SOMFY" to LogoStyle("somfy", Color(0xFF111827)),
    "MARANTEC" to LogoStyle("MARANTEC", Color(0xFF0F3D7A)),
    "ROGER" to LogoStyle("ROGER", Color(0xFF0F766E)),
)

private val fallbackColors = listOf(
    Color(0xFFEA580C), Color(0xFF1D4ED8), Color(0xFFDC2626), Color(0xFF0F766E),
    Color(0xFF7C3AED), Color(0xFF0369A1), Color(0xFFB45309), Color(0xFF334155),
)

/** شعار نصي للماركة بألوانها المعروفة؛ للماركات غير المعروفة يُستخدم لون ثابت مشتق من الاسم. */
@Composable
fun BrandLogo(name: String, size: Dp = 64.dp) {
    val key = name.trim().uppercase().substringBefore(' ')
    val st = logoStyles[key] ?: LogoStyle(
        name.trim().take(9).uppercase(), fallbackColors[name.sumOf { it.code } % fallbackColors.size],
    )
    Box(
        Modifier.size(size).clip(RoundedCornerShape(10.dp)).background(st.bg).padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        val fs = when {
            st.text.length <= 3 -> size.value * 0.34f
            st.text.length <= 5 -> size.value * 0.26f
            else -> size.value * 0.17f
        }
        Text(
            st.text, color = st.color, fontWeight = FontWeight.ExtraBold, fontSize = fs.sp, maxLines = 1,
            fontStyle = if (st.italic) FontStyle.Italic else FontStyle.Normal, textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = 0.dp),
        )
    }
}
