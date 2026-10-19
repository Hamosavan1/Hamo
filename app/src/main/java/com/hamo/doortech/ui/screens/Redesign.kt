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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Info
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
import com.hamo.doortech.ui.components.GuideIcon
import com.hamo.doortech.ui.components.GuideStyle
import com.hamo.doortech.ui.components.BeforeYouStart
import com.hamo.doortech.ui.components.StepsChecklist
import com.hamo.doortech.ui.components.TermsCard
import com.hamo.doortech.ui.components.guideStyle
import com.hamo.doortech.ui.theme.FaultRose
import com.hamo.doortech.ui.theme.FaultSoft
import com.hamo.doortech.ui.theme.InstallTeal
import com.hamo.doortech.ui.theme.MutedText
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.OrangeSoft
import com.hamo.doortech.ui.theme.ProgBlue
import com.hamo.doortech.ui.theme.RemoteViolet
import com.hamo.doortech.ui.theme.WireAmber
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

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // الرأس: الشعار + ترحيب + البحث
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.linearGradient(listOf(DeepNavy, NavyLight))).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Orange), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("HAMO DOOR TECH", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("مكتبتك الفنية في جيبك", color = SoftText, fontSize = 13.sp)
                }
            }
            Text("عايز تعمل إيه النهارده؟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .clickable { nav.goTop(Routes.SEARCH) }.padding(horizontal = 14.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Search, null, tint = MutedText)
                Text("ابحث عن موديل أو شركة أو عطل...", color = MutedText)
            }
        }

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // الأقسام الأساسية: كروت كبيرة ملونة
            SectionTitle("الأقسام الأساسية")
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigTile(guideStyle(GuideType.PROGRAMMING), Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.PROGRAMMING)) }
                BigTile(guideStyle(GuideType.WIRING), Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.WIRING)) }
            }
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigTile(guideStyle(GuideType.REMOTE), Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.REMOTE)) }
                BigTile(
                    GuideStyle(FaultRose, FaultSoft, Icons.Default.MedicalServices, "تشخيص الأعطال", "اكتب العَرَض واعرف السبب والحل"),
                    Modifier.weight(1f),
                ) { nav.navigate(Routes.DIAGNOSE) }
            }

            // نوع البوابة
            SectionTitle("اختار نوع البوابة")
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                catTiles.forEach { t ->
                    Column(
                        Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(Color.White)
                            .clickable { nav.navigate(Routes.brands(catList.idFor(t.key))) }
                            .padding(vertical = 16.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(OrangeSoft), contentAlignment = Alignment.Center) {
                            Icon(t.icon, null, tint = Orange, modifier = Modifier.size(30.dp))
                        }
                        Text(t.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    }
                }
            }

            val s = (stats as? UiState.Success)?.value
            if (s != null) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    HomeStat("موديل", s.models)
                    HomeStat("دليل", s.guides)
                    HomeStat("عطل", s.faults)
                }
            }

            SectionTitle("أدوات تانية")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OtherTile("الشركات", Icons.Default.Business, Modifier.weight(1f)) { nav.navigate(Routes.brands(0)) }
                OtherTile("التركيب", Icons.Default.Construction, Modifier.weight(1f)) { nav.navigate(Routes.guides(GuideType.INSTALL)) }
                OtherTile("قطع الغيار", Icons.Default.Handyman, Modifier.weight(1f)) { nav.navigate(Routes.PARTS) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OtherTile("الصيانة", Icons.Default.Build, Modifier.weight(1f)) { nav.navigate(Routes.JOBS) }
                OtherTile("بحث بالإنترنت", Icons.Default.Search, Modifier.weight(1f)) { nav.navigate(Routes.ai()) }
                OtherTile("لوحة الإدارة", Icons.Default.Category, Modifier.weight(1f)) { nav.navigate(Routes.ADMIN) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
}

/** كارت قسم كبير: أيقونة ملونة + اسم + جملة بسيطة تشرح القسم. */
@Composable
private fun BigTile(style: GuideStyle, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color.White).clickable(onClick = onClick).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GuideIcon(style, size = 46)
        Text(style.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(style.hint, color = MutedText, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2)
    }
}

@Composable
private fun HomeStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(label, color = MutedText, fontSize = 12.sp)
    }
}

