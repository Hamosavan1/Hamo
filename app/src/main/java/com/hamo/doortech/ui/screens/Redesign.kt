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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.DoorSliding
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.CatalogViewModel
import com.hamo.doortech.ui.GuideDetailViewModel
import com.hamo.doortech.ui.HomeViewModel
import com.hamo.doortech.ui.ModelDetailViewModel
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.InfoRow
import com.hamo.doortech.ui.components.ListCard
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.MediaSection
import com.hamo.doortech.ui.components.NumberedStep
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.VerificationBadge
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.goTop
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.NavyLight
import com.hamo.doortech.ui.theme.Orange
import com.hamo.doortech.util.ltr
import kotlinx.coroutines.delay

private val DeepNavy = Color(0xFF071A31)
private val PanelNavy = Color(0xFF0F2D52)
private val PanelBorder = Color(0xFF27507F)
private val SoftText = Color(0xFFCBD5E1)
private val BgGradient = Brush.verticalGradient(listOf(Color(0xFF05152A), Color(0xFF0B2547), Color(0xFF123A66)))

// ======================= شاشة البداية =======================

@Composable
fun HamoSplash(onStart: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2600)
        onStart()
    }
    Box(Modifier.fillMaxSize().background(BgGradient), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(104.dp).clip(CircleShape).background(PanelNavy).padding(2.dp)
                    .background(Brush.linearGradient(listOf(Orange, Color(0xFFFFB066))), CircleShape).padding(3.dp)
                    .background(PanelNavy, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Build, null, tint = Orange, modifier = Modifier.size(54.dp))
            }
            Text("HAMO", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
            Text("DOOR TECH", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "المكتبة الفنية الشاملة لفنيي أبواب الجراج والبوابات الأوتوماتيكية",
                color = SoftText, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("ابدأ الآن", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
        }
    }
}

// ======================= الرئيسية =======================

private data class CatTile(val title: String, val sub: String, val key: String, val icon: ImageVector)

private val catTiles = listOf(
    CatTile("أبواب الجراج العلوية", "Overhead Garage Doors", "جراج", Icons.Default.Garage),
    CatTile("البوابات المنزلقة", "Sliding Gate Motors", "منزلق", Icons.Default.DoorSliding),
    CatTile("البوابات ذات الضلفتين", "Double Leaf Swing Gates", "ضلف", Icons.Default.DoorFront),
)

private fun List<DoorCategory>.idFor(key: String): Long = firstOrNull { it.name.contains(key) }?.id ?: 0L

@Composable
fun HomeScreen(nav: NavController) {
    val container = LocalContainer.current
    val catalog = hamoVm { CatalogViewModel(container.repo) }
    val home = hamoVm { HomeViewModel(container.repo) }
    val cats by catalog.categories.collectAsStateWithLifecycle()
    val stats by home.stats.collectAsStateWithLifecycle()
    val catList = (cats as? UiState.Success)?.value ?: emptyList()

    Box(Modifier.fillMaxSize().background(BgGradient)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).background(PanelNavy, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = Orange, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("HAMO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("DOOR TECH", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            // شريط البحث
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                    .clickable { nav.goTop(Routes.SEARCH) }.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Search, null, tint = Color(0xFF64748B))
                Text("ابحث عن موديل أو شركة أو عطل...", color = Color(0xFF64748B))
            }
            // الأقسام الثلاثة
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                catTiles.forEach { t ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(PanelNavy)
                            .clickable { nav.navigate(Routes.brands(catList.idFor(t.key))) }
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(t.icon, null, tint = Color.White, modifier = Modifier.size(38.dp))
                        Text(t.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            val s = (stats as? UiState.Success)?.value
            if (s != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeStat("الموديلات", s.models, Modifier.weight(1f))
                    HomeStat("الأدلة", s.guides, Modifier.weight(1f))
                    HomeStat("الأعطال", s.faults, Modifier.weight(1f))
                }
            }
            Text("أقسام أخرى", color = Orange, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OtherTile("تشخيص الأعطال", Icons.Default.MedicalServices, Modifier.weight(1f)) { nav.navigate(Routes.DIAGNOSE) }
                OtherTile("الشركات", Icons.Default.Business, Modifier.weight(1f)) { nav.navigate(Routes.brands(0)) }
                OtherTile("قطع الغيار", Icons.Default.Handyman, Modifier.weight(1f)) { nav.navigate(Routes.PARTS) }
                OtherTile("الصيانة", Icons.Default.Construction, Modifier.weight(1f)) { nav.navigate(Routes.JOBS) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OtherTile("أدلة البرمجة", Icons.Default.Settings, Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.PROGRAMMING)) }
                OtherTile("التوصيلات", Icons.Default.Memory, Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.WIRING)) }
                OtherTile("بحث بالإنترنت", Icons.Default.Search, Modifier.weight(1f)) { nav.navigate(Routes.ai()) }
                OtherTile("لوحة الإدارة", Icons.Default.Category, Modifier.weight(1f)) { nav.navigate(Routes.ADMIN) }
            }
        }
    }
}

