package com.hamo.doortech.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.R
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.CatalogViewModel
import com.hamo.doortech.ui.ModelDetailViewModel
import com.hamo.doortech.ui.ModelEditViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.ConfirmDialog
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
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.util.ltr

@Composable
fun CategoriesScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val state by vm.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<DoorCategory?>(null) }
    var deleting by remember { mutableStateOf<DoorCategory?>(null) }

    Scaffold(
        topBar = { HamoTopBar("أنواع الأبواب والبوابات", onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { editing = DoorCategory(name = "") }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("تصنيف جديد") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد تصنيفات", "أضف أول تصنيف للأبواب والبوابات.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { c ->
                        ListCard(
                            title = c.name, subtitle = c.description,
                            badges = { if (c.isSeed) SeedBadge() },
                            trailing = {
                                IconButton(onClick = { editing = c }) { Icon(Icons.Default.Edit, "تعديل") }
                                IconButton(onClick = { deleting = c }) { Icon(Icons.Default.Delete, "حذف") }
                            },
                        )
                    }
                }
            }
        }
    }

    editing?.let { c ->
        var name by remember(c.id) { mutableStateOf(c.name) }
        var desc by remember(c.id) { mutableStateOf(c.description) }
        var error by remember(c.id) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (c.id == 0L) "تصنيف جديد" else "تعديل التصنيف") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField("اسم التصنيف", name, { name = it; error = null }, error = error)
                    FormField("الوصف (اختياري)", desc, { desc = it }, singleLine = false, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.saveCategory(c.copy(name = name, description = desc)) { r ->
                        if (r is Outcome.Fail) error = r.message else { editing = null; messenger.show("تم حفظ التصنيف") }
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("إلغاء") } },
        )
    }
    deleting?.let { c ->
        ConfirmDialog("حذف التصنيف", "هل تريد حذف «${c.name}»؟ لا يمكن حذف تصنيف مستخدم في بيانات أخرى.", "حذف", true,
            onConfirm = { vm.deleteCategory(c) { messenger.show(it.messageOr("تم حذف التصنيف")) }; deleting = null },
            onDismiss = { deleting = null })
    }
}

@Composable
fun BrandsScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val state by vm.brands.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Brand?>(null) }
    var deleting by remember { mutableStateOf<Brand?>(null) }

    Scaffold(
        topBar = { HamoTopBar("الشركات المصنعة", onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { editing = Brand(name = "") }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("شركة جديدة") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد شركات", "أضف الشركات المصنعة التي تتعامل معها.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { b ->
                        ListCard(
                            title = b.name.ltr(), subtitle = listOf(b.country, b.notes).filter { it.isNotBlank() }.joinToString(" — "),
                            badges = { if (b.isSeed) SeedBadge() },
                            trailing = {
                                IconButton(onClick = { editing = b }) { Icon(Icons.Default.Edit, "تعديل") }
                                IconButton(onClick = { deleting = b }) { Icon(Icons.Default.Delete, "حذف") }
                            },
                            onClick = { nav.navigate(Routes.MODELS) },
                        )
                    }
                }
            }
        }
    }

    editing?.let { b ->
        var name by remember(b.id) { mutableStateOf(b.name) }
        var country by remember(b.id) { mutableStateOf(b.country) }
        var notes by remember(b.id) { mutableStateOf(b.notes) }
        var error by remember(b.id) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (b.id == 0L) "شركة جديدة" else "تعديل الشركة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField("اسم الشركة", name, { name = it; error = null }, error = error)
                    FormField("البلد (اختياري)", country, { country = it })
                    FormField("ملاحظات (اختياري)", notes, { notes = it }, singleLine = false, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.saveBrand(b.copy(name = name, country = country, notes = notes)) { r ->
                        if (r is Outcome.Fail) error = r.message else { editing = null; messenger.show("تم حفظ الشركة") }
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("إلغاء") } },
        )
    }
    deleting?.let { b ->
        ConfirmDialog("حذف الشركة", "هل تريد حذف «${b.name}»؟ لا يمكن حذف شركة لها موديلات أو قطع أو زيارات.", "حذف", true,
            onConfirm = { vm.deleteBrand(b) { messenger.show(it.messageOr("تم حذف الشركة")) }; deleting = null },
            onDismiss = { deleting = null })
    }
}

