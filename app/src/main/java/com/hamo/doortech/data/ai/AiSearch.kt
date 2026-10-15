package com.hamo.doortech.data.ai

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.data.repo.AppRepository
import com.hamo.doortech.data.repo.CodeDraft
import com.hamo.doortech.data.repo.FaultDraft
import com.hamo.doortech.data.repo.GuideDraft
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.data.repo.SolutionDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AiFault(val title: String, val code: String, val meaning: String)

data class AiResult(
    val found: Boolean,
    val brand: String,
    val model: String,
    val controlUnit: String,
    val specs: String,
    val sensors: List<String>,
    val wiring: List<String>,
    val programming: List<String>,
    val remote: List<String>,
    val faults: List<AiFault>,
    val warnings: String,
    val notes: String,
)

sealed interface AiOutcome {
    data class Success(val result: AiResult, val sources: List<String>) : AiOutcome
    data class Failure(val message: String) : AiOutcome
}

/**
 * بحث على الإنترنت عبر Claude API (أداة web_search). يتطلب مفتاح API يدخله المستخدم ويُحفظ على الجهاز فقط.
 * النتائج غير موثّقة: تُعرض كاقتراح ولا تُحفظ إلا بموافقة المستخدم، ومعلّمة «غير موثّق».
 */
class AiSearchClient(private val model: String = DEFAULT_MODEL) {

