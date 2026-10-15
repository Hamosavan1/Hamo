package com.hamo.doortech.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.SparePart
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.domain.Validators
import com.hamo.doortech.ui.AllMediaViewModel
import com.hamo.doortech.ui.BackupViewModel
import com.hamo.doortech.ui.FavoritesViewModel
import com.hamo.doortech.ui.PartsViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.SettingsViewModel
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.FormField
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.ListCard
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SecondaryButton
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.Thumbnail
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.util.Formatting
import com.hamo.doortech.util.ltr

private data class MoreEntry(val label: String, val hint: String, val icon: ImageVector, val route: String)

private val moreEntries = listOf(
    MoreEntry("أنواع الأبواب والبوابات", "تصنيفات قابلة للتعديل", Icons.Default.Category, Routes.CATEGORIES),
    MoreEntry("الشركات المصنعة", "إضافة وتعديل الشركات", Icons.Default.Business, Routes.BRANDS),
    MoreEntry("قائمة الموديلات", "الماكينات ومواصفاتها", Icons.Default.Memory, Routes.MODELS),
    MoreEntry("أدلة التركيب", "خطوات وقوائم فحص", Icons.Default.Construction, Routes.guides(GuideType.INSTALL)),
    MoreEntry("أدلة البرمجة", "برمجة وضبط", Icons.Default.Settings, Routes.guides(GuideType.PROGRAMMING)),
    MoreEntry("التوصيلات الكهربائية", "قواعد وملاحظات التوصيل", Icons.Default.ElectricalServices, Routes.guides(GuideType.WIRING)),
    MoreEntry("إعدادات الريموت والحساسات", "الريموتات والخلايا والحساسات", Icons.Default.SettingsRemote, Routes.guides(GuideType.REMOTE)),
    MoreEntry("قطع الغيار", "مخزون وأرقام القطع", Icons.Default.Handyman, Routes.PARTS),
    MoreEntry("بحث بالإنترنت (ذكاء اصطناعي)", "للمكينات غير الموجودة في قاعدتك", Icons.Default.Memory, Routes.ai()),
    MoreEntry("المفضلة", "العناصر التي حفظتها", Icons.Default.Bookmark, Routes.FAVORITES),
    MoreEntry("الصور والمرفقات", "كل المرفقات المحفوظة", Icons.Default.PhotoLibrary, Routes.MEDIA),
    MoreEntry("النسخ الاحتياطي والاستعادة", "تصدير واستيراد البيانات", Icons.Default.Backup, Routes.BACKUP),
    MoreEntry("الإعدادات والمساعدة", "عن التطبيق والسلامة", Icons.Default.Settings, Routes.SETTINGS),
)

@Composable
fun MoreScreen(nav: NavController) {
    Scaffold(topBar = { HamoTopBar("المزيد") }) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(moreEntries, key = { it.route }) { e ->
                ListCard(
                    title = e.label, subtitle = e.hint,
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) },
                    onClick = { nav.navigate(e.route) },
                )
            }
        }
    }
}

