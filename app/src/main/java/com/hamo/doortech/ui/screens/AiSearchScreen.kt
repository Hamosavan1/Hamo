package com.hamo.doortech.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hamo.doortech.data.ai.AiOutcome
import com.hamo.doortech.ui.AiSearchViewModel
import com.hamo.doortech.ui.UiState
import com.hamo.doortech.ui.components.DropdownField
import com.hamo.doortech.ui.components.FormField
import com.hamo.doortech.ui.components.HamoTopBar
import com.hamo.doortech.ui.components.LoadingBox
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.PrimaryButton
import com.hamo.doortech.ui.components.SecondaryButton
import com.hamo.doortech.ui.components.SectionCard
import com.hamo.doortech.ui.components.WarningBox
import com.hamo.doortech.ui.components.hamoVm

@Composable
private fun Lines(title: String, items: List<String>) {
    if (items.isEmpty()) return
    SectionCard(title) {
        items.forEachIndexed { i, t -> Text("${i + 1}. $t", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
fun AiSearchScreen(nav: NavController, initialQuery: String) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val vm = hamoVm(key = "ai_$initialQuery") { com.hamo.doortech.ui.AiSearchViewModel(container, initialQuery) }
    val cats by vm.categories.collectAsStateWithLifecycle()
    val outcome = vm.outcome

    androidx.compose.material3.Scaffold(topBar = { HamoTopBar("بحث بالإنترنت (ذكاء اصطناعي)", onBack = { nav.popBackStack() }) }) { pad ->
        ScrollColumn(pad) {
            WarningBox("هذه الميزة تحتاج إنترنت ومفتاح Claude API خاصًا بك. نتائج الذكاء الاصطناعي ملخصات من صفحات الإنترنت وقد تحتوي أخطاء؛ لا تُعامل كدليل الشركة. راجع الدليل الأصلي قبل أي ضبط أو توصيل. باقي التطبيق يعمل دون إنترنت.")

            SectionCard("مفتاح API") {
                OutlinedTextField(
                    value = vm.apiKey, onValueChange = { vm.apiKey = it },
                    label = { Text("مفتاح Claude API (sk-ant-...)") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                )
                Text("يُحفظ على جهازك فقط، ولا يُضاف إلى النسخ الاحتياطي. استهلاك البحث يُحسب على حسابك لدى Anthropic.", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("حفظ المفتاح", { vm.saveKey(); messenger.show("تم حفظ المفتاح") })
                    SecondaryButton("مسح المفتاح", { vm.clearKey(); messenger.show("تم مسح المفتاح") })
                }
            }

            SectionCard("ماذا تبحث عن؟") {
                FormField("الشركة والموديل (مثال: CAME BX-243)", vm.query, { vm.query = it }, placeholder = "اسم الشركة + الموديل أو اللوحة")
                PrimaryButton(if (vm.loading) "جارٍ البحث..." else "ابحث في الإنترنت", { vm.search() }, Modifier.fillMaxWidth(), enabled = !vm.loading && vm.query.isNotBlank())
            }

            if (vm.loading) LoadingBox()

            when (outcome) {
                null -> {}
                is AiOutcome.Failure -> WarningBox(outcome.message)
                is AiOutcome.Success -> {
                    val r = outcome.result
                    if (!r.found) {
                        WarningBox("لم يتم العثور على دليل موثوق لهذه المكينة. جرّب كتابة الاسم بشكل أدق (الشركة + رقم الموديل أو اللوحة).")
                        if (r.notes.isNotBlank()) Text(r.notes, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        SectionCard("${r.brand} ${r.model}") {
                            if (r.controlUnit.isNotBlank()) Text("اللوحة: ${r.controlUnit}")
                            if (r.specs.isNotBlank()) Text(r.specs, style = MaterialTheme.typography.bodyMedium)
                        }
                        Lines("الحساسات", r.sensors)
                        Lines("التوصيلات", r.wiring)
                        Lines("برمجة المكينة", r.programming)
                        Lines("برمجة الريموت", r.remote)
                        if (r.faults.isNotEmpty()) {
                            SectionCard("أكواد الأعطال") {
                                r.faults.forEach { f -> Text("• ${listOf(f.code, f.title, f.meaning).filter { it.isNotBlank() }.joinToString(" — ")}", style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                        if (r.warnings.isNotBlank()) WarningBox(r.warnings)
                        if (r.notes.isNotBlank()) SectionCard("ملاحظات (ما لم يُعثر عليه)") { Text(r.notes, style = MaterialTheme.typography.bodyMedium) }
                        if (outcome.sources.isNotEmpty()) {
                            SectionCard("المصادر") { outcome.sources.take(8).forEach { Text(it, style = MaterialTheme.typography.bodyMedium) } }
                        }
                        SectionCard("حفظ في قاعدة بياناتي") {
                            Text("يُحفظ كشركة وموديل وأدلة بوسم «غير موثّق» ومعه روابط المصادر. تأكد من صحته ثم عدّله حسب الدليل الأصلي.", style = MaterialTheme.typography.bodyMedium)
                            val list = (cats as? UiState.Success)?.value.orEmpty()
                            DropdownField(
                                "نوع الباب",
                                listOf<Pair<Long?, String>>(null to "غير محدد") + list.map { it.id as Long? to it.name },
                                vm.categoryId, { vm.categoryId = it },
                            )
                            PrimaryButton(if (vm.saving) "جارٍ الحفظ..." else "حفظ", {
                                vm.save { res -> messenger.show(res.messageOr("تم الحفظ")) }
                            }, Modifier.fillMaxWidth(), enabled = !vm.saving)
                        }
                    }
                }
            }
        }
    }
}
