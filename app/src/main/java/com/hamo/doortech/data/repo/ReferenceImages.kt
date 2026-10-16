package com.hamo.doortech.data.repo

import android.content.Context
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import kotlinx.coroutines.flow.first

/** يرفق صور مخططات الكتالوج (من assets/ref) بالأدلة المقابلة، مرة واحدة لكل دليل. */
object ReferenceImages {
    private val items = listOf(
        Triple("Nice RD400 — مخطط التوصيل (الشكل 7)", "ref/nice_rd400_wiring.jpg", "صفحة التوصيل — الدليل الأصلي"),
    )

    suspend fun attach(context: Context, repo: AppRepository) {
        val guides = repo.guides.first()
        for ((title, asset, caption) in items) {
            val g = guides.firstOrNull { it.title == title } ?: continue
            if (repo.mediaOnce(OwnerType.GUIDE, g.id).any { it.title == caption }) continue
            val name = try {
                context.assets.open(asset).use { repo.mediaStore.copyFromStream(it, asset.substringAfterLast('/')) }
            } catch (e: Exception) { null } ?: continue
            repo.db.mediaDao().insert(
                MediaAttachment(ownerType = OwnerType.GUIDE, ownerId = g.id, kind = MediaKind.IMAGE, path = name, title = caption, role = MediaRole.NONE)
            )
        }
    }

    private val catalogs = listOf(
        "BX-74" to "https://doorentrydirect.com/media/product_attach_files/Installation_manual_119RIBX041.pdf",
        "BX-78" to "https://doorentrydirect.com/media/product_attach_files/Installation_manual_119RIBX041.pdf",
        "844 ER" to "https://cdn.shopify.com/s/files/1/0671/3786/3920/files/844ER.pdf",
        "RUN1800" to "https://manualmachine.com/nice/run/2591733-user-manual/",
        "RUN2500" to "https://manualmachine.com/nice/run/2591733-user-manual/",
        "BM30" to "https://ismartgate.com/manuals/Roger_BM30_manual.pdf",
        "لوحة MC50SC" to "https://ismartgate.com/manuals/Motorline_MC50SC_manual.pdf",
        "LIVI" to "https://manualslib.mx/manual/369786/Dea-Livi.html?page=10",
        "ARES ULTRA" to "https://pdf.archiexpo.com/pdf/bft/ares-ultra-bt/46-267146.html",
        "DEIMOS ULTRA" to "https://pdf.archiexpo.com/ja/pdf-en/bft/deimos-ultra-bt-a/46-249552.html",
    )

    /** يرفق رابط الكتالوج الأصلي (PDF/صفحة الدليل) بكل موديل لفتحه من الإنترنت. */
    suspend fun attachCatalogLinks(repo: AppRepository) {
        val models = repo.modelsOnce()
        for ((key, url) in catalogs) {
            val m = models.firstOrNull { it.name.startsWith(key) || it.name.contains(key) } ?: continue
            if (repo.mediaOnce(OwnerType.MODEL, m.id).any { it.path == url }) continue
            repo.addVideoLink(OwnerType.MODEL, m.id, url, "الكتالوج الأصلي (اضغط لفتحه)")
        }
    }
}
