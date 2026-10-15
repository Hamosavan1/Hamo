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

    /** يحمّل البيانات التجريبية مرة واحدة فقط عند أول تشغيل على قاعدة فارغة. */
    suspend fun seedIfNeeded() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        if (!repo.hasAnyData()) SeedData.populate(repo)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    /** يسمح بإعادة تحميل البيانات التجريبية بعد حذفها (من الإعدادات). */
    suspend fun reloadSeed(): Boolean {
        SeedData.populate(repo)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        return true
    }

    companion object {
        private const val KEY_SEEDED = "seed_done_v1"
    }
}
