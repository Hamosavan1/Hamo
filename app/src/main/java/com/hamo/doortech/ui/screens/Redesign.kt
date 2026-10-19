package com.hamo.doortech.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.R
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.CatalogViewModel
import com.hamo.doortech.ui.FaultListViewModel
import com.hamo.doortech.ui.GuideDetailViewModel
import com.hamo.doortech.ui.ModelDetailViewModel
import com.hamo.doortech.ui.NameMaps
import com.hamo.doortech.ui.Routes
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.BeforeYouStart
import com.hamo.doortech.ui.components.BrandLogo
import com.hamo.doortech.ui.components.Chip
import com.hamo.doortech.ui.components.ConfirmDialog
import com.hamo.doortech.ui.components.EmptyState
import com.hamo.doortech.ui.components.HamoLogo
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.MediaSection
import com.hamo.doortech.ui.components.StateContent
import com.hamo.doortech.ui.components.StepsChecklist
import com.hamo.doortech.ui.components.TermsCard
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.goTop
import com.hamo.doortech.ui.components.guideStyle
import com.hamo.doortech.ui.components.hamoVm
import com.hamo.doortech.ui.components.splitStep
import com.hamo.doortech.ui.theme.BrandBlue
import com.hamo.doortech.ui.theme.BrandBlueSoft
import com.hamo.doortech.ui.theme.DeepNavy
import com.hamo.doortech.ui.theme.MutedText
import com.hamo.doortech.ui.theme.Navy
import com.hamo.doortech.ui.theme.NavyLight
import com.hamo.doortech.ui.theme.Orange
import com.hamo.doortech.ui.theme.OrangeSoft
import com.hamo.doortech.util.ltr

private val CardLine = Color(0xFFE3E8F0)
private val SoftText = Color(0xFFCBD5E1)

// ======================= شاشة البداية =======================

@Composable
fun HamoSplash(onStart: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(2600, easing = LinearEasing))
        onStart()
    }
    Box(Modifier.fillMaxSize().background(DeepNavy).clickable(onClick = onStart)) {
        Image(
            painterResource(R.drawable.garage_photo), null,
            Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop,
        )
        // تدرّج يدمج الصورة مع الخلفية الكحلي
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xFF0E2A55), 0.45f to DeepNavy, 0.6f to DeepNavy.copy(alpha = 0.35f),
                    0.85f to Color.Transparent, 1f to DeepNavy.copy(alpha = 0.85f),
                ),
            ),
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 90.dp, start = 28.dp, end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            HamoLogo(markSize = 110.dp, vertical = true, titleSize = 46)
            Text(
                "دليلك الشامل لصيانة وبرمجة\nالأبواب الأوتوماتيكية",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 34.sp, textAlign = TextAlign.Center,
            )
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("جودة . خبرة . احترافية", color = Color.White, fontSize = 15.sp)
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.width(150.dp).height(5.dp).clip(RoundedCornerShape(50)),
                color = Orange, trackColor = Color.White.copy(alpha = 0.2f), drawStopIndicator = {},
            )
        }
    }
}

// ======================= الرئيسية =======================

private data class HomeTile(val label: String, val icon: ImageVector, val route: String)

private val homeTiles = listOf(
    HomeTile("الشركات\nوالماركات", Icons.Default.Business, Routes.brands(0)),
    HomeTile("الموديلات", Icons.Default.DeveloperBoard, Routes.modelsFor()),
    HomeTile("البرمجة", Icons.Default.Tune, Routes.guides(GuideType.PROGRAMMING)),
    HomeTile("التوصيلات", Icons.Default.Cable, Routes.guides(GuideType.WIRING)),
    HomeTile("الأعطال والحلول", Icons.Default.WarningAmber, Routes.FAULTS),
    HomeTile("قطع الغيار", Icons.Default.Settings, Routes.PARTS),
    HomeTile("سجل الصيانة", Icons.Default.AssignmentTurnedIn, Routes.JOBS),
    HomeTile("المفضلة", Icons.Default.Favorite, Routes.FAVORITES),
    HomeTile("الريموت", Icons.Default.SettingsRemote, Routes.guides(GuideType.REMOTE)),
    HomeTile("تشخيص سريع", Icons.Default.MedicalServices, Routes.DIAGNOSE),
    HomeTile("بحث بالإنترنت", Icons.Default.TravelExplore, Routes.ai()),
    HomeTile("لوحة الإدارة", Icons.Default.AdminPanelSettings, Routes.ADMIN),
)

