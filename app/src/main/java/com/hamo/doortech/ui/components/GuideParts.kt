package com.hamo.doortech.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.ui.theme.InstallSoft
import com.hamo.doortech.ui.theme.InstallTeal
import com.hamo.doortech.ui.theme.MutedText
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.OkGreen
import com.hamo.doortech.ui.theme.ProgBlue
import com.hamo.doortech.ui.theme.ProgSoft
import com.hamo.doortech.ui.theme.RemoteSoft
import com.hamo.doortech.ui.theme.RemoteViolet
import com.hamo.doortech.ui.theme.WireAmber
import com.hamo.doortech.ui.theme.WireSoft
import com.hamo.doortech.util.ltr

/** هوية بصرية لكل قسم أدلة: لون، خلفية فاتحة، أيقونة، ووصف قصير مبسّط. */
data class GuideStyle(val color: Color, val soft: Color, val icon: ImageVector, val title: String, val hint: String)

fun guideStyle(type: String): GuideStyle = when (type) {
    GuideType.PROGRAMMING -> GuideStyle(ProgBlue, ProgSoft, Icons.Default.Settings, "برمجة المكينة", "ضبط المشوار والوقت والقوة خطوة بخطوة")
    GuideType.WIRING -> GuideStyle(WireAmber, WireSoft, Icons.Default.ElectricalServices, "التوصيلات", "كل طرف في الكارتة بيتوصل فين")
    GuideType.REMOTE -> GuideStyle(RemoteViolet, RemoteSoft, Icons.Default.SettingsRemote, "الريموت والحساسات", "حفظ ومسح الريموت وضبط الفوتوسيل")
    GuideType.INSTALL -> GuideStyle(InstallTeal, InstallSoft, Icons.Default.Construction, "التركيب", "تركيب المكينة والستوبات قبل التشغيل")
    else -> GuideStyle(Navy, Color(0xFFE2E8F0), Icons.Default.MenuBook, "دليل فني", "")
}

/** أيقونة القسم داخل مربع ملون بزوايا مستديرة. */
@Composable
fun GuideIcon(style: GuideStyle, size: Int = 48) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size / 3).dp)).background(style.soft),
        contentAlignment = Alignment.Center,
    ) { Icon(style.icon, null, tint = style.color, modifier = Modifier.size((size / 2).dp)) }
}

/**
 * يقسم نص الخطوة إلى عنوان قصير (قبل أول «:») ومتن، حتى تُقرأ الخطوة بنظرة سريعة.
 * مثال: «المرحلة 1 — ريست اللوحة: افصل الكهرباء...» ← عنوان «المرحلة 1 — ريست اللوحة».
 */
internal fun splitStep(text: String): Pair<String?, String> {
    val i = text.indexOf(':')
    if (i in 2..48) {
        val head = text.substring(0, i).trim()
        val body = text.substring(i + 1).trim()
        if (body.isNotEmpty()) return head to body
    }
    return null to text.trim()
}

/** يقسم المتن الطويل إلى جمل منفصلة (نقطة أو علامة استفهام متبوعة بمسافة). */
internal fun sentences(body: String): List<String> =
    body.split(Regex("(?<=[.؟!])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }

/**
 * قائمة خطوات تفاعلية: كل خطوة في بطاقة برقم كبير، والضغط عليها يعلّمها «تمت»
 * مع شريط تقدم أعلى القائمة. مناسبة للفني وهو قدام اللوحة.
 */
@Composable
fun StepsChecklist(key: String, steps: List<String>, accent: Color = ProgBlue) {
    if (steps.isEmpty()) return
    var done by rememberSaveable(key) { mutableStateOf(emptyList<Int>()) }
    val doneCount = done.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (doneCount == steps.size) "خلصت كل الخطوات ✓" else "خلصت $doneCount من ${steps.size}",
                color = if (doneCount == steps.size) OkGreen else MutedText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            )
            if (doneCount > 0) TextButton(onClick = { done = emptyList() }) { Text("ابدأ من الأول", fontSize = 13.sp) }
        }
        LinearProgressIndicator(
            progress = { doneCount.toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
            color = if (doneCount == steps.size) OkGreen else accent,
            trackColor = Color(0xFFE2E8F0),
        )
        Text("اضغط على الخطوة بعد ما تنفّذها عشان تتعلّم عليها.", color = MutedText, fontSize = 12.sp)
        steps.forEachIndexed { i, s ->
            StepItem(i + 1, s, i in done, accent) { done = if (i in done) done - i else done + i }
        }
    }
}

