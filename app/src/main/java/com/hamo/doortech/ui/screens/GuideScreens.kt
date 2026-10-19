package com.hamo.doortech.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.ui.components.BeforeYouStart
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.GuideIcon
import com.hamo.doortech.ui.components.StepsChecklist
import com.hamo.doortech.ui.components.TermsCard
import com.hamo.doortech.ui.components.guideStyle
import com.hamo.doortech.ui.theme.MutedText
import com.hamo.doortech.ui.theme.Navy
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
    val style = guideStyle(type)
    var query by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { HamoTopBar(style.title, onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.guideEdit(0, type)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("إضافة") },
                containerColor = style.color, contentColor = Color.White,
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            StateContent(state, "لا توجد أدلة", "أضف أول دليل في هذا القسم.", isEmpty = { it.isEmpty() }) { list ->
                val brandOf = { g: TechnicalGuide -> n?.brandOfModel(g.modelId) ?: "عام" }
                val brands = list.map(brandOf).distinct().sorted()
                val q = query.trim().lowercase()
                val shown = list.filter { g ->
                    (brand == null || brandOf(g) == brand) &&
                        (q.isEmpty() || g.title.lowercase().contains(q) || (n?.modelName(g.modelId)?.lowercase()?.contains(q) == true))
                }
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                                .background(Brush.linearGradient(listOf(style.color, style.color.copy(alpha = 0.75f))))
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(style.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                Text(style.hint, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                Text("${list.size} دليل • ${brands.size} شركة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it }, singleLine = true,
                            placeholder = { Text("ابحث باسم الموديل أو الدليل...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (brands.size > 1) item {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = brand == null, onClick = { brand = null }, label = { Text("الكل") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = style.color, selectedLabelColor = Color.White))
                            brands.forEach { b ->
                                FilterChip(selected = brand == b, onClick = { brand = if (brand == b) null else b }, label = { Text(b.ltr()) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = style.color, selectedLabelColor = Color.White))
                            }
                        }
                    }
                    if (shown.isEmpty()) item {
                        Text("مفيش نتائج. جرّب كلمة تانية أو اختار «الكل».", color = MutedText, modifier = Modifier.padding(8.dp))
                    }
                    items(shown, key = { it.id }) { g ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                                .clickable { nav.navigate(Routes.guide(g.id)) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            GuideIcon(style)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(g.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 22.sp)
                                Text(
                                    listOfNotNull(brandOf(g).ltr(), n?.modelName(g.modelId)?.ltr(), n?.category(g.categoryId)).distinct().joinToString(" • "),
                                    color = MutedText, fontSize = 12.sp,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { if (g.isSeed) SeedBadge(); VerificationBadge(g.level) }
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF94A3B8))
                        }
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
        HamoTopBar("الدليل", onBack = { nav.popBackStack() }) {
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
            val style = guideStyle(g.type)
            val steps = d.full.orderedSteps.map { it.text }
            Column(
                Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // رأس الدليل: القسم والعنوان والموديل في بطاقة ملونة واحدة.
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(style.color, style.color.copy(alpha = 0.75f)))).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(style.title, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(g.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 28.sp)
                    val where = listOfNotNull(d.brandName?.ltr(), d.modelName?.ltr(), d.categoryName).joinToString(" • ")
                    if (where.isNotBlank()) Text(where, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (g.isSeed) SeedBadge()
                        VerificationBadge(g.level)
                        if (steps.isNotEmpty()) Chip("${steps.size} خطوات", Color.White.copy(alpha = 0.2f), Color.White)
                    }
                }
                BeforeYouStart(g.summary, style)
                WarningBox(g.warnings)
                TermsCard(steps.joinToString("\n") + "\n" + g.summary)
                if (steps.isNotEmpty()) {
                    Text("الخطوات", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    StepsChecklist("guide_$id", steps, style.color)
                } else {
                    EmptyState("لسه مفيش خطوات", "أضف الخطوات من زر التعديل، وراجع كتالوج الشركة قبل أي ضبط.")
                }
                Text("الصور والمخططات", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                MediaSection(OwnerType.GUIDE, id)
                if (g.source.isNotBlank()) {
                    Text("المصدر: ${g.source}", color = MutedText, fontSize = 12.sp, lineHeight = 18.sp)
                }
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
