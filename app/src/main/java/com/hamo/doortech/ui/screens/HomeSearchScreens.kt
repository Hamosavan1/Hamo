package com.hamo.doortech.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.domain.SearchResult
import com.hamo.doortech.domain.SearchType
import com.hamo.doortech.ui.CatalogViewModel
import com.hamo.doortech.ui.HomeViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.SearchViewModel
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.ListCard
import com.hamo.doortech.ui.components.LoadingBox
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SecondaryButton
import com.hamo.doortech.ui.components.SeedBadge
import com.hamo.doortech.ui.components.VerificationBadge
import com.hamo.doortech.ui.components.goTop
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.NavyLight
import com.hamo.doortech.ui.theme.Orange
import com.hamo.doortech.util.Formatting
import com.hamo.doortech.util.ltr

private data class Tile(val label: String, val icon: ImageVector, val action: (NavController) -> Unit)

private val tiles = listOf(
    Tile("تشخيص الأعطال", Icons.Default.MedicalServices) { it.goTop(Routes.DIAGNOSE) },
    Tile("أنواع الأبواب والبوابات", Icons.Default.Category) { it.navigate(Routes.CATEGORIES) },
    Tile("الشركات المصنعة", Icons.Default.Business) { it.navigate(Routes.BRANDS) },
    Tile("قائمة الموديلات", Icons.Default.Memory) { it.navigate(Routes.MODELS) },
    Tile("أدلة التركيب", Icons.Default.Construction) { it.navigate(Routes.guides(GuideType.INSTALL)) },
    Tile("أدلة البرمجة", Icons.Default.Settings) { it.navigate(Routes.guides(GuideType.PROGRAMMING)) },
    Tile("التوصيلات الكهربائية", Icons.Default.ElectricalServices) { it.navigate(Routes.guides(GuideType.WIRING)) },
    Tile("الريموت والحساسات", Icons.Default.SettingsRemote) { it.navigate(Routes.guides(GuideType.REMOTE)) },
    Tile("قطع الغيار", Icons.Default.Handyman) { it.navigate(Routes.PARTS) },
    Tile("المفضلة", Icons.Default.Bookmark) { it.navigate(Routes.FAVORITES) },
)

