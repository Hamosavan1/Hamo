package com.hamo.doortech.data.repo

import android.content.Context
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.db.Verification
import kotlinx.coroutines.flow.first

/** صورة من صفحة كتالوج أصلي: مسار داخل assets/cat + عنوان. */
data class CatImage(val asset: String, val caption: String)

/** دليل (توصيلات/برمجة/ريموت) لموديل: نص عربي مترجم من الكتالوج + صور الصفحات الأصلية. */
data class CatGuide(
    val type: String,
    val title: String,
    val summary: String,
    val warnings: String,
    val steps: List<String>,
    val images: List<CatImage> = emptyList(),
)

/**
 * موديل من كتالوج رسمي رفعه المستخدم. [match] أسماء/أجزاء أسماء لموديلات موجودة مسبقًا نربط بها
 * (لتفادي التكرار)، وإلا يُنشأ الموديل. كل المحتوى مترجم من الكتالوج المذكور في [source].
 */
data class CatModel(
    val brand: String,
    val country: String,
    val category: String,
    val name: String,
    val match: List<String>,
    val controlUnit: String,
    val specs: String,
    val remoteInfo: String,
    val sensorsInfo: String,
    val source: String,
    val guides: List<CatGuide>,
    val notes: String = "",
)

object ReferenceCatalogs {
    /** عناوين أدلة قديمة (نسخ سابقة) تُحذف عند الترقية لأن النسخة الجديدة بديلة لها. */
    private val obsoleteGuideTitles = listOf("Nice RD400 — مخطط التوصيل (الشكل 7)")

    private fun all(): List<CatModel> =
        CatalogsNice.models() + CatalogsBft.models() + CatalogsLife.models() + CatalogsOther.models()

    suspend fun populate(context: Context, repo: AppRepository) {
        for (t in obsoleteGuideTitles) {
            repo.guides.first().firstOrNull { it.title == t }?.let { repo.deleteGuide(it.id) }
        }
        for (cm in all()) {
            try { load(context, repo, cm) } catch (e: Exception) { /* موديل واحد لا يوقف الباقي */ }
        }
    }

    private suspend fun load(context: Context, repo: AppRepository, cm: CatModel) {
        val catId = repo.categoriesOnce().firstOrNull { it.name.contains(cm.category) }?.id
        val brands = repo.brandsOnce()
        val brandId = brands.firstOrNull { it.name.equals(cm.brand, true) }?.id
            ?: ((repo.saveBrand(Brand(name = cm.brand, country = cm.country)) as? Outcome.Ok)?.id ?: return)
        val models = repo.modelsOnce()
        val existing = models.firstOrNull { m ->
            m.brandId == brandId && (m.name == cm.name || cm.match.any { k -> m.name.contains(k, true) })
        }
        val modelId = existing?.id ?: ((repo.saveModel(
            MachineModel(
                brandId = brandId, categoryId = catId, name = cm.name, controlUnit = cm.controlUnit,
                specs = cm.specs, remoteInfo = cm.remoteInfo, sensorsInfo = cm.sensorsInfo,
                source = cm.source, notes = cm.notes,
            )
        ) as? Outcome.Ok)?.id ?: return)

        for (g in cm.guides) {
            val have = repo.guides.first().firstOrNull { it.title == g.title && it.modelId == modelId }
            val guideId = have?.id ?: ((repo.saveGuide(
                GuideDraft(
                    TechnicalGuide(
                        type = g.type, title = g.title, categoryId = catId, modelId = modelId,
                        summary = g.summary, warnings = g.warnings, source = cm.source,
                        level = Verification.DOCUMENTED,
                    ),
                    g.steps,
                )
            ) as? Outcome.Ok)?.id ?: continue)
            val media = repo.mediaOnce(OwnerType.GUIDE, guideId)
            for (img in g.images) {
                if (media.any { it.title == img.caption }) continue
                val stored = repo.mediaStore.copyFromAssets(img.asset, img.asset.substringAfterLast('/')) ?: continue
                repo.db.mediaDao().insert(
                    MediaAttachment(ownerType = OwnerType.GUIDE, ownerId = guideId, kind = MediaKind.IMAGE, path = stored, title = img.caption, role = MediaRole.NONE)
                )
            }
        }
    }
}

/** اختصارات لبناء أدلة بصيغة موحّدة. */
internal fun wiring(title: String, summary: String, warnings: String, steps: List<String>, vararg images: CatImage) =
    CatGuide(GuideType.WIRING, title, summary, warnings, steps, images.toList())

internal fun programming(title: String, summary: String, warnings: String, steps: List<String>, vararg images: CatImage) =
    CatGuide(GuideType.PROGRAMMING, title, summary, warnings, steps, images.toList())

internal fun remote(title: String, summary: String, warnings: String, steps: List<String>, vararg images: CatImage) =
    CatGuide(GuideType.REMOTE, title, summary, warnings, steps, images.toList())

internal fun img(file: String, caption: String) = CatImage("cat/$file", caption)

internal const val SAFETY_WARN = "افصل الكهرباء وبطارية الاحتياط (إن وُجدت) قبل أي توصيل. لا تُلغِ ولا تُعطّل عمل الفوتوسيل أو حافة الأمان أو أي جهاز أمان."
