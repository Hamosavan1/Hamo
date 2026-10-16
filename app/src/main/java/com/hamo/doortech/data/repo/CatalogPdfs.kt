package com.hamo.doortech.data.repo

import android.content.Context
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import kotlinx.coroutines.flow.first

/** ملف PDF أصلي مرفق مع التطبيق (assets/cat/pdf) وعنوانه كما يظهر في المرفقات. */
data class CatPdf(val file: String, val title: String)

/** ربط ملفات الكتالوجات الأصلية (PDF كاملة) بالمكينة: تُرفق في صفحتي التوصيل والبرمجة. */
data class PdfLink(val brand: String, val keys: List<String>, val pdfs: List<CatPdf>)

object CatalogPdfs {
    private fun p(file: String, title: String) = CatPdf("cat/pdf/$file", "📄 $title (PDF كامل — الأصل)")

    val links: List<PdfLink> = listOf(
        PdfLink("Nice", listOf("RD400"), listOf(p("nice_rd400.pdf", "Nice RD400 — دليل التركيب"))),
        PdfLink("Nice", listOf("RUN1500"), listOf(p("nice_run1500.pdf", "Nice RUN1500 — دليل التركيب"))),
        PdfLink("Nice", listOf("RUN1800", "RUN2500"), listOf(p("nice_run1800.pdf", "Nice RUN1800/RUN2500 — دليل التركيب (212 صفحة بعدة لغات)"))),
        PdfLink("Nice", listOf("ROBO600", "ROBO 600"), listOf(p("nice_robo600.pdf", "Nice ROBO — دليل التركيب"))),
        PdfLink("Nice", listOf("ROBUS"), listOf(
            p("nice_rb500hs.pdf", "Nice RB500HS/RB600/RB1000 — دليل التركيب"),
            p("nice_robus_quick.pdf", "Nice ROBUS 400/600/1000 — ورقة التوصيلات السريعة"),
        )),
        PdfLink("Nice", listOf("ROX600", "ROX1000"), listOf(p("nice_rox_roa40_sheet.pdf", "Nice ROX600/1000 + ROA40 — ورقة الموزّع المجري"))),
        PdfLink("Beninca", listOf("BULL5", "BULL 5"), listOf(p("beninca_bull5.pdf", "Beninca CP.BULL5 ARC — دليل التركيب"))),
        PdfLink("Beninca", listOf("BULL8", "BULL 8"), listOf(p("beninca_bull8.pdf", "Beninca CP.BULL8 OM — دليل التركيب"))),
        PdfLink("Beninca", listOf("KS4", "MS4"), listOf(p("beninca_ks4.pdf", "Beninca KS4 / CP.MS4 — دليل التركيب (مجري)"))),
        PdfLink("BFT", listOf("DEIMOS BT A400", "DEIMOS BT A 400", "DEIMOS BT A600"), listOf(p("bft_deimos_bt.pdf", "BFT DEIMOS BT A400/A600 — دليل التركيب"))),
        PdfLink("BFT", listOf("DEIMOS BT KIT UL"), listOf(p("bft_deimos_kit_ul.pdf", "BFT DEIMOS BT KIT UL — دليل مرجعي سريع"))),
        PdfLink("BFT", listOf("SHYRA"), listOf(p("bft_shyra.pdf", "BFT SHYRA AC SL — دليل التركيب"))),
        PdfLink("BFT", listOf("ALPHA"), listOf(p("bft_alpha.pdf", "BFT ALPHA / ALPHA BOM — دليل اللوحة"))),
        PdfLink("LIFE", listOf("RG1R"), listOf(p("life_rg1r.pdf", "LIFE RG1R DL — دليل التركيب"))),
        PdfLink("LIFE", listOf("GE UNI"), listOf(
            p("life_ge_uni.pdf", "LIFE GE UNI R DL / RS DL — دليل التركيب"),
            p("life_ge_uni24r_hr.pdf", "LIFE GE UNI24R DL — دليل التركيب (كرواتي)"),
        )),
        PdfLink("LIFE", listOf("ACER"), listOf(p("life_acer.pdf", "LIFE ACER R DL + RG1R DL2 — دليل التركيب"))),
        PdfLink("CAME", listOf("BKS"), listOf(p("came_bks.pdf", "CAME BKS — دليل التركيب"))),
        PdfLink("CAME", listOf("V6000"), listOf(p("came_v6000.pdf", "CAME V6000 — دليل التركيب والتشغيل"))),
        PdfLink("CAME", listOf("FROG"), listOf(p("came_frog.pdf", "CAME FROG — دليل التركيب"))),
        PdfLink("VDS", listOf("EURO230"), listOf(
            p("euro230m1_a.pdf", "VDS EURO230M1 — دليل التعليمات (نسخة 1)"),
            p("euro230m1_b.pdf", "VDS EURO230M1 — دليل التعليمات (نسخة 2)"),
        )),
        PdfLink("Somfy", listOf("DEXXO PRO 800 RTS", "DEXXO PRO 1000 RTS"), listOf(
            p("somfy_dexxopro_rts_manual.pdf", "Somfy Dexxo Pro — دليل التركيب"),
            p("somfy_dexxopro_commissioning.pdf", "Somfy Dexxo Pro — ورقة التشغيل والمعاملات"),
            p("somfy_dexxo_quick_setup.pdf", "Somfy Dexxo Pro / Compact — دليل الإعداد السريع"),
        )),
        PdfLink("Somfy", listOf("DEXXO PRO 800 IO", "DEXXO PRO 1000 IO"), listOf(
            p("somfy_dexxopro_io_manual.pdf", "Somfy Dexxo Pro io — دليل التركيب"),
            p("somfy_dexxopro_io_illustrations.pdf", "Somfy Dexxo Pro io — ملف الرسوم التوضيحية"),
            p("somfy_dexxo_quick_setup.pdf", "Somfy Dexxo Pro / Compact — دليل الإعداد السريع"),
        )),
        PdfLink("Marantec", listOf("Comfort 260"), listOf(p("marantec_comfort260.pdf", "Marantec Comfort 260/270/280 — تعليمات التشغيل"))),
        PdfLink("علامة غير محددة (Generic)", listOf("FS 600", "FS600", "FS 1000", "FS1000"), listOf(
            p("fs1000_manual.pdf", "FS 1000 — دليل التركيب والمستخدم"),
            p("fs_manual_a.pdf", "FS 600/1000/1200 — دليل التركيب والمستخدم"),
        )),
        PdfLink("علامة غير محددة (Generic)", listOf("SL1500", "SL1000"), listOf(p("sl1500ac.pdf", "SL1000AC/SL1500AC — دليل المستخدم"))),
        PdfLink("علامة غير محددة (Generic)", listOf("Magic 1000"), listOf(p("magic1000.pdf", "Magic 1000 — تعليمات التشغيل"))),
    )