@Composable
fun HomeScreen(nav: NavController) {
    val container = LocalContainer.current
    val vm = hamoVm { HomeViewModel(container.repo) }
    val stats by vm.stats.collectAsStateWithLifecycle()

    Scaffold(topBar = { HamoTopBar("HAMO DOOR TECH") }) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().background(Color(0xFFF4F7FB)),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF071A31), Navy, Color(0xFF24517B))), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(42.dp).background(Color(0x33FFFFFF), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Build, null, tint = Orange, modifier = Modifier.size(25.dp))
                            }
                            Column {
                                Text("HAMO DOOR TECH", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text("المكتبة الفنية للفنيين", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("كل معلومة فنية في مكان واحد", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "كتالوجات • توصيلات • برمجة • ريموتات • أعطال",
                            color = Color(0xFFE2E8F0),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.background(Orange, RoundedCornerShape(10.dp)).clickable { nav.goTop(Routes.SEARCH) }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text("ابحث الآن", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(Modifier.background(Color(0x26FFFFFF), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("يعمل دون إنترنت", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    Modifier.fillMaxWidth().clickable { nav.goTop(Routes.SEARCH) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(Modifier.padding(horizontal = 15.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(42.dp).background(Color(0xFFFFF1E6), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Search, null, tint = Orange)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("ابحث عن مكينة أو عطل", color = Navy, fontWeight = FontWeight.Bold)
                            Text("اسم الشركة، رقم الموديل أو كود الخطأ", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.Tune, null, tint = Color(0xFF64748B))
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("تصفّح حسب نوع البوابة", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                    Text("اختار نوع النظام للوصول للمكينات والأدلة المناسبة", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        GateTypeCard(
                            title = "أبواب الجراج العلوية",
                            subtitle = "Overhead",
                            icon = Icons.Default.Construction,
                            modifier = Modifier.weight(1f),
                            onClick = { nav.navigate(Routes.CATEGORIES) },
                        )
                        GateTypeCard(
                            title = "بوابات منزلقة",
                            subtitle = "Sliding",
                            icon = Icons.Default.Category,
                            modifier = Modifier.weight(1f),
                            onClick = { nav.navigate(Routes.CATEGORIES) },
                        )
                        GateTypeCard(
                            title = "ضلفتين",
                            subtitle = "Swing",
                            icon = Icons.Default.Build,
                            modifier = Modifier.weight(1f),
                            onClick = { nav.navigate(Routes.CATEGORIES) },
                        )
                    }
                }
            }
            item {
                Text("نظرة سريعة", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                when (val s = stats) {
                    is UiState.Success -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("الموديلات", s.value.models, Modifier.weight(1f))
                        StatBox("الأدلة", s.value.guides, Modifier.weight(1f))
                        StatBox("الأعطال", s.value.faults, Modifier.weight(1f))
                        StatBox("الزيارات", s.value.jobs, Modifier.weight(1f))
                    }
                    else -> Unit
                }
            }
            item {
                Text("استكشف المكتبة الفنية", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                Text("اختار القسم اللي تحتاجه", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
            }
            items(tiles.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { t ->
                        TileCard(t, Modifier.weight(1f)) { t.action(nav) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("آخر الزيارات", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { nav.goTop(Routes.JOBS) }) { Text("عرض الكل") }
                }
            }
            when (val s = stats) {
                is UiState.Success -> {
                    if (s.value.recentJobs.isEmpty()) {
                        item {
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.Build, null, tint = Orange)
                                    Column {
                                        Text("لسه مفيش زيارات صيانة", color = Navy, fontWeight = FontWeight.Bold)
                                        Text("أضف أول زيارة من تبويب الصيانة لمتابعة شغلك.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    } else {
                        items(s.value.recentJobs, key = { it.id }) { j ->
                            ListCard(
                                title = j.customerName,
                                subtitle = Formatting.date(j.visitDate) + if (j.problem.isNotBlank()) " — ${j.problem}" else "",
                                badges = { if (j.isSeed) SeedBadge() },
                                onClick = { nav.navigate(Routes.job(j.id)) },
                            )
                        }
                    }
                }
                is UiState.Error -> item { Text(s.message, color = MaterialTheme.colorScheme.error) }
                UiState.Loading -> item { Text("جارٍ التحميل...", color = Color(0xFF64748B)) }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StatBox(label: String, value: Int, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", style = MaterialTheme.typography.titleLarge, color = Orange)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GateTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Navy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(Modifier.size(38.dp).background(Color(0x26FFFFFF), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(22.dp))
            }
            Text(title, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(subtitle, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TileCard(t: Tile, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(44.dp).background(Color(0xFFFFF1E6), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(t.icon, null, tint = Orange, modifier = Modifier.size(23.dp))
            }
            Text(t.label, style = MaterialTheme.typography.bodyMedium, color = Navy, fontWeight = FontWeight.Bold, minLines = 2)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(nav: NavController, diagnose: Boolean) {
    val container = LocalContainer.current
    val vm = hamoVm(key = if (diagnose) "diagnose" else "search") {
        SearchViewModel(container.repo, if (diagnose) setOf(SearchType.FAULT) else emptySet())
    }
    val catalog = hamoVm { CatalogViewModel(container.repo) }
    val names by catalog.names.collectAsStateWithLifecycle()
    val results = vm.results
    var showFilters by remember { mutableStateOf(false) }

    val activeFilters = listOf(vm.categoryId, vm.brandId, vm.modelId).count { it != null } + if (vm.typeFilter != null) 1 else 0

    Scaffold(topBar = { HamoTopBar(if (diagnose) "تشخيص الأعطال" else "البحث الشامل") }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = vm.query,
                    onValueChange = vm::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(if (diagnose) "صف العطل أو العرض أو اكتب كود الخطأ" else "ابحث بالعربية أو الإنجليزية") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (vm.query.isNotEmpty()) {
                            IconButton(onClick = { vm.updateQuery("") }) { Icon(Icons.Default.Clear, "مسح") }
                        }
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(if (activeFilters > 0) "تصفية ($activeFilters)" else "تصفية")
                    }
                    if (activeFilters > 0) TextButton(onClick = vm::clearFilters) { Text("مسح التصفية") }
                }
                if (showFilters) {
                    val st = names
                    if (st is UiState.Success) {
                        val n = st.value
                        DropdownField(
                            "نوع الباب",
                            listOf<Pair<Long?, String>>(null to "الكل") + n.categories.map { it.key as Long? to it.value },
                            vm.categoryId, vm::setCategory,
                        )
                        DropdownField(
                            "الشركة",
                            listOf<Pair<Long?, String>>(null to "الكل") + n.brands.map { it.key as Long? to it.value },
                            vm.brandId, vm::setBrand,
                        )
                        val modelOptions = n.models.values
                            .filter { (vm.brandId == null || it.brandId == vm.brandId) && (vm.categoryId == null || it.categoryId == vm.categoryId) }
                            .map { it.id as Long? to it.name }
                        DropdownField("الموديل", listOf<Pair<Long?, String>>(null to "الكل") + modelOptions, vm.modelId, vm::setModel)
                    }
                    if (!diagnose) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = vm.typeFilter == null, onClick = { vm.setType(null) }, label = { Text("الكل") })
                            SearchType.entries.forEach { t ->
                                FilterChip(selected = vm.typeFilter == t, onClick = { vm.setType(t) }, label = { Text(t.label) })
                            }
                        }
                    }
                }
            }

            when (results) {
                is UiState.Loading -> LoadingBox()
                is UiState.Error -> EmptyState("تعذّر البحث", results.message)
                is UiState.Success -> {
                    val list = results.value
                    val idle = vm.query.isBlank() && activeFilters == 0
                    if (diagnose && idle) {
                        EmptyState(
                            "ابدأ بوصف العطل",
                            "اكتب العرض أو كود الخطأ، أو اختر نوع الباب والشركة والموديل لتضييق النتائج. النتائج هنا احتمالات لمساعدتك وليست تشخيصًا مؤكدًا.",
                        )
                    } else if (list.isEmpty()) {
                        EmptyState(
                            if (diagnose) "لا يوجد تطابق موثّق" else "لا توجد نتائج",
                            if (diagnose) "لم نجد عطلًا مطابقًا. حدّد الشركة والموديل أو صف المشكلة بكلمات أخرى، أو أضف العطل لقاعدة بياناتك." else "جرّب كلمات أخرى أو أزل بعض عوامل التصفية.",
                            action = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (vm.query.isNotBlank()) {
                                        PrimaryButton("ابحث عنها في الإنترنت (ذكاء اصطناعي)", { nav.navigate(Routes.ai(vm.query)) })
                                    }
                                    if (diagnose) {
                                        SecondaryButton("إضافة عطل جديد", { nav.navigate(Routes.faultEdit(0, vm.modelId ?: 0)) })
                                    }
                                }
                            },
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (diagnose) {
                                item {
                                    Text(
                                        "النتائج احتمالات مرتبة حسب التطابق وليست تشخيصًا مؤكدًا. افحص بأمان واستعن بفني مؤهل أو دليل الشركة عند الحاجة.",
                                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            items(list, key = { "${it.doc.type}_${it.doc.id}" }) { r ->
                                ResultCard(r) { open(nav, r) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun open(nav: NavController, r: SearchResult) {
    when (r.doc.type) {
        SearchType.FAULT -> nav.navigate(Routes.fault(r.doc.id))
        SearchType.MODEL -> nav.navigate(Routes.model(r.doc.id))
        SearchType.GUIDE -> nav.navigate(Routes.guide(r.doc.id))
        SearchType.BRAND -> nav.navigate(Routes.MODELS)
    }
}

@Composable
private fun ResultCard(r: SearchResult, onClick: () -> Unit) {
    val d = r.doc
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    ListCard(
        title = if (d.type == SearchType.MODEL) d.title.ltr() else d.title,
        subtitle = buildString {
            if (d.subtitle.isNotBlank()) append(d.subtitle)
            if (d.source.isNotBlank()) { if (isNotEmpty()) append(" • "); append("المصدر: ${d.source}") }
        },
        badges = {
            Chip(d.type.label, Color(0xFFDCE6F5), Navy)
            if (d.type == SearchType.FAULT || d.type == SearchType.GUIDE) {
                VerificationBadge(d.level)
                if (d.modelId == null) Chip("عام") else Chip(if (r.modelSpecific) "خاص بالموديل" else "لموديل محدد", Color(0xFFFFEDD5), Color(0xFF9A3412))
            }
        },
        trailing = {},
        onClick = onClick,
    )
    if (r.matched.isNotEmpty()) {
        Text("تطابق في: ${r.matched.joinToString("، ")}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
    }
    }
}