/**
 * أيقونة المكينة في القوائم: صورة حقيقية للمكينة نفسها (مقصوصة من صورة غلاف الكتالوج
 * الرسمي للماركة/الموديل) لو متاحة، وإلا أيقونة عامة. أضِف سطرًا هنا كل ما تمت إضافة
 * صورة غلاف جديدة (drawable) لموديل جديد في ReferenceData.
 */
private fun machineIconFor(brandName: String?, modelName: String): Int? {
    val b = brandName.orEmpty()
    val m = modelName
    return when {
        b.contains("Beninca", true) && m.contains("BULL", true) -> R.drawable.ic_machine_beninca_bull
        b.contains("Roger", true) && (m.contains("BM30", true) || m.contains("BH30", true)) -> R.drawable.ic_machine_roger_bm30
        else -> null
    }
}

@Composable
fun MachineIcon(brandName: String?, modelName: String, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val res = machineIconFor(brandName, modelName)
    Box(
        Modifier.size(size).padding(end = 12.dp).clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (res != null) {
            Image(painterResource(res), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Build, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun ModelsScreen(nav: NavController) {
    val container = LocalContainer.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val models by vm.models.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    var brandFilter by remember { mutableStateOf<Long?>(null) }
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = { HamoTopBar("قائمة الموديلات", onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.modelEdit(0, brandFilter ?: 0)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("موديل جديد") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            val n = (names as? UiState.Success)?.value
            Column(Modifier.padding(14.dp, 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FormField("بحث في الموديلات", text, { text = it }, placeholder = "اسم الموديل أو وحدة التحكم")
                if (n != null) {
                    DropdownField(
                        "الشركة",
                        listOf<Pair<Long?, String>>(null to "كل الشركات") + n.brands.map { it.key as Long? to it.value },
                        brandFilter, { brandFilter = it },
                    )
                }
            }
            StateContent(models, "لا توجد موديلات", "أضف موديلات الماكينات لربط الأعطال والأدلة بها.", isEmpty = { it.isEmpty() }) { all ->
                val q = text.trim().lowercase()
                val list = all.filter { m ->
                    (brandFilter == null || m.brandId == brandFilter) &&
                        (q.isEmpty() || m.name.lowercase().contains(q) || m.controlUnit.lowercase().contains(q))
                }
                if (list.isEmpty()) EmptyState("لا توجد نتائج", "غيّر الشركة أو كلمة البحث.")
                else LazyColumn(contentPadding = PaddingValues(14.dp, 4.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { m ->
                        ListCard(
                            title = m.name.ltr(),
                            subtitle = listOfNotNull(n?.brands?.get(m.brandId), n?.category(m.categoryId)).joinToString(" • "),
                            badges = { if (m.isSeed) SeedBadge() },
                            leading = { MachineIcon(brandName = n?.brands?.get(m.brandId), modelName = m.name) },
                            onClick = { nav.navigate(Routes.model(m.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelEditScreen(nav: NavController, id: Long, presetBrand: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "model_edit_$id") { ModelEditViewModel(container.repo, id, presetBrand.takeIf { it > 0 }) }
    val cats by vm.categories.collectAsStateWithLifecycle()
    val brands by vm.brands.collectAsStateWithLifecycle()
    val form = vm.form

    Scaffold(topBar = { HamoTopBar(if (id == 0L) "موديل جديد" else "تعديل الموديل", onBack = { nav.popBackStack() }) }) { pad ->
        when {
            vm.missing -> EmptyState("الموديل غير موجود", "ربما حُذف.")
            form == null -> LoadingBox()
            else -> ScrollColumn(pad) {
                val brandList = (brands as? UiState.Success)?.value ?: emptyList()
                val catList = (cats as? UiState.Success)?.value ?: emptyList()
                if (brandList.isEmpty()) {
                    SectionCard("لا توجد شركات") {
                        Text("أضف شركة مصنعة أولًا ثم ارجع لإضافة الموديل.")
                        SecondaryButton("إضافة شركة", { nav.navigate(Routes.BRANDS) })
                    }
                }
                var nameError by remember { mutableStateOf<String?>(null) }
                FormField("اسم الموديل", form.name, { form.name = it; nameError = null }, error = nameError)
                DropdownField(
                    "الشركة المصنعة",
                    listOf<Pair<Long?, String>>(null to "اختر الشركة") + brandList.map { it.id as Long? to it.name },
                    form.brandId, { form.brandId = it },
                )
                DropdownField(
                    "نوع الباب",
                    listOf<Pair<Long?, String>>(null to "غير محدد") + catList.map { it.id as Long? to it.name },
                    form.categoryId, { form.categoryId = it },
                )
                FormField("وحدة التحكم", form.controlUnit, { form.controlUnit = it })
                FormField("المواصفات", form.specs, { form.specs = it }, singleLine = false, minLines = 3)
                FormField("الريموت وإعداداته", form.remoteInfo, { form.remoteInfo = it }, singleLine = false, minLines = 2)
                FormField("الحساسات", form.sensorsInfo, { form.sensorsInfo = it }, singleLine = false, minLines = 2)
                FormField("مصدر المعلومة (دليل/رابط/خبرة)", form.source, { form.source = it })
                FormField("ملاحظات", form.notes, { form.notes = it }, singleLine = false, minLines = 2)
                PrimaryButton(if (vm.saving) "جارٍ الحفظ..." else "حفظ", {
                    vm.save { r ->
                        if (r is Outcome.Fail) { nameError = r.message; messenger.show(r.message) }
                        else { messenger.show("تم حفظ الموديل"); nav.popBackStack() }
                    }
                }, Modifier.fillMaxWidth(), enabled = !vm.saving)
            }
        }
    }
}

@Composable
fun ModelDetailScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "model_$id") { ModelDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل الماكينة", onBack = { nav.popBackStack() }) {
            IconButton(onClick = vm::toggleFavorite) { Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, "مفضلة") }
            IconButton(onClick = { nav.navigate(Routes.modelEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "الموديل غير موجود") { d ->
            if (d == null) return@StateContent
            ScrollColumn(pad) {
                SectionCard(d.model.name.ltr()) {
                    if (d.model.isSeed) SeedBadge()
                    InfoRow("الشركة", d.brandName.orEmpty().ltr())
                    InfoRow("نوع الباب", d.categoryName.orEmpty())
                    InfoRow("وحدة التحكم", d.model.controlUnit.ltr())
                    InfoRow("المواصفات", d.model.specs)
                    InfoRow("الريموت", d.model.remoteInfo)
                    InfoRow("الحساسات", d.model.sensorsInfo)
                    InfoRow("المصدر", d.model.source)
                    InfoRow("ملاحظات", d.model.notes)
                }
                SectionCard("الأعطال المرتبطة (${d.faults.size})") {
                    if (d.faults.isEmpty()) Text("لا توجد أعطال مسجلة لهذا الموديل.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.faults.forEach { f ->
                        TextButton(onClick = { nav.navigate(Routes.fault(f.id)) }) { Text(f.title) }
                    }
                    SecondaryButton("إضافة عطل لهذا الموديل", { nav.navigate(Routes.faultEdit(0, id)) }, Modifier.fillMaxWidth())
                }
                SectionCard("الأدلة المرتبطة (${d.guides.size})") {
                    if (d.guides.isEmpty()) Text("لا توجد أدلة مرتبطة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.guides.forEach { g -> TextButton(onClick = { nav.navigate(Routes.guide(g.id)) }) { Text(g.title) } }
                }
                SectionCard("قطع الغيار (${d.parts.size})") {
                    if (d.parts.isEmpty()) Text("لا توجد قطع مسجلة لهذا الموديل.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.parts.forEach { p -> Text("• ${p.name} ${p.partNumber.ltr()} — المخزون: ${p.quantity}") }
                }
                MediaSection(OwnerType.MODEL, id)
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog("حذف الموديل", "سيُحذف الموديل ومرفقاته. لا يمكن الحذف إن كانت هناك أعطال أو أدلة أو زيارات مرتبطة به.", "حذف", true,
            onConfirm = {
                confirmDelete = false
                vm.delete { r -> messenger.show(r.messageOr("تم حذف الموديل")); if (r is Outcome.Ok) nav.popBackStack() }
            },
            onDismiss = { confirmDelete = false })
    }
}