@Composable
fun PartsScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm { PartsViewModel(container.repo) }
    val state by vm.parts.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    val n = (names as? UiState.Success)?.value
    var editing by remember { mutableStateOf<SparePart?>(null) }
    var deleting by remember { mutableStateOf<SparePart?>(null) }

    Scaffold(
        topBar = { HamoTopBar("قطع الغيار", onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { editing = SparePart(name = "") }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("قطعة جديدة") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد قطع غيار", "أضف القطع التي تستخدمها لتربطها بالموديلات والزيارات.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { p ->
                        ListCard(
                            title = p.name,
                            subtitle = listOfNotNull(
                                p.partNumber.takeIf { it.isNotBlank() }?.ltr(),
                                n?.brand(p.brandId)?.ltr(), n?.modelName(p.modelId)?.ltr(),
                                "المخزون: ${p.quantity}",
                            ).joinToString(" • "),
                            trailing = {
                                IconButton(onClick = { editing = p }) { Icon(Icons.Default.Edit, "تعديل") }
                                IconButton(onClick = { deleting = p }) { Icon(Icons.Default.Delete, "حذف") }
                            },
                        )
                    }
                }
            }
        }
    }

    editing?.let { p ->
        var name by remember(p.id) { mutableStateOf(p.name) }
        var number by remember(p.id) { mutableStateOf(p.partNumber) }
        var qty by remember(p.id) { mutableStateOf(p.quantity.toString()) }
        var notes by remember(p.id) { mutableStateOf(p.notes) }
        var brandId by remember(p.id) { mutableStateOf(p.brandId) }
        var modelId by remember(p.id) { mutableStateOf(p.modelId) }
        var error by remember(p.id) { mutableStateOf<String?>(null) }
        var qtyError by remember(p.id) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(if (p.id == 0L) "قطعة جديدة" else "تعديل القطعة") },
            text = {
                Column(Modifier.then(Modifier), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField("اسم القطعة", name, { name = it; error = null }, error = error)
                    FormField("رقم القطعة", number, { number = it })
                    FormField("الكمية بالمخزون", qty, { qty = it; qtyError = null }, error = qtyError, keyboardType = KeyboardType.Number)
                    if (n != null) {
                        DropdownField("الشركة", listOf<Pair<Long?, String>>(null to "غير محددة") + n.brands.map { it.key as Long? to it.value }, brandId, { brandId = it })
                        DropdownField(
                            "الموديل",
                            listOf<Pair<Long?, String>>(null to "غير محدد") +
                                n.models.values.filter { brandId == null || it.brandId == brandId }.map { it.id as Long? to it.name },
                            modelId, { modelId = it },
                        )
                    }
                    FormField("ملاحظات", notes, { notes = it }, singleLine = false, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qErr = Validators.quantity(qty)
                    if (qErr != null) { qtyError = qErr; return@TextButton }
                    vm.save(p.copy(name = name, partNumber = number, quantity = qty.trim().toIntOrNull() ?: 0, brandId = brandId, modelId = modelId, notes = notes)) { r ->
                        if (r is Outcome.Fail) error = r.message else { editing = null; messenger.show("تم حفظ القطعة") }
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("إلغاء") } },
        )
    }
    deleting?.let { p ->
        ConfirmDialog("حذف القطعة", "هل تريد حذف «${p.name}»؟ ستبقى أسماء القطع في الزيارات السابقة كما هي.", "حذف", true,
            onConfirm = { vm.delete(p) { messenger.show(it.messageOr("تم حذف القطعة")) }; deleting = null },
            onDismiss = { deleting = null })
    }
}