@Composable
private fun HomeStat(label: String, value: Int, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x22FFFFFF)).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$value", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = SoftText, fontSize = 12.sp)
    }
}

@Composable
private fun OtherTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = Navy, modifier = Modifier.size(26.dp))
        Text(label, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2)
    }
}

// ======================= قائمة الشركات (مع عدد الموديلات) =======================

private val brandColors = listOf(
    Color(0xFFEA580C), Color(0xFF1D4ED8), Color(0xFFDC2626), Color(0xFF0F766E),
    Color(0xFF7C3AED), Color(0xFF0369A1), Color(0xFFB45309), Color(0xFF334155),
)

@Composable
fun BrandsListScreen(nav: NavController, catId: Long) {
    val container = LocalContainer.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val brands by vm.brands.collectAsStateWithLifecycle()
    val models by vm.models.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val catName = (cats as? UiState.Success)?.value?.firstOrNull { it.id == catId }?.name

    Scaffold(topBar = { HamoTopBar(catName ?: "الشركات المصنعة", onBack = { nav.popBackStack() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(Color(0xFFF4F7FB))) {
            SearchBox(text, { text = it }, "ابحث عن شركة...")
            StateContent(brands, "لا توجد شركات", "أضف شركات من لوحة الإدارة.", isEmpty = { it.isEmpty() }) { all ->
                val ms = (models as? UiState.Success)?.value ?: emptyList()
                val q = text.trim().lowercase()
                val rows = all.map { b ->
                    b to ms.count { it.brandId == b.id && (catId == 0L || it.categoryId == catId) }
                }.filter { (b, n) ->
                    (catId == 0L || n > 0) && (q.isEmpty() || b.name.lowercase().contains(q))
                }
                if (rows.isEmpty()) EmptyState("لا توجد شركات", "لا توجد شركات مطابقة.")
                else LazyColumn(
                    contentPadding = PaddingValues(14.dp, 4.dp, 14.dp, 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(rows, key = { it.first.id }) { (b, n) ->
                        val color = brandColors[(b.name.sumOf { it.code }) % brandColors.size]
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                                .clickable { nav.navigate(Routes.modelsFor(b.id, catId)) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(Modifier.size(46.dp).background(color, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Text(b.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(b.name.ltr(), fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                                if (b.country.isNotBlank()) Text(b.country, color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                            Text("$n موديل", color = Color(0xFF64748B), fontSize = 13.sp)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, hint: String) {
    OutlinedTextField(
        value = value, onValueChange = onChange, singleLine = true,
        placeholder = { Text(hint) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(14.dp, 10.dp, 14.dp, 6.dp),
    )
}

// ======================= قائمة الموديلات =======================

@Composable
fun ModelsScreen(nav: NavController, brandId: Long, catId: Long) {
    val container = LocalContainer.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val models by vm.models.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var cat by remember(catId) { mutableStateOf(catId) }
    val n = (names as? UiState.Success)?.value
    val title = n?.brand(brandId.takeIf { it > 0 })?.ltr() ?: "قائمة الموديلات"

    Scaffold(
        topBar = { HamoTopBar(title, onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.modelEdit(0, brandId)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("موديل جديد") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(Color(0xFFF4F7FB))) {
            SearchBox(text, { text = it }, "ابحث عن موديل...")
            val catList = (cats as? UiState.Success)?.value ?: emptyList()
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = cat == 0L, onClick = { cat = 0L }, label = { Text("جميع الموديلات") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy, selectedLabelColor = Color.White))
                catList.forEach { c ->
                    FilterChip(selected = cat == c.id, onClick = { cat = c.id }, label = { Text(c.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy, selectedLabelColor = Color.White))
                }
            }
            StateContent(models, "لا توجد موديلات", "أضف موديلات الماكينات.", isEmpty = { it.isEmpty() }) { all ->
                val q = text.trim().lowercase()
                val list = all.filter { m ->
                    (brandId == 0L || m.brandId == brandId) && (cat == 0L || m.categoryId == cat) &&
                        (q.isEmpty() || m.name.lowercase().contains(q) || m.controlUnit.lowercase().contains(q))
                }
                if (list.isEmpty()) EmptyState("لا توجد نتائج", "غيّر القسم أو كلمة البحث.")
                else LazyColumn(
                    contentPadding = PaddingValues(14.dp, 10.dp, 14.dp, 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(list, key = { it.id }) { m ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                                .clickable { nav.navigate(Routes.model(m.id)) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MachineIcon(brandName = n?.brands?.get(m.brandId), modelName = m.name, size = 58.dp)
                            Column(Modifier.weight(1f)) {
                                Text(m.name.ltr(), fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                                Text(
                                    listOfNotNull(n?.brands?.get(m.brandId)?.ltr(), n?.category(m.categoryId)).joinToString(" • "),
                                    color = Color(0xFF64748B), fontSize = 12.sp,
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}

// ======================= تفاصيل المكينة بالتبويبات =======================

@Composable
private fun GuideBlock(nav: NavController, g: TechnicalGuide) {
    val container = LocalContainer.current
    val vm = hamoVm(key = "block_${g.id}") { GuideDetailViewModel(container.repo, g.id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val steps = (state as? UiState.Success)?.value?.full?.orderedSteps ?: emptyList()
    var expanded by remember(g.id) { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Construction, contentDescription = null, tint = Orange, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(g.title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 17.sp, lineHeight = 24.sp)
                    Text(when (g.type) {
                        GuideType.PROGRAMMING -> "دليل برمجة خطوة بخطوة"
                        GuideType.WIRING -> "دليل التوصيلات"
                        GuideType.REMOTE -> "دليل إعداد الريموت"
                        GuideType.INSTALL -> "دليل التركيب"
                        else -> "دليل فني"
                    }, color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                VerificationBadge(g.level)
                Text(if (steps.isEmpty()) "الخطوات غير مضافة" else "${steps.size} خطوات",
                    color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            if (g.summary.isNotBlank()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("قبل أن تبدأ", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(g.summary, color = Color(0xFF334155), fontSize = 14.sp, lineHeight = 22.sp)
                }
            }
            if (g.warnings.isNotBlank()) WarningBox(g.warnings)
            if (steps.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFF6FF))
                    .clickable { expanded = !expanded }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("خطوات التنفيذ", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(if (expanded) "إخفاء ▲" else "عرض الخطوات ▼", color = NavyLight, fontSize = 12.sp)
                }
                if (expanded) steps.forEachIndexed { i, step ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(Navy), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(step.text, Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC)).padding(horizontal = 12.dp, vertical = 10.dp),
                            color = Color(0xFF1E293B), fontSize = 14.sp, lineHeight = 23.sp)
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF7ED)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("لا توجد خطوات تفصيلية حتى الآن", color = Color(0xFF9A3412), fontWeight = FontWeight.Bold)
                    Text("أضف الخطوات من لوحة الإدارة، وراجع كتالوج الشركة المصنّعة قبل تنفيذ أي إعداد.",
                        color = Color(0xFF7C2D12), fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            Text("الصور والمرفقات", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            MediaSection(OwnerType.GUIDE, g.id)
            if (g.source.isNotBlank()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("المصدر الفني", color = Color(0xFF64748B), fontSize = 11.sp)
                    Text(g.source, color = Color(0xFF334155), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            Button(onClick = { nav.navigate(Routes.guide(g.id)) }, modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("فتح الدليل كاملًا", fontWeight = FontWeight.Bold)
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
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل الماكينة", onBack = { nav.popBackStack() }) {
            IconButton(onClick = vm::toggleFavorite) {
                Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, "مفضلة", tint = if (fav) Orange else Color.White)
            }
            IconButton(onClick = { nav.navigate(Routes.modelEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "الموديل غير موجود") { d ->
            if (d == null) return@StateContent
            val wiring = d.guides.filter { it.type == GuideType.WIRING }
            val program = d.guides.filter { it.type == GuideType.PROGRAMMING }
            val remote = d.guides.filter { it.type == GuideType.REMOTE }
            val install = d.guides.filter { it.type == GuideType.INSTALL }
            val tabs = buildList {
                add("نظرة عامة")
                add("التوصيلات (${wiring.size})")
                add("برمجة المكينة (${program.size})")
                add("برمجة الريموت (${remote.size})")
                if (install.isNotEmpty()) add("التركيب (${install.size})")
                add("الأعطال (${d.faults.size})")
                add("الكتالوج")
            }
            val sel = tab.coerceIn(0, tabs.size - 1)
            val label = tabs[sel]
            val documented = d.guides.any { it.level == Verification.DOCUMENTED }

            Column(Modifier.padding(pad).fillMaxSize().background(Color(0xFFF4F7FB))) {
                Row(
                    Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(DeepNavy, NavyLight))).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MachineIcon(d.brandName, d.model.name, size = 84.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(d.model.name.ltr(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(
                            listOfNotNull(d.brandName?.ltr(), d.categoryName).joinToString(" • "),
                            color = SoftText, fontSize = 13.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Chip(
                                if (documented) "معلومات موثّقة من كتالوج" else "راجع المصدر",
                                if (documented) Color(0xFFDCFCE7) else Color(0xFFFEF9C3),
                                if (documented) Color(0xFF166534) else Color(0xFF854D0E),
                            )
                        }
                    }
                }
                ScrollableTabRow(
                    selectedTabIndex = sel, edgePadding = 8.dp,
                    containerColor = Color.White, contentColor = Navy,
                ) {
                    tabs.forEachIndexed { i, t ->
                        Tab(selected = sel == i, onClick = { tab = i }, text = { Text(t, fontWeight = if (sel == i) FontWeight.Bold else FontWeight.Normal) },
                            selectedContentColor = Navy, unselectedContentColor = Color(0xFF64748B))
                    }
                }
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when {
                        label == "نظرة عامة" -> {
                            SectionCard("المواصفات") {
                                InfoRow("الشركة", d.brandName.orEmpty().ltr())
                                InfoRow("نوع الباب", d.categoryName.orEmpty())
                                InfoRow("وحدة التحكم", d.model.controlUnit.ltr())
                                InfoRow("المواصفات", d.model.specs)
                                InfoRow("الريموت", d.model.remoteInfo)
                                InfoRow("الحساسات", d.model.sensorsInfo)
                                InfoRow("المصدر", d.model.source)
                                InfoRow("ملاحظات", d.model.notes)
                            }
                            if (d.parts.isNotEmpty()) SectionCard("قطع الغيار (${d.parts.size})") {
                                d.parts.forEach { p -> Text("• ${p.name} ${p.partNumber.ltr()} — المخزون: ${p.quantity}") }
                            }
                        }
                        label.startsWith("التوصيلات") -> guideTab(nav, wiring, "لا يوجد مخطط توصيل مسجّل لهذا الموديل بعد.")
                        label.startsWith("برمجة المكينة") -> guideTab(nav, program, "لا توجد خطوات برمجة مسجّلة لهذا الموديل بعد.")
                        label.startsWith("برمجة الريموت") -> guideTab(nav, remote, "لا توجد خطوات برمجة ريموت مسجّلة لهذا الموديل بعد.")
                        label.startsWith("التركيب") -> guideTab(nav, install, "")
                        label.startsWith("الأعطال") -> {
                            if (d.faults.isEmpty()) Text("لا توجد أعطال مسجلة لهذا الموديل.", color = Color(0xFF64748B))
                            d.faults.forEach { f ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                                        .clickable { nav.navigate(Routes.fault(f.id)) }.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(Modifier.size(44.dp).background(Orange, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.MedicalServices, null, tint = Color.White)
                                    }
                                    Text(f.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Navy)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF94A3B8))
                                }
                            }
                            TextButton(onClick = { nav.navigate(Routes.faultEdit(0, id)) }) { Text("إضافة عطل لهذا الموديل") }
                        }
                        else -> {
                            Text("الكتالوج الأصلي والصور والروابط والملفات المرفقة بهذا الموديل:", color = Color(0xFF475569))
                            MediaSection(OwnerType.MODEL, id)
                        }
                    }
                }
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

@Composable
private fun guideTab(nav: NavController, list: List<TechnicalGuide>, empty: String) {
    if (list.isEmpty() && empty.isNotBlank()) {
        Text(empty, color = Color(0xFF64748B))
        Text("يمكنك إضافتها من لوحة الإدارة أو البحث بالإنترنت (ذكاء اصطناعي).", color = Color(0xFF64748B), fontSize = 12.sp)
    }
    list.forEach { g -> GuideBlock(nav, g) }
}

// ======================= لوحة الإدارة =======================

private data class AdminEntry(val label: String, val icon: ImageVector, val route: String)

@Composable
fun AdminScreen(nav: NavController) {
    val entries = listOf(
        AdminEntry("إضافة شركة", Icons.Default.Business, Routes.BRANDS_ADMIN),
        AdminEntry("إضافة موديل", Icons.Default.Memory, Routes.modelEdit(0)),
        AdminEntry("إضافة دليل", Icons.Default.Construction, Routes.guideEdit(0, GuideType.PROGRAMMING)),
        AdminEntry("إضافة عطل", Icons.Default.MedicalServices, Routes.faultEdit(0)),
        AdminEntry("التصنيفات", Icons.Default.Category, Routes.CATEGORIES),
        AdminEntry("قطع الغيار", Icons.Default.Handyman, Routes.PARTS),
        AdminEntry("الصور والمرفقات", Icons.Default.PhotoLibrary, Routes.MEDIA),
        AdminEntry("النسخ الاحتياطي", Icons.Default.Backup, Routes.BACKUP),
        AdminEntry("الإعدادات", Icons.Default.Settings, Routes.SETTINGS),
    )
    Scaffold(topBar = { HamoTopBar("لوحة الإدارة", onBack = { nav.popBackStack() }) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().background(Color(0xFFF4F7FB)).verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            entries.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { e ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White)
                                .clickable { nav.navigate(e.route) }.padding(vertical = 18.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(e.icon, null, tint = Navy, modifier = Modifier.size(32.dp))
                            Text(e.label, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