    /** يرفق ملفات PDF بأدلة التوصيل والبرمجة لكل مكينة (مرة واحدة لكل ملف). */
    suspend fun attach(context: Context, repo: AppRepository) {
        val brands = repo.brandsOnce()
        val models = repo.modelsOnce()
        val guides = repo.guides.first()
        for (link in links) {
            val brandId = brands.firstOrNull { it.name.equals(link.brand, true) }?.id ?: continue
            val targets = models.filter { m -> m.brandId == brandId && link.keys.any { k -> m.name.contains(k, true) } }
            for (m in targets) {
                val gs = guides.filter { it.modelId == m.id && (it.type == GuideType.WIRING || it.type == GuideType.PROGRAMMING) }
                for (g in gs) {
                    try {
                        val media = repo.mediaOnce(OwnerType.GUIDE, g.id)
                        for (pdf in link.pdfs) {
                            if (media.any { it.title == pdf.title }) continue
                            val stored = repo.mediaStore.copyFromAssets(pdf.file, pdf.file.substringAfterLast('/')) ?: continue
                            repo.db.mediaDao().insert(
                                MediaAttachment(ownerType = OwnerType.GUIDE, ownerId = g.id, kind = MediaKind.DOC, path = stored, title = pdf.title, role = MediaRole.NONE)
                            )
                        }
                    } catch (e: Exception) { /* دليل واحد لا يوقف الباقي */ }
                }
            }
        }
    }
}
