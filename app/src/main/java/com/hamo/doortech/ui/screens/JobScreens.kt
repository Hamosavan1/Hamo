package com.hamo.doortech.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.domain.Validators
import com.hamo.doortech.ui.JobDetailViewModel
import com.hamo.doortech.ui.JobEditViewModel
import com.hamo.doortech.ui.JobsViewModel
import com.hamo.doortech.ui.PartForm
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.DateField
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.FormField
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.InfoRow
import com.hamo.doortech.ui.components.ListCard
import com.hamo.doortech.ui.components.LoadingBox
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.MediaSection
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SecondaryButton
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.SeedBadge
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.util.Formatting
import com.hamo.doortech.util.ltr

@Composable
fun JobsScreen(nav: NavController) {
    val container = LocalContainer.current
    val vm = hamoVm { JobsViewModel(container.repo) }
    val state by vm.jobs.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    val n = (names as? UiState.Success)?.value
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = { HamoTopBar("سجل الصيانة") },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.jobEdit(0)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("زيارة جديدة") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            FormField("بحث في الزيارات", text, { text = it }, Modifier.padding(14.dp, 10.dp), placeholder = "اسم العميل أو المشكلة")
            StateContent(state, "لا توجد زيارات صيانة", "سجّل أول زيارة صيانة لتظهر هنا.", isEmpty = { it.isEmpty() }) { all ->
                val q = text.trim().lowercase()
                val list = all.filter { q.isEmpty() || it.customerName.lowercase().contains(q) || it.problem.lowercase().contains(q) || it.location.lowercase().contains(q) }
                if (list.isEmpty()) EmptyState("لا توجد نتائج", "غيّر كلمة البحث.")
                else LazyColumn(contentPadding = PaddingValues(14.dp, 4.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { j ->
                        ListCard(
                            title = j.customerName,
                            subtitle = buildString {
                                append(Formatting.date(j.visitDate))
                                n?.modelName(j.modelId)?.let { append(" • ${it.ltr()}") }
                                if (j.problem.isNotBlank()) append(" — ${j.problem}")
                            },
                            badges = {
                                if (j.isSeed) SeedBadge()
                                j.nextVisitDate?.let { Chip("القادمة: ${Formatting.date(it)}") }
                            },
                            onClick = { nav.navigate(Routes.job(j.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobDetailScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val context = LocalContext.current
    val vm = hamoVm(key = "job_$id") { JobDetailViewModel(container, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    fun withReport(share: Boolean) {
        vm.createReport { file, err ->
            if (file == null) { messenger.show(err ?: "تعذّر إنشاء التقرير"); return@createReport }
            try {
                val uri = container.mediaStore.uriFor(file)
                val intent = if (share) {
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        "مشاركة تقرير الصيانة",
                    )
                } else {
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                messenger.show("تم إنشاء التقرير لكن لا يوجد تطبيق لفتحه أو مشاركته")
            }
        }
    }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل زيارة الصيانة", onBack = { nav.popBackStack() }) {
            IconButton(onClick = { nav.navigate(Routes.jobEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = {
                vm.duplicate { r ->
                    messenger.show(r.messageOr("تم تكرار الزيارة"))
                    if (r is Outcome.Ok) nav.navigate(Routes.job(r.id))
                }
            }) { Icon(Icons.Default.ContentCopy, "تكرار") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "الزيارة غير موجودة") { d ->
            if (d == null) return@StateContent
            val j = d.full.job
            ScrollColumn(pad) {
                SectionCard(j.customerName) {
                    if (j.isSeed) SeedBadge()
                    InfoRow("الموقع", j.location)
                    InfoRow("تاريخ الزيارة", Formatting.date(j.visitDate))
                    InfoRow("نوع الباب", d.categoryName.orEmpty())
                    InfoRow("الشركة", d.brandName.orEmpty().ltr())
                    InfoRow("الموديل", d.modelName.orEmpty().ltr())
                    InfoRow("موعد الصيانة القادمة", j.nextVisitDate?.let { Formatting.date(it) }.orEmpty())
                }
                SectionCard("المشكلة والفحص والحل") {
                    InfoRow("وصف المشكلة", j.problem)
                    InfoRow("نتائج الفحص", j.findings)
                    InfoRow("الحل المنفذ", j.solution)
                    InfoRow("ملاحظات الفني", j.notes)
                }
                SectionCard("قطع الغيار المستخدمة") {
                    if (d.full.parts.isEmpty()) Text("لا توجد قطع مسجلة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.full.parts.forEach { Text("• ${it.name} × ${it.quantity}") }
                }
                MediaSection(OwnerType.JOB, id)
                SectionCard("تقرير PDF") {
                    Text("تقرير عربي بكل تفاصيل الزيارة والصور قبل/بعد.", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(if (vm.exporting) "جارٍ الإنشاء..." else "مشاركة التقرير", { withReport(true) }, Modifier.weight(1f), enabled = !vm.exporting)
                        SecondaryButton("عرض", { withReport(false) }, Modifier.weight(1f), enabled = !vm.exporting)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog("حذف الزيارة", "ستُحذف الزيارة وصورها وقطعها نهائيًا.", "حذف", true,
            onConfirm = {
                confirmDelete = false
                vm.delete { r -> messenger.show(r.messageOr("تم حذف الزيارة")); if (r is Outcome.Ok) nav.popBackStack() }
            },
            onDismiss = { confirmDelete = false })
    }
}

@Composable
fun JobEditScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "job_edit_$id") { JobEditViewModel(container.repo, id) }
    val names by vm.names.collectAsStateWithLifecycle()
    val stock by vm.parts.collectAsStateWithLifecycle()
    val form = vm.form
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { HamoTopBar(if (id == 0L) "زيارة جديدة" else "تعديل الزيارة", onBack = { nav.popBackStack() }) }) { pad ->
        when {
            vm.missing -> EmptyState("الزيارة غير موجودة", "ربما حُذفت.")
            form == null -> LoadingBox()
            else -> ScrollColumn(pad) {
                val n = (names as? UiState.Success)?.value
                FormField("اسم العميل أو الموقع", form.customerName, { form.customerName = it; error = null }, error = error)
                FormField("العنوان / الموقع", form.location, { form.location = it })
                DateField("تاريخ الزيارة", form.visitDate, { it?.let { d -> form.visitDate = d } })
                if (n != null) {
                    DropdownField(
                        "نوع الباب",
                        listOf<Pair<Long?, String>>(null to "غير محدد") + n.categories.map { it.key as Long? to it.value },
                        form.categoryId, { form.categoryId = it },
                    )
                    DropdownField(
                        "الشركة",
                        listOf<Pair<Long?, String>>(null to "غير محددة") + n.brands.map { it.key as Long? to it.value },
                        form.brandId, { form.brandId = it },
                    )
                    val models = n.models.values.filter { form.brandId == null || it.brandId == form.brandId }
                    DropdownField(
                        "الموديل",
                        listOf<Pair<Long?, String>>(null to "غير محدد") + models.map { it.id as Long? to it.name },
                        form.modelId, { form.modelId = it },
                    )
                }
                FormField("وصف المشكلة", form.problem, { form.problem = it }, singleLine = false, minLines = 2)
                FormField("نتائج الفحص", form.findings, { form.findings = it }, singleLine = false, minLines = 2)
                FormField("الحل المنفذ", form.solution, { form.solution = it }, singleLine = false, minLines = 2)

                SectionCard("قطع الغيار المستخدمة") {
                    form.parts.forEachIndexed { i, p ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FormField("القطعة", p.name, { p.name = it }, Modifier.weight(2f))
                            FormField("العدد", p.quantity, { p.quantity = it }, Modifier.weight(1f), keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            IconButton(onClick = { form.parts.removeAt(i) }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                    }
                    val stockList = (stock as? UiState.Success)?.value ?: emptyList()
                    if (stockList.isNotEmpty()) {
                        DropdownField(
                            "إضافة من مخزون القطع",
                            listOf<Long?>(null) .map { it to "اختر قطعة" } + stockList.map { it.id as Long? to it.name },
                            null as Long?, { pid ->
                                stockList.firstOrNull { it.id == pid }?.let { form.parts.add(PartForm(it.name, "1", it.id)) }
                            },
                        )
                    }
                    SecondaryButton("إضافة قطعة يدويًا", { form.parts.add(PartForm()) }, Modifier.fillMaxWidth())
                }

                FormField("ملاحظات الفني", form.notes, { form.notes = it }, singleLine = false, minLines = 2)
                DateField(
                    "موعد الصيانة القادمة (اختياري)", form.nextVisitDate, { form.nextVisitDate = it },
                    allowClear = true, error = Validators.nextVisit(form.visitDate, form.nextVisitDate),
                )
                if (id != 0L) MediaSection(OwnerType.JOB, id)
                else Text("احفظ الزيارة أولًا ثم أضف الصور «قبل/بعد» من شاشة التفاصيل.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                PrimaryButton(if (vm.saving) "جارٍ الحفظ..." else "حفظ الزيارة", {
                    vm.save { r ->
                        if (r is Outcome.Fail) { error = if (r.message.contains("العميل")) r.message else null; messenger.show(r.message) }
                        else {
                            messenger.show("تم حفظ الزيارة")
                            if (id == 0L) nav.navigate(Routes.job((r as Outcome.Ok).id)) { popUpTo(Routes.JOBS) } else nav.popBackStack()
                        }
                    }
                }, Modifier.fillMaxWidth(), enabled = !vm.saving)
            }
        }
    }
}