    suspend fun search(apiKey: String, query: String): AiOutcome = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext AiOutcome.Failure("أدخل مفتاح API أولًا")
        if (query.isBlank()) return@withContext AiOutcome.Failure("اكتب اسم الشركة أو الموديل")
        try {
            val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 150_000
                doOutput = true
                setRequestProperty("content-type", "application/json")
                setRequestProperty("x-api-key", apiKey.trim())
                setRequestProperty("anthropic-version", "2023-06-01")
            }
            conn.outputStream.use { it.write(buildRequest(model, query).toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code in 200..299) parseResponse(body) else AiOutcome.Failure(httpError(code, body))
        } catch (e: java.net.UnknownHostException) {
            AiOutcome.Failure("لا يوجد اتصال بالإنترنت")
        } catch (e: java.net.SocketTimeoutException) {
            AiOutcome.Failure("انتهت مهلة الاتصال، حاول مرة أخرى")
        } catch (e: Exception) {
            AiOutcome.Failure("تعذّر البحث: ${e.message ?: "خطأ غير معروف"}")
        }
    }

    companion object {
        const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        const val DEFAULT_MODEL = "claude-sonnet-5-5"

        private const val SYSTEM = "أنت مساعد لفنيي صيانة الأبواب والبوابات الأوتوماتيك. ابحث في الإنترنت عن دليل الشركة المصنعة " +
            "للمكينة المطلوبة (يفضّل الدليل الرسمي أو كتالوج الشركة). أجب بكائن JSON واحد فقط بلا أي نص خارجه، وبالعربية، " +
            "بالمفاتيح: found (true/false), brand, model, controlUnit, specs (نص), sensors (قائمة نصوص), " +
            "wiring (قائمة خطوات أو أطراف التوصيل), programming (قائمة خطوات وقيم البرمجة), remote (قائمة خطوات حفظ وحذف الريموت), " +
            "faults (قائمة كائنات title وcode وmeaning), warnings (نص), notes (نص: ما لم تجده أو لم تتأكد منه). " +
            "قواعد صارمة: اكتب فقط ما وجدته فعلًا في المصادر؛ لا تخترع أكواد أخطاء ولا قيم برمجة ولا أرقام أطراف؛ " +
            "ما لم تجده اتركه قائمة فارغة أو اكتب «غير مذكور»؛ إن لم تجد المكينة اجعل found=false. " +
            "لا تقترح تعطيل حساسات الأمان أبدًا. استخدم أسماء الأطراف والمعاملات كما هي بالإنجليزية داخل النص العربي."

        fun buildRequest(model: String, query: String): String {
            val root = JsonObject()
            root.addProperty("model", model)
            root.addProperty("max_tokens", 4096)
            root.addProperty("system", SYSTEM)
            val tool = JsonObject().apply {
                addProperty("type", "web_search_20250305")
                addProperty("name", "web_search")
                addProperty("max_uses", 6)
            }
            root.add("tools", JsonArray().apply { add(tool) })
            val msg = JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", "المكينة / الشركة المطلوبة: ${query.trim()}\nأعطني: المواصفات، الحساسات، التوصيلات، برمجة المكينة، برمجة الريموت، وأكواد الأعطال إن وُجدت.")
            }
            root.add("messages", JsonArray().apply { add(msg) })
            return root.toString()
        }

        fun httpError(code: Int, body: String): String {
            val apiMsg = try {
                JsonParser.parseString(body).asJsonObject.getAsJsonObject("error")?.get("message")?.asString
            } catch (e: Exception) { null }
            return when (code) {
                401 -> "مفتاح API غير صالح"
                403 -> "المفتاح لا يملك صلاحية لهذه العملية"
                429 -> "تم تجاوز حد الطلبات، حاول لاحقًا"
                529, 503 -> "الخدمة مشغولة، حاول بعد قليل"
                else -> "خطأ من الخدمة ($code)" + (apiMsg?.let { ": $it" } ?: "")
            }
        }

        /** يحوّل رد API إلى نتيجة. دالة نقية قابلة للاختبار. */
        fun parseResponse(body: String): AiOutcome {
            val root = try { JsonParser.parseString(body).asJsonObject } catch (e: Exception) {
                return AiOutcome.Failure("رد غير مفهوم من الخدمة")
            }
            val content = root.getAsJsonArray("content") ?: return AiOutcome.Failure("الرد فارغ")
            val text = StringBuilder()
            val sources = LinkedHashSet<String>()
            for (el in content) {
                val b = el.asJsonObjectOrNull() ?: continue
                when (b.str("type")) {
                    "text" -> {
                        text.append(b.str("text"))
                        b.getAsJsonArray("citations")?.forEach { c -> c.asJsonObjectOrNull()?.str("url")?.takeIf { it.isNotBlank() }?.let(sources::add) }
                    }
                    "web_search_tool_result" -> {
                        val c = b.get("content")
                        if (c != null && c.isJsonArray) c.asJsonArray.forEach { r -> r.asJsonObjectOrNull()?.str("url")?.takeIf { it.isNotBlank() }?.let(sources::add) }
                    }
                }
            }
            val t = text.toString()
            val start = t.indexOf('{'); val end = t.lastIndexOf('}')
            if (start < 0 || end <= start) return AiOutcome.Failure("لم يرجع البحث بيانات منظمة، أعد المحاولة بكلمات أدق")
            val obj = try { JsonParser.parseString(t.substring(start, end + 1)).asJsonObject } catch (e: Exception) {
                return AiOutcome.Failure("تعذّر قراءة نتيجة البحث، أعد المحاولة")
            }
            val faults = obj.getAsJsonArray("faults")?.mapNotNull { f ->
                f.asJsonObjectOrNull()?.let { AiFault(it.str("title"), it.str("code"), it.str("meaning")) }
                    ?.takeIf { it.title.isNotBlank() || it.code.isNotBlank() }
            } ?: emptyList()
            val result = AiResult(
                found = obj.get("found")?.let { it.isJsonPrimitive && it.asBoolean } ?: false,
                brand = obj.str("brand"), model = obj.str("model"), controlUnit = obj.str("controlUnit"),
                specs = obj.str("specs"), sensors = obj.list("sensors"), wiring = obj.list("wiring"),
                programming = obj.list("programming"), remote = obj.list("remote"), faults = faults,
                warnings = obj.str("warnings"), notes = obj.str("notes"),
            )
            return AiOutcome.Success(result, sources.toList())
        }

        private fun JsonElement.asJsonObjectOrNull(): JsonObject? = if (isJsonObject) asJsonObject else null
        private fun JsonObject.str(k: String): String = get(k)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()
        private fun JsonObject.list(k: String): List<String> {
            val e = get(k) ?: return emptyList()
            return when {
                e.isJsonArray -> e.asJsonArray.mapNotNull { x -> (if (x.isJsonPrimitive) x.asString else x.toString()).trim().takeIf { it.isNotEmpty() } }
                e.isJsonPrimitive -> e.asString.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                else -> emptyList()
            }
        }
    }
}

