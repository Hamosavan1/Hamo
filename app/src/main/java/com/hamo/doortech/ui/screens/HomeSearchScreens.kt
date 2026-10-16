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

    Scaffold(topBar = { HamoTopBar(if (diagnose) "تشخيص الأعطال" else "البحث الشامل", onBack = if (diagnose) ({ nav.popBackStack() }) else null) }) { pad ->
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
        SearchType.BRAND -> nav.navigate(Routes.modelsFor(r.doc.id, 0))
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
