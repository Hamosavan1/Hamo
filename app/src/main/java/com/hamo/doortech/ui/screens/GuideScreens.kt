package com.hamo.doortech.ui.screens

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.GuideDetailViewModel
import com.hamo.doortech.ui.GuideEditViewModel
import com.hamo.doortech.ui.GuideListViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.FormField
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.InfoRow
import com.hamo.doortech.ui.components.LevelDropdown
import com.hamo.doortech.ui.components.ListCard
import com.hamo.doortech.ui.components.LoadingBox
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.MediaSection
import com.hamo.doortech.ui.components.NumberedStep
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.SeedBadge
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.StringListEditor
import com.hamo.doortech.ui.components.VerificationBadge
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.util.ltr

@Composable
fun GuidesScreen(nav: NavController, type: String) {
    val container = LocalContainer.current
    val vm = hamoVm(key = "guides_$type") { GuideListViewModel(container.repo, type) }
    val state by vm.guides.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    val n = (names as? UiState.Success)?.value

    Scaffold(
        topBar = { HamoTopBar(GuideType.label(type), onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.guideEdit(0, type)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("إضافة") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد أدلة", "أضف أول دليل في هذا القسم.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { g ->
                        ListCard(
                            title = g.title,
                            subtitle = listOfNotNull(n?.category(g.categoryId), n?.modelName(g.modelId)?.ltr()).joinToString(" • ").ifBlank { "عام" },
                            badges = { if (g.isSeed) SeedBadge(); VerificationBadge(g.level) },
                            onClick = { nav.navigate(Routes.guide(g.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuideDetailScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "guide_$id") { GuideDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل الدليل", onBack = { nav.popBackStack() }) {
            IconButton(onClick = vm::toggleFavorite) { Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, "مفضلة") }
            IconButton(onClick = { nav.navigate(Routes.guideEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = {
                vm.duplicate { r ->
                    messenger.show(r.messageOr("تم تكرار الدليل"))
                    if (r is Outcome.Ok) nav.navigate(Routes.guide(r.id))
                }
            }) { Icon(Icons.Default.ContentCopy, "تكرار") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "الدليل غير موجود") { d ->
            if (d == null) return@StateContent
            val g = d.full.guide
            ScrollColumn(pad) {
                SectionCard(g.title) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (g.isSeed) SeedBadge()
                        VerificationBadge(g.level)
                    }
                    InfoRow("القسم", GuideType.singular(g.type))
                    InfoRow("نوع الباب", d.categoryName.orEmpty())
                    InfoRow("الشركة", d.brandName.orEmpty().ltr())
                    InfoRow("الموديل", d.modelName.orEmpty().ltr())
                    InfoRow("الملخص", g.summary)
                    InfoRow("المصدر", g.source)
                }
                WarningBox(g.warnings)
                SectionCard("الخطوات") {
                    d.full.orderedSteps.forEachIndexed { i, s -> NumberedStep(i + 1, s.text) }
                }
                MediaSection(OwnerType.GUIDE, id)
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog("حذف الدليل", "سيُحذف الدليل ومرفقاته نهائيًا.", "حذف", true,
            onConfirm = {
                confirmDelete = false
                vm.delete { r -> messenger.show(r.messageOr("تم حذف الدليل")); if (r is Outcome.Ok) nav.popBackStack() }
            },
            onDismiss = { confirmDelete = false })
    }
}

@Composable
fun GuideEditScreen(nav: NavController, id: Long, type: String) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "guide_edit_${id}_$type") { GuideEditViewModel(container.repo, id, type) }
    val names by vm.names.collectAsStateWithLifecycle()
    val form = vm.form
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { HamoTopBar(if (id == 0L) "دليل جديد" else "تعديل الدليل", onBack = { nav.popBackStack() }) }) { pad ->
        when {
            vm.missing -> EmptyState("الدليل غير موجود", "ربما حُذف.")
            form == null -> LoadingBox()
            else -> ScrollColumn(pad) {
                val n = (names as? UiState.Success)?.value
                FormField("عنوان الدليل", form.title, { form.title = it; error = null }, error = error)
                if (n != null) {
                    DropdownField(
                        "نوع الباب",
                        listOf<Pair<Long?, String>>(null to "عام / غير محدد") + n.categories.map { it.key as Long? to it.value },
                        form.categoryId, { form.categoryId = it },
                    )
                    DropdownField(
                        "الموديل (اتركه عامًا إن لم يخص موديلًا)",
                        listOf<Pair<Long?, String>>(null to "عام — غير خاص بموديل") +
                            n.models.values.map { it.id as Long? to "${n.brands[it.brandId].orEmpty()} ${it.name}".trim() },
                        form.modelId, { form.modelId = it },
                    )
                }
                FormField("ملخص", form.summary, { form.summary = it }, singleLine = false, minLines = 2)
                FormField("تحذيرات", form.warnings, { form.warnings = it }, singleLine = false, minLines = 2)
                FormField("مصدر المعلومة", form.source, { form.source = it })
                LevelDropdown(form.level, { form.level = it })
                StringListEditor("الخطوات بالترتيب", form.steps, "إضافة خطوة", "خطوة", multiline = true)
                PrimaryButton(if (vm.saving) "جارٍ الحفظ..." else "حفظ الدليل", {
                    vm.save { r ->
                        if (r is Outcome.Fail) { error = r.message; messenger.show(r.message) }
                        else { messenger.show("تم حفظ الدليل"); nav.popBackStack() }
                    }
                }, Modifier.fillMaxWidth(), enabled = !vm.saving)
            }
        }
    }
}