@Composable
fun FavoritesScreen(nav: NavController) {
    val container = LocalContainer.current
    val vm = hamoVm { FavoritesViewModel(container.repo) }
    val state by vm.items.collectAsStateWithLifecycle()
    Scaffold(topBar = { HamoTopBar("المفضلة", onBack = { nav.popBackStack() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد عناصر مفضلة", "اضغط على النجمة داخل أي عطل أو دليل أو موديل لإضافته هنا.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { "${it.type}_${it.id}" }) { f ->
                        ListCard(
                            title = f.title, subtitle = f.subtitle,
                            trailing = { IconButton(onClick = { vm.remove(f) }) { Icon(Icons.Default.Star, "إزالة من المفضلة", tint = com.hamo.doortech.ui.theme.Orange) } },
                            onClick = {
                                nav.navigate(
                                    when (f.type) {
                                        OwnerType.FAULT -> Routes.fault(f.id)
                                        OwnerType.MODEL -> Routes.model(f.id)
                                        else -> Routes.guide(f.id)
                                    }
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaLibraryScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm { AllMediaViewModel(container.repo) }
    val state by vm.media.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf<MediaAttachment?>(null) }

    fun ownerLabel(m: MediaAttachment) = when (m.ownerType) {
        OwnerType.FAULT -> "عطل"
        OwnerType.MODEL -> "موديل"
        OwnerType.GUIDE -> "دليل"
        OwnerType.JOB -> "زيارة صيانة"
        else -> ""
    }

    Scaffold(topBar = { HamoTopBar("الصور والمرفقات", onBack = { nav.popBackStack() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            StateContent(state, "لا توجد مرفقات", "أضف الصور والمستندات من شاشات الأعطال والموديلات والأدلة وزيارات الصيانة.", isEmpty = { it.isEmpty() }) { list ->
                LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { m ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                nav.navigate(
                                    when (m.ownerType) {
                                        OwnerType.FAULT -> Routes.fault(m.ownerId)
                                        OwnerType.MODEL -> Routes.model(m.ownerId)
                                        OwnerType.GUIDE -> Routes.guide(m.ownerId)
                                        else -> Routes.job(m.ownerId)
                                    }
                                )
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (m.kind == MediaKind.IMAGE) {
                                Thumbnail(container.mediaStore.fileFor(m.path), Modifier.size(64.dp).padding(0.dp))
                            } else {
                                Icon(if (m.kind == MediaKind.LINK) Icons.Default.Link else Icons.Default.AttachFile, null, Modifier.size(64.dp).padding(16.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(m.title.ifBlank { if (m.kind == MediaKind.IMAGE) "صورة" else m.path }, maxLines = 1)
                                Text("مرتبط بـ: ${ownerLabel(m)} • ${Formatting.date(m.createdAt)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleting = m }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                    }
                }
            }
        }
    }
    deleting?.let { m ->
        ConfirmDialog("حذف المرفق", "سيُحذف المرفق نهائيًا من التطبيق.", "حذف", true,
            onConfirm = { vm.delete(m) { messenger.show(it.messageOr("تم حذف المرفق")) }; deleting = null },
            onDismiss = { deleting = null })
    }
}

@Composable
fun BackupScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm = hamoVm { BackupViewModel(container) }
    val preview = vm.pendingPreview

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) vm.export(context, uri) { messenger.show(it.messageOr("تم التصدير")) }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.inspect(context, uri) { r -> if (r is Outcome.Fail) messenger.show(r.message) }
    }

    Scaffold(topBar = { HamoTopBar("النسخ الاحتياطي والاستعادة", onBack = { nav.popBackStack() }) }) { pad ->
        ScrollColumn(pad) {
            SectionCard("تصدير نسخة احتياطية") {
                Text("يحفظ كل بياناتك والصور والمرفقات في ملف ZIP واحد يمكنك حفظه في أي مكان (ذاكرة الجهاز، الملفات، السحابة...).", style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(
                    if (vm.busy) "جارٍ العمل..." else "تصدير الآن",
                    { exporter.launch("hamo_door_tech_backup_${Formatting.fileStamp(System.currentTimeMillis())}.zip") },
                    Modifier.fillMaxWidth(), enabled = !vm.busy,
                )
            }
            SectionCard("استعادة من نسخة احتياطية") {
                Text("يُفحص الملف أولًا (بصمات SHA-256) ثم تُعرض محتوياته قبل أي تغيير. الاستعادة تدمج المحتوى الجديد فقط ولا تستبدل ولا تحذف أي شيء موجود.", style = MaterialTheme.typography.bodyMedium)
                SecondaryButton("اختيار ملف نسخة احتياطية", { importer.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) }, Modifier.fillMaxWidth(), enabled = !vm.busy)
            }
            WarningBox("احتفظ بنسخة احتياطية قبل حذف التطبيق أو تغيير الجهاز؛ البيانات محلية فقط ولا تُرفع لأي خادم.")
        }
    }

    if (preview != null) {
        AlertDialog(
            onDismissRequest = { vm.cancelImport() },
            title = { Text("ملف النسخة سليم") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("تاريخ النسخة: ${Formatting.dateTime(preview.manifest.createdAt)}")
                    preview.manifest.counts.forEach { (k, v) -> Text("• $k: $v") }
                    Text("سيتم دمج الجديد فقط: التصنيفات والشركات والموديلات المتطابقة تُدمج، والأعطال والأدلة والزيارات المكررة تُتخطى. لن يُحذف أو يُستبدل شيء.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(enabled = !vm.busy, onClick = {
                    vm.confirmImport { r -> messenger.show(r.messageOr("تمت الاستعادة")) }
                }) { Text(if (vm.busy) "جارٍ الاستعادة..." else "دمج الآن") }
            },
            dismissButton = { TextButton(enabled = !vm.busy, onClick = { vm.cancelImport() }) { Text("إلغاء") } },
        )
    }
}

@Composable
fun SettingsScreen(nav: NavController) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm { SettingsViewModel(container) }
    val stats by vm.stats.collectAsStateWithLifecycle()
    var confirmDeleteSeed by remember { mutableStateOf(false) }
    var confirmReload by remember { mutableStateOf(false) }

    Scaffold(topBar = { HamoTopBar("الإعدادات والمساعدة", onBack = { nav.popBackStack() }) }) { pad ->
        ScrollColumn(pad) {
            SectionCard("عن التطبيق") {
                Text("HAMO DOOR TECH — الإصدار 1.0.0")
                Text("دليل فني لصيانة الأبواب والبوابات الأوتوماتيك. يعمل بالكامل دون إنترنت، وبياناتك محفوظة على جهازك فقط.", style = MaterialTheme.typography.bodyMedium)
            }
            SectionCard("محتوى قاعدة البيانات") {
                when (val s = stats) {
                    is UiState.Success -> s.value.forEach { (k, v) -> Text("• $k: $v") }
                    is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                    UiState.Loading -> Text("جارٍ التحميل...")
                }
            }
            SectionCard("البيانات التجريبية") {
                Text("النسخة الأولى تحوي بيانات تعليمية مميزة بوسم «تجريبي». هي إرشادات عامة غير خاصة بأي شركة أو موديل، والشركات والموديلات فيها أمثلة وهمية. يمكنك حذفها دون المساس ببياناتك.", style = MaterialTheme.typography.bodyMedium)
                SecondaryButton("حذف البيانات التجريبية", { confirmDeleteSeed = true }, Modifier.fillMaxWidth(), enabled = !vm.busy)
                SecondaryButton("إعادة تحميل البيانات التجريبية", { confirmReload = true }, Modifier.fillMaxWidth(), enabled = !vm.busy)
            }
            SectionCard("طريقة الاستخدام") {
                Text("• «تشخيص»: اكتب العرض أو كود الخطأ واختر الموديل لتضييق النتائج.\n• «البحث»: يبحث في الأعطال والأعراض والأكواد والموديلات والشركات والأدلة، بالعربية والإنجليزية.\n• أضف بياناتك الخاصة (تصنيفات، شركات، موديلات، أعطال، أدلة) من شاشاتها، وأرفق الصور والمستندات.\n• سجّل كل زيارة في «الصيانة» وأنشئ تقرير PDF للعميل.\n• اعمل نسخة احتياطية دورية من «النسخ الاحتياطي».", style = MaterialTheme.typography.bodyMedium)
            }
            WarningBox("السلامة أولًا: افصل الكهرباء قبل أي عمل على التوصيلات، وابتعد عن الأجزاء المتحركة والزنبركات المحمّلة، ولا تعطّل حساسات الأمان أو تتجاوزها. المعلومات العامة في التطبيق لا تغني عن دليل الشركة المصنعة، ولا تعتبر أي سبب محتمل تشخيصًا مؤكدًا. استعن بفني مؤهل عند الحاجة.")
        }
    }

    if (confirmDeleteSeed) {
        ConfirmDialog("حذف البيانات التجريبية", "ستُحذف كل العناصر الموسومة «تجريبي» فقط. بياناتك الخاصة لن تُمس.", "حذف", true,
            onConfirm = { confirmDeleteSeed = false; vm.deleteSeed { messenger.show(it.messageOr("تم حذف البيانات التجريبية")) } },
            onDismiss = { confirmDeleteSeed = false })
    }
    if (confirmReload) {
        ConfirmDialog("إعادة تحميل البيانات التجريبية", "ستُضاف البيانات التجريبية مرة أخرى. إن كانت موجودة سيظهر بعضها مكررًا.", "إضافة", false,
            onConfirm = { confirmReload = false; vm.reloadSeed { messenger.show(it.messageOr("تمت إضافة البيانات التجريبية")) } },
            onDismiss = { confirmReload = false })
    }
}