@Composable
private fun StepItem(index: Int, text: String, isDone: Boolean, accent: Color, onToggle: () -> Unit) {
    val (head, body) = remember(text) { splitStep(text) }
    val parts = remember(body) { sentences(body) }
    Surface(
        color = if (isDone) Color(0xFFF0FDF4) else Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isDone) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onToggle).animateContentSize(),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(if (isDone) OkGreen else accent),
                contentAlignment = Alignment.Center,
            ) { Text(if (isDone) "✓" else "$index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val muted = if (isDone) TextDecoration.LineThrough else null
                if (head != null) {
                    Text(if (head.any { it in '\u0600'..'\u06FF' }) head else head.ltr(), color = if (isDone) MutedText else accent, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, textDecoration = muted)
                }
                if (isDone) {
                    // بعد التنفيذ نختصر الخطوة لسطر واحد حتى يركّز الفني على اللي باقي.
                    Text(body, color = MutedText, fontSize = 14.sp, maxLines = 1, textDecoration = muted)
                } else if (parts.size <= 1) {
                    Text(body, color = Color(0xFF1E293B), fontSize = 16.sp, lineHeight = 26.sp)
                } else {
                    parts.forEach { p ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.padding(top = 10.dp).size(6.dp).clip(CircleShape).background(accent.copy(alpha = 0.6f)))
                            Text(p, Modifier.weight(1f), color = Color(0xFF1E293B), fontSize = 16.sp, lineHeight = 26.sp)
                        }
                    }
                }
            }
        }
    }
}

/** صندوق «قبل ما تبدأ» بخلفية فاتحة بلون القسم. */
@Composable
fun BeforeYouStart(text: String, style: GuideStyle) {
    if (text.isBlank()) return
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(style.soft).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Lightbulb, null, tint = style.color)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("قبل ما تبدأ", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text, color = Color(0xFF1E293B), fontSize = 15.sp, lineHeight = 24.sp)
        }
    }
}

private data class Term(val pattern: Regex, val term: String, val meaning: String)

private fun word(w: String) = Regex("(?<![A-Za-z])$w(?![A-Za-z])")

// قاموس مصطلحات مبسّط يظهر فقط للمصطلحات الموجودة فعلًا في نص الدليل.
private val glossary = listOf(
    Term(word("NC"), "NC", "مقفول طبيعيًا: الطرف لازم يفضل متوصّل. لو مش مركّب عليه جهاز اعمله جسر."),
    Term(word("NO"), "NO", "مفتوح طبيعيًا: بيشتغل لحظة ما يتقفل، زي زرار أو أمر ريموت."),
    Term(Regex("فوتوسيل|PHOTO"), "فوتوسيل (PHOTO)", "عين أمان بتوقف أو بترجّع البوابة لو فيه حاجة في طريقها."),
    Term(Regex("ليمت|Limit|LIMIT"), "ليمت سويتش", "مفتاح نهاية المشوار: بيفصل الموتور لما البوابة توصل للآخر."),
    Term(Regex("إنكودر|انكودر|Encoder|ENCODER"), "إنكودر", "حساس بيعدّ لفّات الموتور عشان اللوحة تعرف البوابة واقفة فين."),
    Term(Regex("جسر|جسّر|تُجسَّر"), "جسر", "سلكة قصيرة بتوصل طرفين ببعض."),
    Term(Regex("فلاشر|Flasher|FLASH"), "فلاشر", "لمبة تحذير بتنوّر وتطفي طول ما البوابة بتتحرك."),
    Term(Regex("STEP|خطوة بخطوة"), "STEP (خطوة بخطوة)", "أمر واحد بيبدّل بين فتح ووقوف وغلق مع كل ضغطة."),
    Term(Regex("كبستور|مكثف"), "كبستور", "مكثف تشغيل موتور الـ 230V. لو ضعيف الموتور بيزنّ ومش بيلف."),
    Term(word("COM|COMUNE"), "COM", "الطرف المشترك اللي كل الأوامر بترجع له."),
    Term(Regex("Rolling Code|رولينج"), "Rolling Code", "كود ريموت بيتغيّر مع كل ضغطة عشان محدش ينسخه."),
    Term(Regex("Pause Time|زمن التوقف"), "زمن التوقف", "المدة اللي البوابة بتستناها مفتوحة قبل ما تقفل لوحدها."),
)

/** «كلمات هتقابلك»: شرح بسيط للمصطلحات الفنية الموجودة في الدليل. */
@Composable
fun TermsCard(text: String) {
    val found = remember(text) { glossary.filter { it.pattern.containsMatchIn(text) } }
    if (found.isEmpty()) return
    var open by remember { mutableStateOf(false) }
    Surface(
        color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { open = !open },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("كلمات هتقابلك في الدليل (${found.size})", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(if (open) "إخفاء ▲" else "اعرض الشرح ▼", color = MutedText, fontSize = 13.sp)
            }
            if (open) found.forEach { t ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8FAFC)).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(t.term, color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(t.meaning, color = Color(0xFF334155), fontSize = 14.sp, lineHeight = 22.sp)
                }
            }
        }
    }
}
