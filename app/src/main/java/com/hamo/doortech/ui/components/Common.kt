package com.hamo.doortech.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamo.doortech.AppContainer
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.theme.CardBorder
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.NavyLight
import com.hamo.doortech.ui.theme.Orange
import com.hamo.doortech.ui.theme.OrangeSoft
import com.hamo.doortech.ui.theme.WarnBg
import com.hamo.doortech.ui.theme.WarnRed
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer غير متوفر") }

class Messenger(private val state: SnackbarHostState, private val scope: CoroutineScope) {
    fun show(message: String) {
        scope.launch {
            state.currentSnackbarData?.dismiss()
            state.showSnackbar(message)
        }
    }
}

val LocalMessenger = staticCompositionLocalOf<Messenger> { error("Messenger غير متوفر") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamoTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Navy,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Orange) }
}

@Composable
fun MessageBox(icon: ImageVector, title: String, hint: String = "", modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (hint.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

@Composable
fun EmptyState(title: String, hint: String = "", modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) =
    MessageBox(Icons.Default.Inbox, title, hint, modifier, action)

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) =
    MessageBox(Icons.Default.ErrorOutline, "حدث خطأ", message, modifier)

/** يعرض حالة التحميل/الخطأ/الفراغ ثم المحتوى. */
@Composable
fun <T> StateContent(
    state: UiState<T>,
    emptyTitle: String = "لا توجد بيانات",
    emptyHint: String = "",
    isEmpty: (T) -> Boolean = { false },
    emptyAction: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is UiState.Loading -> LoadingBox()
        is UiState.Error -> ErrorState(state.message)
        is UiState.Success -> if (isEmpty(state.value)) EmptyState(emptyTitle, emptyHint, action = emptyAction) else content(state.value)
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun <T> DropdownField(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selected }?.second ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().clickable(enabled = enabled) { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(key); expanded = false })
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "تأكيد",
    danger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (danger) WarnRed else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(Modifier.padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun Chip(text: String, background: Color = MaterialTheme.colorScheme.surfaceVariant, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
fun SeedBadge() = Chip("تجريبي", OrangeSoft, Color(0xFF9A3412))

@Composable
fun VerificationBadge(level: Int) {
    val (bg, fg) = when (level) {
        Verification.DOCUMENTED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        Verification.GENERAL -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    Chip(Verification.label(level), bg, fg)
}

@Composable
fun WarningBox(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Surface(color = WarnBg, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFCA5A5)), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.WarningAmber, null, tint = WarnRed)
            Column {
                Text("تحذير", fontWeight = FontWeight.Bold, color = WarnRed)
                Text(text, color = Color(0xFF7F1D1D), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun NumberedStep(index: Int, text: String, done: Boolean? = null, onToggle: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onToggle != null) Modifier.clickable { onToggle() } else Modifier).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(color = if (done == true) Color(0xFF15803D) else Orange, shape = RoundedCornerShape(50)) {
            Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                Text(if (done == true) "✓" else "$index", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}

/** بطاقة خطوة فردية لأدلة البرمجة/التوصيلات: تفصل أي تحذير سلامة (مسبوق بعلامة ⚠ في النص
 * المصدري) في صندوق بارز منفصل، وتطوي النص الطويل جدًا مع زر "عرض المزيد" بدل كتلة نصية
 * واحدة ضخمة يصعب قراءتها. مُستخدمة في بطاقة الدليل المصغّرة وفي صفحة الدليل الكاملة معًا. */
@Composable
fun StepCard(index: Int, rawText: String) {
    val parts = rawText.split("⚠", limit = 2)
    val main = parts[0].trim()
    val warn = parts.getOrNull(1)?.trim()
    // النصوص الحقيقية من الكتالوجات بتستخدم "•" كفاصل حقيقي بين بارامترات/مواصفات
    // (موجود أصلاً في بيانات المصدر) — لو فيه أكتر من فاصل، نعرضها كقائمة واضحة
    // بدل فقرة مزدحمة، بدل ما نفبرك أي حقول مش موجودة في البيانات.
    val bulletItems = remember(main) {
        val segs = main.split("•").map { it.trim() }.filter { it.isNotEmpty() }
        if (segs.size >= 2) segs else null
    }
    var expanded by remember(index, rawText) {
        mutableStateOf(if (bulletItems != null) bulletItems.size <= 5 else main.length <= 220)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(Navy), contentAlignment = Alignment.Center) {
            Text("$index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF7F8FA))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .then(if (!expanded) Modifier.clickable { expanded = true } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (bulletItems != null) {
                    val shown = if (expanded) bulletItems else bulletItems.take(5)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        shown.forEach { seg -> ParameterRow(seg) }
                    }
                    if (!expanded) {
                        Text(
                            "+ ${bulletItems.size - 5} بنود أخرى — عرض الكل ▾",
                            color = NavyLight, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    } else if (bulletItems.size > 5) {
                        Text("عرض أقل ▴", color = NavyLight, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { expanded = false }.padding(top = 8.dp))
                    }
                } else {
                    Text(
                        main, color = Color(0xFF1E293B), fontSize = 14.sp, lineHeight = 23.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis,
                    )
                    if (!expanded) {
                        Text("عرض المزيد ▾", color = NavyLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    } else if (main.length > 220) {
                        Text("عرض أقل ▴", color = NavyLight, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { expanded = false }.padding(top = 4.dp))
                    }
                }
            }
            if (!warn.isNullOrBlank()) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(WarnBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top,
                ) {
                    Text("⚠", fontSize = 13.sp)
                    Text(warn, color = WarnRed, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * يعرض بندًا واحدًا من قائمة بارامترات (زي "ON = مفعّل" أو "DIP1: وصف").
 * لو فيه فاصل "=" أو ":" قريب من البداية، بيفصل المفتاح (غامق) عن الوصف —
 * بدون أي تعديل في النص الأصلي نفسه، مجرد تقسيم بصري للي موجود فعلاً.
 */
@Composable
private fun ParameterRow(segment: String) {
    val splitIdx = segment.indexOfFirst { it == '=' || it == ':' }
        .takeIf { it in 1..40 && it < segment.length - 1 }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.padding(top = 7.dp).size(5.dp).clip(RoundedCornerShape(2.dp)).background(Orange),
        )
        if (splitIdx != null) {
            val key = segment.substring(0, splitIdx + 1).trim()
            val value = segment.substring(splitIdx + 1).trim()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(key, color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 19.sp)
                if (value.isNotEmpty()) {
                    Text(value, color = Color(0xFF1E293B), fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        } else {
            Text(segment, color = Color(0xFF1E293B), fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ListCard(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    badges: @Composable () -> Unit = {},
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            leading()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { badges() }
            }
            trailing()
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.height(46.dp), shape = RoundedCornerShape(12.dp)) {
        Text(text)
    }
}

@Composable
fun ProvideMessenger(messenger: Messenger, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMessenger provides messenger, content = content)
}
