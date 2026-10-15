package com.hamo.doortech

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.util.JobReportData
import com.hamo.doortech.util.JobReportPdf
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** يحتاج جهازًا/محاكيًا حقيقيًا (PdfDocument لا يعمل تحت Robolectric). مُجمَّع في CI لكنه غير مُشغَّل هناك. */
@RunWith(AndroidJUnit4::class)
class PdfInstrumentedTest {
    @Test fun pdfReportIsCreated() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val job = MaintenanceJob(id = 1, customerName = "عميل تجريبي", problem = "مشكلة", solution = "حل")
        val f = JobReportPdf(ctx).create(JobReportData(job, emptyList(), "تصنيف", "شركة", "موديل", emptyList()))
        assertTrue(f.exists() && f.length() > 500)
        assertTrue(String(f.readBytes().copyOf(4)) == "%PDF")
    }
}
