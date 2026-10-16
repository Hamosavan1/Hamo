package com.hamo.doortech.data.repo

import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.db.Verification
import kotlinx.coroutines.flow.first

/** Nice RD400 (ROAD 400): صفحة التوصيل الأصلية من الدليل الذي رفعه المستخدم، مستقلة عن ReferenceData. */
object ReferenceExtra {
    private const val SRC = "صفحة من دليل Nice RD400 (الشكل 7 وخطوات التركيب) + رسم منتدى لـ Nice ROAD 400 للتحقق من أرقام الأطراف"
    private const val GUIDE_TITLE = "Nice RD400 — مخطط التوصيل (الشكل 7)"
    private const val WARN = "افصل الكهرباء تمامًا قبل فتح الغطاء. لا تحرّك مفتاح الاتجاه والمحرك يعمل. لا تُلغِ عمل الفوتوسيل أو أجهزة الأمان."

    suspend fun populate(repo: AppRepository) {
        val catId = repo.categoriesOnce().firstOrNull { it.name == ReferenceData.CATEGORY }?.id
        val niceId = repo.brandsOnce().firstOrNull { it.name == "Nice" }?.id
            ?: ((repo.saveBrand(Brand(name = "Nice", country = "إيطاليا")) as? Outcome.Ok)?.id ?: return)
        val modelName = "RD400 (ROAD 400)"
        val modelId = repo.modelsOnce().firstOrNull { it.brandId == niceId && it.name == modelName }?.id
            ?: ((repo.saveModel(MachineModel(
                brandId = niceId, categoryId = catId, name = modelName,
                controlUnit = "لوحة مدمجة بأطراف 1–8 + LEDs L1..L4 وأزرار Open ▲ / Stop-Set / Close ▼",
                specs = "فيوز اللوحة 2A T • مفتاح اختيار اتجاه الفتح (Selector) • باقي المواصفات غير متوفرة بعد",
                remoteInfo = "هوائي AERIAL على اللوحة؛ LED L4 للراديو. خطوات الحفظ في الفصل 7 من الدليل (لم تصلني بعد).",
                sensorsInfo = "فوتوسيل RX/TX على Photo وPhoto Test • مدخل STOP يقبل NO أو NC أو 8.2kΩ • مدخل Sb-S (خطوة بخطوة)",
                notes = "الاسم على الملف Nice Rd400 وعلى رسم المنتدى NICE ROAD 400.", source = SRC,
            )) as? Outcome.Ok)?.id ?: return)
        if (repo.guides.first().none { it.title == GUIDE_TITLE }) {
            repo.saveGuide(GuideDraft(
                TechnicalGuide(
                    type = GuideType.WIRING, title = GUIDE_TITLE, categoryId = catId, modelId = modelId,
                    summary = "صورة صفحة التوصيل من الدليل الأصلي مع الترجمة العربية.", warnings = WARN,
                    source = SRC, level = Verification.DOCUMENTED,
                ),
                listOf(
                    "فتح الغطاء: فك البرغي وارفع الغطاء.",
                    "مرّر كابل الكهرباء من الفتحة المخصصة (اترك 20–30 سم حرة) ووصّله بطرف التغذية.",
                    "مرّر كابلات الملحقات (الجديدة أو الموجودة) بنفس الطريقة (20–30 سم حرة) ووصّلها بأطرافها (الشكل 7).",
                    "قبل إغلاق الغطاء: برمج النظام كما تريد (الفصل 7 من الدليل).",
                    "أغلق الغطاء بالبرغي.",
                    "أسماء الأطراف على اللوحة: Flash • Photo Test • Photo • GND • Stop • Sb-S • 24V • Aerial.",
                    "ترقيم الأطراف (مستنتج من ترتيب الملصقات ورسم المنتدى لـ ROAD 400): 1 = +24V • 2 = Sb-S (خطوة بخطوة) • 3 = STOP • 4 = GND • 5 = Photo • 6 = Photo Test • 7-8 = Flash. تحقق من الملصق على لوحتك قبل التوصيل.",
                    "الفوتوسيل: المستقبل RX له أطراف 1–5 والمرسل TX أطراف 1–2؛ التغذية من 24V وGND وخرج RX على Photo (حسب رسم المنتدى: 1 = 24V، 4 = GND، 5 = Photo).",
                    "مدخل STOP يقبل تلامس NO أو NC أو مقاومة 8.2kΩ.",
                    "الهوائي AERIAL على أطراف الهوائي بجانب الفلاشر.",
                    "اتجاه الفتح: إن كانت البوابة تفتح لليسار حرّك المفتاح (Selector) لليسار (الشكل 8a)، وإن كانت لليمين حرّكه لليمين (الشكل 8b). تحذير: لا تبدّل المفتاح والمحرك يتحرك.",
                    "ملاحظة: صفحات برمجة الأزرار (L1–L4) وحفظ الريموت لهذا الموديل لم تصلني بعد.",
                ),
            ))
        }
    }
}