/** يحفظ نتيجة البحث في قاعدة البيانات بعد موافقة المستخدم، معلّمة «غير موثّق». */
object AiImporter {
    private const val NOTE = "ملخص ذكاء اصطناعي من الإنترنت — غير موثّق؛ راجع دليل الشركة الأصلي قبل الاعتماد عليه."

    suspend fun save(repo: AppRepository, r: AiResult, sources: List<String>, categoryId: Long?): Outcome {
        if (!r.found) return Outcome.Fail("لم يتم العثور على المكينة، لا يوجد ما يُحفظ")
        val brandName = r.brand.trim().ifBlank { return Outcome.Fail("اسم الشركة غير معروف في النتيجة") }
        val modelName = r.model.trim().ifBlank { return Outcome.Fail("اسم الموديل غير معروف في النتيجة") }
        val src = "بحث بالذكاء الاصطناعي" + if (sources.isNotEmpty()) " — " + sources.take(5).joinToString(" , ") else ""

        val brandId = repo.brandsOnce().firstOrNull { it.name.equals(brandName, true) }?.id
            ?: ((repo.saveBrand(Brand(name = brandName)) as? Outcome.Ok)?.id ?: return Outcome.Fail("تعذّر حفظ الشركة"))
        val modelId = repo.modelsOnce().firstOrNull { it.brandId == brandId && it.name.equals(modelName, true) }?.id
            ?: run {
                val res = repo.saveModel(MachineModel(
                    brandId = brandId, categoryId = categoryId, name = modelName, controlUnit = r.controlUnit,
                    specs = r.specs, sensorsInfo = r.sensors.joinToString("\n"),
                    remoteInfo = r.remote.joinToString("\n"), notes = (r.notes + "\n" + NOTE).trim(), source = src,
                ))
                (res as? Outcome.Ok)?.id ?: return res
            }

        val existing = repo.guides.first().map { it.title }.toSet()
        suspend fun guide(type: String, label: String, steps: List<String>) {
            if (steps.isEmpty()) return
            val title = "$brandName $modelName — $label"
            if (title in existing) return
            repo.saveGuide(GuideDraft(
                TechnicalGuide(type = type, title = title, categoryId = categoryId, modelId = modelId,
                    summary = NOTE, warnings = r.warnings, source = src, level = Verification.UNVERIFIED),
                steps,
            ))
        }
        guide(GuideType.WIRING, "التوصيلات والحساسات", r.wiring + r.sensors.map { "حساس: $it" })
        guide(GuideType.PROGRAMMING, "برمجة المكينة", r.programming)
        guide(GuideType.REMOTE, "برمجة الريموت", r.remote)

        if (r.faults.isNotEmpty()) {
            val title = "$brandName $modelName — أكواد الأعطال"
            if (repo.faults.first().none { it.title == title }) {
                repo.saveFault(FaultDraft(
                    fault = Fault(title = title, categoryId = categoryId, modelId = modelId, description = NOTE,
                        warnings = r.warnings, source = src, level = Verification.UNVERIFIED),
                    codes = r.faults.filter { it.code.isNotBlank() }.map { CodeDraft(it.code, it.meaning.ifBlank { it.title }) },
                    solutions = listOf(SolutionDraft("راجع دليل الشركة", source = src, level = Verification.UNVERIFIED,
                        steps = r.faults.map { f -> listOf(f.code, f.title, f.meaning).filter { it.isNotBlank() }.joinToString(": ") }.ifEmpty { listOf("راجع الدليل") })),
                ))
            }
        }
        return Outcome.Ok(modelId, "تم الحفظ — العناصر معلّمة «غير موثّق»")
    }
}