@Composable
fun HomeScreen(nav: NavController) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // الشريط العلوي: الإشعارات (سجل الصيانة) — الشعار — القائمة
        Row(
            Modifier.fillMaxWidth().background(DeepNavy).padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { nav.navigate(Routes.JOBS) }) { Icon(Icons.Default.Notifications, "سجل الصيانة", tint = Color.White) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { HamoLogo(markSize = 40.dp, titleSize = 20) }
            IconButton(onClick = { nav.goTop(Routes.MORE) }) { Icon(Icons.Default.Menu, "القائمة", tint = Color.White) }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                    .border(1.dp, CardLine, RoundedCornerShape(14.dp))
                    .clickable { nav.goTop(Routes.SEARCH) }.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Search, null, tint = MutedText)
                Text("ابحث عن ماكينة أو ماركة أو موديل...", color = MutedText)
            }
            // البانر
            Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(18.dp))) {
                Image(painterResource(R.drawable.garage_photo), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(Color.Transparent, DeepNavy.copy(alpha = 0.55f), DeepNavy.copy(alpha = 0.92f)), startX = 0f),
                    ),
                )
                Column(
                    Modifier.align(Alignment.CenterStart).padding(16.dp).fillMaxWidth(0.68f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("حلول متكاملة", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("لجميع أنواع الأبواب الأوتوماتيكية", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = { nav.goTop(Routes.brands(0)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White),
                        shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text("تصفح الماركات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, Modifier.size(18.dp))
                    }
                }
            }
            homeTiles.chunked(3).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { t ->
                        Column(
                            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Color.White)
                                .border(1.dp, CardLine, RoundedCornerShape(16.dp))
                                .clickable { nav.navigate(t.route) }.padding(vertical = 16.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(t.icon, null, tint = Navy, modifier = Modifier.size(32.dp))
                            Text(t.label, color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ======================= الشركات والماركات (شبكة شعارات) =======================

@Composable
fun BrandsListScreen(nav: NavController, catId: Long) {
    val container = LocalContainer.current
    val vm = hamoVm { CatalogViewModel(container.repo) }
    val brands by vm.brands.collectAsStateWithLifecycle()
    val models by vm.models.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val catName = (cats as? UiState.Success)?.value?.firstOrNull { it.id == catId }?.name

    Scaffold(topBar = { HamoTopBar(catName ?: "الشركات والماركات", onBack = { nav.popBackStack() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            SearchBox(text, { text = it }, "ابحث عن شركة أو ماركة...")
            StateContent(brands, "لا توجد شركات", "أضف شركات من لوحة الإدارة.", isEmpty = { it.isEmpty() }) { all ->
                val ms = (models as? UiState.Success)?.value ?: emptyList()
                val q = text.trim().lowercase()
                val rows = all.map { b ->
                    b to ms.count { it.brandId == b.id && (catId == 0L || it.categoryId == catId) }
                }.filter { (b, n) ->
                    (catId == 0L || n > 0) && (q.isEmpty() || b.name.lowercase().contains(q))
                }
                if (rows.isEmpty()) EmptyState("لا توجد شركات", "لا توجد شركات مطابقة.")
                else LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(14.dp, 6.dp, 14.dp, 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(rows, key = { it.first.id }) { (b, n) ->
                        Column(
                            Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White)
                                .border(1.dp, CardLine, RoundedCornerShape(16.dp))
                                .clickable { nav.navigate(Routes.modelsFor(b.id, catId)) }.padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BrandLogo(b.name, 66.dp)
                            Text(b.name.ltr(), color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                            Text("$n موديل", color = MutedText, fontSize = 11.sp)
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, unfocusedBorderColor = CardLine,
        ),
        modifier = Modifier.fillMaxWidth().padding(14.dp, 12.dp, 14.dp, 8.dp),
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

// ======================= تفاصيل الماكينة =======================

/** تبويبات مقسّمة بخلفية رمادية فاتحة، والمختار أزرق. */
@Composable
private fun Segmented(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2F7)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.forEachIndexed { i, l ->
            val on = i == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (on) BrandBlue else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) { Text(l, color = if (on) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1) }
        }
    }
}

@Composable
private fun SpecRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(22.dp))
        Text(
            buildString { append(label); append(" : "); append(value) },
            Modifier.weight(1f), color = Color(0xFF1E293B), fontSize = 15.sp, lineHeight = 23.sp,
        )
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F4F8)))
}

@Composable
fun ModelDetailScreen(nav: NavController, id: Long) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "model_$id") { ModelDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var tab by rememberSaveable { mutableStateOf(0) }

    Scaffold(topBar = {
        HamoTopBar("تفاصيل الماكينة", onBack = { nav.popBackStack() }) {
            IconButton(onClick = { nav.navigate(Routes.modelEdit(id)) }) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "حذف") }
            IconButton(onClick = vm::toggleFavorite) {
                Icon(if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "مفضلة", tint = if (fav) Orange else Color.White)
            }
        }
    }) { pad ->
        StateContent(state, isEmpty = { it == null }, emptyTitle = "الموديل غير موجود") { d ->
            if (d == null) return@StateContent
            val documented = d.guides.any { it.level == Verification.DOCUMENTED }
            Column(
                Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(1.dp, CardLine, RoundedCornerShape(18.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(d.model.name.ltr(), color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 26.sp)
                            Text(d.brandName.orEmpty().ltr(), color = MutedText, fontSize = 13.sp)
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                d.categoryName?.let { Chip(it, OrangeSoft, Color(0xFF9A3412)) }
                                Chip(if (documented) "موثّق" else "راجع المصدر", OrangeSoft, Color(0xFF9A3412))
                            }
                        }
                        MachineIcon(d.brandName, d.model.name, size = 118.dp)
                    }
                    Segmented(listOf("معلومات عامة", "المواصفات", "الصور"), tab) { tab = it }
                    when (tab) {
                        0 -> Column {
                            SpecRow(Icons.Default.Category, "النوع", d.categoryName.orEmpty())
                            SpecRow(Icons.Default.Business, "الشركة", d.brandName.orEmpty().ltr())
                            SpecRow(Icons.Default.Memory, "وحدة التحكم", d.model.controlUnit)
                            SpecRow(Icons.Default.SettingsRemote, "الريموت", d.model.remoteInfo)
                            SpecRow(Icons.Default.Sensors, "الحساسات", d.model.sensorsInfo)
                        }
                        1 -> Column {
                            val specLines = d.model.specs.split(Regex("(?<=[.؛])\\s+|\\n")).map { it.trim() }.filter { it.isNotEmpty() }
                            if (specLines.isEmpty()) Text("لا توجد مواصفات مسجلة.", color = MutedText)
                            specLines.forEach { line ->
                                val (h, b) = splitStep(line)
                                SpecRow(Icons.Default.Bolt, h ?: "•", b)
                            }
                            SpecRow(Icons.Default.Info, "ملاحظات", d.model.notes)
                            SpecRow(Icons.Default.Shield, "المصدر", d.model.source)
                            if (d.parts.isNotEmpty()) {
                                SpecRow(Icons.Default.Handyman, "قطع الغيار", d.parts.joinToString("، ") { "${it.name} ${it.partNumber.ltr()}".trim() })
                            }
                        }
                        else -> MediaSection(OwnerType.MODEL, id)
                    }
                    Button(
                        onClick = { nav.navigate(Routes.modelGuides(id)) },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    ) { Text("البرمجة والتوصيلات", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
                    OutlinedButton(
                        onClick = { nav.navigate(Routes.modelGuides(id, "FAULTS")) },
                        modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Orange),
                    ) { Text("الأعطال والحلول (${d.faults.size})", color = Color(0xFFC2410C), fontWeight = FontWeight.Bold) }
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

// ======================= البرمجة / التوصيلات / الريموت لموديل =======================

private data class GuideTabDef(val key: String, val label: String, val title: String, val icon: ImageVector)

private val guideTabs = listOf(
    GuideTabDef(GuideType.REMOTE, "الريموت", "برمجة الريموت", Icons.Default.SettingsRemote),
    GuideTabDef(GuideType.WIRING, "التوصيلات", "التوصيلات", Icons.Default.Cable),
    GuideTabDef(GuideType.PROGRAMMING, "البرمجة", "البرمجة", Icons.Default.Tune),
    GuideTabDef(GuideType.INSTALL, "التركيب", "التركيب", Icons.Default.Construction),
    GuideTabDef("FAULTS", "الأعطال", "الأعطال والحلول", Icons.Default.WarningAmber),
)

@Composable
fun ModelGuidesScreen(nav: NavController, id: Long, startTab: String) {
    val container = LocalContainer.current
    val vm = hamoVm(key = "model_$id") { ModelDetailViewModel(container.repo, id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    var current by rememberSaveable { mutableStateOf(startTab) }
    val d = (state as? UiState.Success)?.value
    // التركيب يظهر فقط لو له أدلة
    val tabs = guideTabs.filter { it.key != GuideType.INSTALL || d?.guides?.any { g -> g.type == GuideType.INSTALL } == true }
    val sel = tabs.firstOrNull { it.key == current } ?: tabs.first { it.key == GuideType.PROGRAMMING }

    Scaffold(topBar = { HamoTopBar(sel.title, onBack = { nav.popBackStack() }, subtitle = d?.model?.name?.ltr()) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEach { t ->
                    val on = t.key == sel.key
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (on) BrandBlueSoft else Color.Transparent)
                            .border(1.dp, if (on) BrandBlue.copy(alpha = 0.35f) else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { current = t.key }.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(t.icon, null, tint = if (on) BrandBlue else MutedText, modifier = Modifier.size(24.dp))
                        Text(t.label, color = if (on) BrandBlue else MutedText, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
            StateContent(state, isEmpty = { it == null }, emptyTitle = "الموديل غير موجود") { det ->
                if (det == null) return@StateContent
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (sel.key == "FAULTS") {
                        if (det.faults.isEmpty()) Text("لا توجد أعطال مسجلة لهذا الموديل.", color = MutedText)
                        det.faults.forEach { f -> FaultRow(f, det.brandName, det.model.name) { nav.navigate(Routes.fault(f.id)) } }
                        TextButton(onClick = { nav.navigate(Routes.faultEdit(0, id)) }) { Text("إضافة عطل لهذا الموديل") }
                    } else {
                        val list = det.guides.filter { it.type == sel.key }
                        if (list.isEmpty()) {
                            EmptyState("لسه مفيش ${sel.label} لهذا الموديل", "تقدر تضيفها من لوحة الإدارة، أو تدوّر عليها من «بحث بالإنترنت».")
                        }
                        list.forEach { g -> ModelGuideSection(nav, g, "${det.brandName.orEmpty()} ${det.model.name}".trim()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelGuideSection(nav: NavController, g: TechnicalGuide, machine: String) {
    val container = LocalContainer.current
    val ctx = LocalContext.current
    val vm = hamoVm(key = "block_${g.id}") { GuideDetailViewModel(container.repo, g.id) }
    val state by vm.detail.collectAsStateWithLifecycle()
    val steps = (state as? UiState.Success)?.value?.full?.orderedSteps?.map { it.text } ?: emptyList()
    val style = guideStyle(g.type)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (g.type == GuideType.REMOTE) {
            // بطاقة رأس الريموت كما في التصميم
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                    .border(1.dp, CardLine, RoundedCornerShape(18.dp)).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(70.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF1F2937)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SettingsRemote, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Text(g.title, color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
                Text("اتبع الخطوات التالية لبرمجة الريموت", color = MutedText, fontSize = 13.sp)
            }
        } else {
            Text(g.title, color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, lineHeight = 24.sp,
                modifier = Modifier.clickable { nav.navigate(Routes.guide(g.id)) })
        }
        BeforeYouStart(g.summary, style)
        WarningBox(g.warnings)
        // في التوصيلات المخطط أولًا ثم شرح الأطراف
        if (g.type == GuideType.WIRING) {
            MediaSection(OwnerType.GUIDE, g.id, readOnly = true)
        }
        if (steps.isNotEmpty()) {
            TermsCard(steps.joinToString("\n") + "\n" + g.summary)
            StepsChecklist("block_${g.id}", steps, BrandBlue)
        }
        if (g.type != GuideType.WIRING) {
            MediaSection(OwnerType.GUIDE, g.id, readOnly = true)
        }
        if (g.type == GuideType.REMOTE) {
            Button(
                onClick = {
                    val q = Uri.encode("$machine remote programming")
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$q")))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text("عرض فيديو توضيحي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        if (g.source.isNotBlank()) Text("المصدر: ${g.source}", color = MutedText, fontSize = 11.sp, lineHeight = 17.sp)
        Box(Modifier.fillMaxWidth().height(1.dp).background(CardLine))
    }
}

// ======================= الأعطال والحلول =======================

@Composable
private fun FaultRow(f: Fault, brand: String?, model: String?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(1.dp, CardLine, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Orange), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.WarningAmber, null, tint = Color.White)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(f.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 22.sp)
            Text(listOf(brand?.ltr() ?: "عام", model?.ltr() ?: "جميع الموديلات").joinToString(" - "), color = MutedText, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF94A3B8))
    }
}

@Composable
fun FaultsListScreen(nav: NavController) {
    val container = LocalContainer.current
    val vm = hamoVm { FaultListViewModel(container.repo) }
    val faults by vm.faults.collectAsStateWithLifecycle()
    val names by vm.names.collectAsStateWithLifecycle()
    val n: NameMaps? = (names as? UiState.Success)?.value
    var query by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { HamoTopBar("الأعطال والحلول", onBack = { nav.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.faultEdit(0)) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("عطل جديد") },
                containerColor = Orange, contentColor = Color.White)
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            SearchBox(query, { query = it }, "ابحث عن عطل أو حل...")
            StateContent(faults, "لا توجد أعطال", "أضف أول عطل.", isEmpty = { it.isEmpty() }) { all ->
                val brandOf = { f: Fault -> n?.brandOfModel(f.modelId) }
                val brands = all.mapNotNull(brandOf).distinct().sorted()
                val q = query.trim().lowercase()
                val shown = all.filter { f ->
                    (brand == null || brandOf(f) == brand) &&
                        (q.isEmpty() || f.title.lowercase().contains(q) || f.description.lowercase().contains(q) || f.altKeywords.lowercase().contains(q))
                }
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = brand == null, onClick = { brand = null }, label = { Text("جميع الأعطال") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandBlue, selectedLabelColor = Color.White))
                    brands.forEach { b ->
                        FilterChip(selected = brand == b, onClick = { brand = if (brand == b) null else b }, label = { Text(b.ltr()) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandBlue, selectedLabelColor = Color.White))
                    }
                }
                LazyColumn(contentPadding = PaddingValues(14.dp, 8.dp, 14.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (shown.isEmpty()) item { Text("مفيش نتائج.", color = MutedText) }
                    items(shown, key = { it.id }) { f ->
                        FaultRow(f, brandOf(f), n?.modelName(f.modelId)) { nav.navigate(Routes.fault(f.id)) }
                    }
                }
            }
        }
    }
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