@Composable
private fun OtherTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(onClick = onClick).padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = NavyLight, modifier = Modifier.size(26.dp))
        Text(label, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2)
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
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
private fun GuideBlock(nav: NavController, g: TechnicalGuide, startOpen: Boolean) {
    val container = LocalContainer.current
    val vm = hamoVm(key = "block_${g.id}") { GuideDetailViewModel(container.repo, g.id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val steps = (state as? UiState.Success)?.value?.full?.orderedSteps?.map { it.text } ?: emptyList()
    var expanded by remember(g.id) { mutableStateOf(startOpen) }
    val style = guideStyle(g.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // العنوان: الضغط عليه يفتح/يقفل الدليل
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GuideIcon(style, size = 44)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(g.title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp, lineHeight = 23.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        VerificationBadge(g.level)
                        Text(if (steps.isEmpty()) "بدون خطوات" else "${steps.size} خطوات", color = MutedText, fontSize = 12.sp)
                    }
                }
                Text(if (expanded) "▲" else "▼", color = style.color, fontSize = 14.sp)
            }
            if (expanded) {
                BeforeYouStart(g.summary, style)
                WarningBox(g.warnings)
                if (steps.isNotEmpty()) {
                    TermsCard(steps.joinToString("\n") + "\n" + g.summary)
                    StepsChecklist("block_${g.id}", steps, style.color)
                } else {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF7ED)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("لسه مفيش خطوات تفصيلية", color = Color(0xFF9A3412), fontWeight = FontWeight.Bold)
                        Text("أضف الخطوات من لوحة الإدارة، وراجع كتالوج الشركة المصنّعة قبل تنفيذ أي إعداد.",
                            color = Color(0xFF7C2D12), fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
                Text("الصور والمخططات", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                MediaSection(OwnerType.GUIDE, g.id)
                if (g.source.isNotBlank()) Text("المصدر: ${g.source}", color = MutedText, fontSize = 11.sp, lineHeight = 17.sp)
                Button(onClick = { nav.navigate(Routes.guide(g.id)) }, modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = style.color)) {
                    Text("افتح الدليل في صفحة كاملة", fontWeight = FontWeight.Bold)
                }
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
                add(ModelTab("نظرة عامة", Icons.Default.Info, Navy, -1))
                add(ModelTab("التوصيلات", guideStyle(GuideType.WIRING).icon, WireAmber, wiring.size))
                add(ModelTab("برمجة المكينة", guideStyle(GuideType.PROGRAMMING).icon, ProgBlue, program.size))
                add(ModelTab("الريموت", guideStyle(GuideType.REMOTE).icon, RemoteViolet, remote.size))
                if (install.isNotEmpty()) add(ModelTab("التركيب", guideStyle(GuideType.INSTALL).icon, InstallTeal, install.size))
                add(ModelTab("الأعطال", Icons.Default.MedicalServices, FaultRose, d.faults.size))
                add(ModelTab("الكتالوج", Icons.Default.PhotoLibrary, Navy, -1))
            }
            val sel = tab.coerceIn(0, tabs.size - 1)
            val label = tabs[sel].label
            val documented = d.guides.any { it.level == Verification.DOCUMENTED }

            Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                Row(
                    Modifier.fillMaxWidth().background(Color.White).horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tabs.forEachIndexed { i, t ->
                        val on = sel == i
                        Row(
                            Modifier.clip(RoundedCornerShape(50)).background(if (on) t.color else Color(0xFFF1F5F9))
                                .clickable { tab = i }.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(t.icon, null, tint = if (on) Color.White else t.color, modifier = Modifier.size(18.dp))
                            Text(t.label, color = if (on) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (t.count >= 0) Text("${t.count}", color = if (on) Color.White.copy(alpha = 0.85f) else MutedText, fontSize = 12.sp)
                        }
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
                        label == "التوصيلات" -> guideTab(nav, wiring, "لا يوجد مخطط توصيل مسجّل لهذا الموديل بعد.")
                        label == "برمجة المكينة" -> guideTab(nav, program, "لا توجد خطوات برمجة مسجّلة لهذا الموديل بعد.")
                        label == "الريموت" -> guideTab(nav, remote, "لا توجد خطوات برمجة ريموت مسجّلة لهذا الموديل بعد.")
                        label == "التركيب" -> guideTab(nav, install, "")
                        label == "الأعطال" -> {
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
    // أول دليل مفتوح، والباقي مقفول عشان الصفحة تبقى مرتبة وسهلة التصفح.
    list.forEachIndexed { i, g -> GuideBlock(nav, g, startOpen = i == 0) }
}

// ======================= لوحة الإدارة =======================

private data class ModelTab(val label: String, val icon: ImageVector, val color: Color, val count: Int)

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
            Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(14.dp),
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
