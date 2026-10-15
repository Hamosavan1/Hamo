package com.hamo.doortech.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.FaultDetailViewModel
import com.hamo.doortech.ui.FaultEditViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.SolutionForm
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.AddRow
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.FormField
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.InfoRow
import com.hamo.doortech.ui.components.LevelDropdown
import com.hamo.doortech.ui.components.LoadingBox
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.MediaSection
import com.hamo.doortech.ui.components.NumberedStep
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SecondaryButton
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.SeedBadge
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.StringListEditor
import com.hamo.doortech.ui.components.VerificationBadge
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.ui.theme.OkGreen
import com.hamo.doortech.util.ltr

private const val GENERIC_SAFETY =
    "هذه أسباب محتملة وخطوات إرشادية وليست تشخيصًا مؤكدًا. اقطع الكهرباء قبل أي عمل على التوصيلات، " +
        "ولا تعطّل حساسات الأمان ولا تتجاوز وسائل الحماية، واستعن بفني مؤهل أو بدليل الشركة عند الحاجة."

@Composable
fun FaultDetailScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "fault_$id") { FaultDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل العطل", onBack = { nav.popBackStack() }) {
            IconButton(onClick = vm::toggleFavorite) { Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, "مفضلة") }
            IconButton(onClick = { nav.navigate(Routes.faultEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = {
                vm.duplicate { r ->
                    messenger.show(r.messageOr("تم تكرار العطل"))
                    if (r is Outcome.Ok) nav.navigate(Routes.fault(r.id))
                }
            }) { Icon(Icons.Default.ContentCopy, "تكرار") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "العطل غير موجود") { d ->
            if (d == null) return@StateContent
            val f = d.full.fault
            ScrollColumn(pad) {
                SectionCard(f.title) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (f.isSeed) SeedBadge()
                        VerificationBadge(f.level)
                        if (f.modelId == null) Chip("عام — غير خاص بموديل") else Chip("خاص بموديل", Color(0xFFFFEDD5), Color(0xFF9A3412))
                    }
                    InfoRow("نوع الباب", d.categoryName.orEmpty())
                    InfoRow("الشركة", d.brandName.orEmpty().ltr())
                    InfoRow("الموديل", d.modelName.orEmpty().ltr())
                    InfoRow("الوصف", f.description)
                    InfoRow("المصدر", f.source)
                    InfoRow("كلمات بحث بديلة", f.altKeywords)
                }
                WarningBox(f.warnings)
                if (d.full.symptoms.isNotEmpty()) {
                    SectionCard("الأعراض") { d.full.symptoms.forEach { Text("• ${it.text}") } }
                }
                if (d.full.codes.isNotEmpty()) {
                    SectionCard("أكواد الأعطال") {
                        d.full.codes.forEach { c -> Text("${c.code.ltr()}${if (c.meaning.isNotBlank()) " — ${c.meaning}" else ""}") }
                    }
                }
                if (f.tools.isNotBlank()) SectionCard("الأدوات المطلوبة") { Text(f.tools) }

                if (d.full.solutions.isEmpty()) {
                    SectionCard("الحلول") {
                        Text("لا توجد حلول مسجلة لهذا العطل بعد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        SecondaryButton("إضافة حل", { nav.navigate(Routes.faultEdit(id)) })
                    }
                } else {
                    d.full.solutions.forEach { s ->
                        SectionCard("حل: ${s.solution.title}") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { VerificationBadge(s.solution.level) }
                            InfoRow("ينطبق على", s.solution.appliesTo)
                            InfoRow("المصدر", s.solution.source)
                            WarningBox(s.solution.warnings)
                            InfoRow("الأدوات", s.solution.tools)
                            s.orderedSteps.forEachIndexed { i, st -> NumberedStep(i + 1, st.text) }
                            if (s.solution.level < Verification.DOCUMENTED) {
                                Text(
                                    "هذا الحل غير موثّق من دليل الشركة؛ تحقق من دليل الموديل قبل التنفيذ.",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    PrimaryButton("افحص خطوة بخطوة", { nav.navigate(Routes.faultSteps(id)) }, Modifier.fillMaxWidth())
                }
                WarningBox(GENERIC_SAFETY)
                MediaSection(OwnerType.FAULT, id)
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog("حذف العطل", "سيُحذف العطل بكل أعراضه وأكواده وحلوله ومرفقاته نهائيًا.", "حذف", true,
            onConfirm = {
                confirmDelete = false
                vm.delete { r -> messenger.show(r.messageOr("تم حذف العطل")); if (r is Outcome.Ok) nav.popBackStack() }
            },
            onDismiss = { confirmDelete = false })
    }
}

/** شاشة خطوات الفحص والإصلاح: قائمة تحقق تفاعلية لخطوات الحل المختار. */
@Composable
fun FaultStepsScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val vm = hamoVm(key = "fault_$id") { FaultDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(0) }
    val checks = remember { mutableStateListOf<Boolean>() }

    Scaffold(topBar = { HamoTopBar("خطوات الفحص والإصلاح", onBack = { nav.popBackStack() }) }) { pad ->
        StateContent(state, isEmpty = { it == null || it.full.solutions.isEmpty() }, emptyTitle = "لا توجد خطوات", emptyHint = "أضف حلًا بخطوات لهذا العطل من شاشة التعديل.") { d ->
            if (d == null) return@StateContent
            val sols = d.full.solutions
            val idx = selected.coerceIn(0, sols.lastIndex)
            val sol = sols[idx]
            val steps = sol.orderedSteps
            while (checks.size < steps.size) checks.add(false)
            while (checks.size > steps.size) checks.removeAt(checks.lastIndex)
            val done = checks.count { it }
            ScrollColumn(pad) {
                Text(d.full.fault.title, style = MaterialTheme.typography.titleLarge)
                if (sols.size > 1) {
                    DropdownField("الحل", sols.mapIndexed { i, s -> i to s.solution.title }, idx, { selected = it; checks.clear() })
                }
                WarningBox(listOf(d.full.fault.warnings, sol.solution.warnings).filter { it.isNotBlank() }.joinToString("\n"))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { VerificationBadge(sol.solution.level) }
                Text("أنجزت $done من ${steps.size} خطوة", color = if (done == steps.size) OkGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                steps.forEachIndexed { i, st ->
                    NumberedStep(i + 1, st.text, done = checks[i], onToggle = { checks[i] = !checks[i] })
                }
                if (done == steps.size && steps.isNotEmpty()) {
                    Text(
                        "إن لم يُحل العطل بعد هذه الخطوات فلا تُجرِ تعديلات خطرة بنفسك؛ راجع دليل الشركة أو استعن بفني مؤهل.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                SecondaryButton("إعادة التعيين", { checks.clear() }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun FaultEditScreen(nav: NavController, id: Long, presetModel: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "fault_edit_${id}_$presetModel") { FaultEditViewModel(container.repo, id, presetModel.takeIf { it > 0 }) }
    val names by vm.names.collectAsStateWithLifecycle()
    val form = vm.form
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { HamoTopBar(if (id == 0L) "عطل جديد" else "تعديل العطل", onBack = { nav.popBackStack() }) }) { pad ->
        when {
            vm.missing -> EmptyState("العطل غير موجود", "ربما حُذف.")
            form == null -> LoadingBox()
            else -> ScrollColumn(pad) {
                val n = (names as? UiState.Success)?.value
                FormField("عنوان العطل", form.title, { form.title = it; error = null }, error = error)
                if (n != null) {
                    DropdownField(
                        "نوع الباب",
                        listOf<Pair<Long?, String>>(null to "عام / غير محدد") + n.categories.map { it.key as Long? to it.value },
                        form.categoryId, { form.categoryId = it },
                    )
                    DropdownField(
                        "الموديل (اتركه عامًا إن كان العطل لا يخص موديلًا)",
                        listOf<Pair<Long?, String>>(null to "عام — غير خاص بموديل") +
                            n.models.values.map { it.id as Long? to "${n.brands[it.brandId].orEmpty()} ${it.name}".trim() },
                        form.modelId, { form.modelId = it },
                    )
                }
                FormField("وصف العطل", form.description, { form.description = it }, singleLine = false, minLines = 3)
                StringListEditor("الأعراض", form.symptoms, "إضافة عرض", "عرض")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أكواد الأعطال (إن وُجدت)", style = MaterialTheme.typography.titleMedium)
                    form.codes.forEachIndexed { i, c ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                FormField("الكود ${i + 1}", c.code, { c.code = it })
                                FormField("معنى الكود", c.meaning, { c.meaning = it })
                            }
                            IconButton(onClick = { form.codes.removeAt(i) }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                    }
                    SecondaryButton("إضافة كود", { form.codes.add(com.hamo.doortech.ui.CodeForm()) }, Modifier.fillMaxWidth())
                }
                FormField("كلمات بحث بديلة (مفصولة بفواصل)", form.altKeywords, { form.altKeywords = it }, singleLine = false, minLines = 2)
                FormField("تحذيرات", form.warnings, { form.warnings = it }, singleLine = false, minLines = 2)
                FormField("الأدوات المطلوبة", form.tools, { form.tools = it })
                FormField("مصدر المعلومة والموديل الذي تنطبق عليه", form.source, { form.source = it })
                LevelDropdown(form.level, { form.level = it })

                Text("الحلول", style = MaterialTheme.typography.titleMedium)
                form.solutions.forEachIndexed { i, s -> SolutionEditor(i, s) { form.solutions.removeAt(i) } }
                AddRow("إضافة حل") { form.solutions.add(SolutionForm()) }

                PrimaryButton(if (vm.saving) "جارٍ الحفظ..." else "حفظ العطل", {
                    vm.save { r ->
                        if (r is Outcome.Fail) { error = r.message; messenger.show(r.message) }
                        else { messenger.show("تم حفظ العطل"); nav.popBackStack() }
                    }
                }, Modifier.fillMaxWidth(), enabled = !vm.saving)
            }
        }
    }
}

@Composable
private fun SolutionEditor(index: Int, s: SolutionForm, onRemove: () -> Unit) {
    SectionCard("الحل ${index + 1}") {
        FormField("عنوان الحل", s.title, { s.title = it })
        FormField("ينطبق على (موديل/حالة)", s.appliesTo, { s.appliesTo = it })
        FormField("مصدر الحل", s.source, { s.source = it })
        LevelDropdown(s.level, { s.level = it })
        FormField("تحذيرات الحل", s.warnings, { s.warnings = it }, singleLine = false, minLines = 2)
        FormField("أدوات الحل", s.tools, { s.tools = it })
        StringListEditor("خطوات الحل بالترتيب", s.steps, "إضافة خطوة", "خطوة", multiline = true)
        SecondaryButton("حذف هذا الحل", onRemove, Modifier.fillMaxWidth())
    }
}
