package com.hamo.doortech

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.util.JobReportData
import com.hamo.doortech.util.JobReportPdf
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PdfTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()

    @Test fun pdfReportIsCreated() {
        val job = MaintenanceJob(id = 1, customerName = "عميل تجريبي", problem = "مشكلة", solution = "حل")
        val f = JobReportPdf(ctx).create(JobReportData(job, emptyList(), "تصنيف", "شركة", "موديل", emptyList()))
        assertTrue(f.exists())
        assertTrue(f.length() > 500)
        assertTrue(f.readBytes().take(4).map { it.toInt().toChar() }.joinToString("") == "%PDF")
    }
}
