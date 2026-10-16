package com.hamo.doortech

import android.content.Context
import com.hamo.doortech.data.db.AppDatabase
import com.hamo.doortech.data.repo.AppRepository
import com.hamo.doortech.data.repo.BackupManager
import com.hamo.doortech.data.repo.SeedData
import com.hamo.doortech.util.JobReportPdf
import com.hamo.doortech.util.MediaStore

/** حاوية الاعتماديات (Service Locator بسيط) تُنشأ مرة واحدة مع التطبيق. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val db: AppDatabase = AppDatabase.create(appContext)
    val mediaStore = MediaStore(appContext)
    val repo = AppRepository(db, mediaStore)
    val backup = BackupManager(repo)
    val pdf = JobReportPdf(appContext)
    private val prefs = appContext.getSharedPreferences("hamo_prefs", Context.MODE_PRIVATE)
    val ai = com.hamo.doortech.data.ai.AiSearchClient()

    /** مفتاح Claude API الخاص بالمستخدم، يُحفظ على الجهاز فقط ولا يدخل النسخ الاحتياطي. */
    var aiKey: String
        get() = prefs.getString("ai_key", "").orEmpty()
        set(v) { prefs.edit().putString("ai_key", v.trim()).apply() }

    /** يحمّل البيانات التجريبية مرة واحدة فقط عند أول تشغيل على قاعدة فارغة. */
    suspend fun seedIfNeeded() {
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            if (!repo.hasAnyData()) SeedData.populate(repo)
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
        // البيانات المرجعية تُحمَّل/تُحدَّث لكل إصدار جديد (آمنة للتكرار) حتى على التثبيتات القديمة.
        if (!prefs.getBoolean(KEY_REF, false)) {
            com.hamo.doortech.data.repo.ReferenceData.populate(repo)
            com.hamo.doortech.data.repo.ReferenceExtra.populate(repo)
            com.hamo.doortech.data.repo.ReferenceImages.attach(appContext, repo)
            com.hamo.doortech.data.repo.ReferenceImages.attachCatalogLinks(repo)
            com.hamo.doortech.data.repo.ReferenceCatalogs.populate(appContext, repo)
            prefs.edit().putBoolean(KEY_REF, true).apply()
        }
    }

    /** يسمح بإعادة تحميل البيانات التجريبية بعد حذفها (من الإعدادات). */
    suspend fun reloadSeed(): Boolean {
        SeedData.populate(repo)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        return true
    }

    companion object {
        private const val KEY_SEEDED = "seed_done_v1"
        private const val KEY_REF = "ref_sliding_v9"
    }
}
