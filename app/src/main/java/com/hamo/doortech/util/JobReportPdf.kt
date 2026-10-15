package com.hamo.doortech.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.MediaRole
import java.io.File

data class ReportImage(val role: String, val file: File)

data class JobReportData(
    val job: MaintenanceJob,
    val parts: List<JobPart>,
    val categoryName: String?,
    val brandName: String?,
    val modelName: String?,
    val images: List<ReportImage>,
)

/**
 * يُنشئ تقرير صيانة PDF عربيًا باستخدام Android PdfDocument.
 * الفقرات تُرسم عبر StaticLayout بتحديد اتجاه النص (RTL) حتى تُعالج الحروف العربية والأرقام صحيحًا،
 * وتنقسم تلقائيًا على الصفحات.
 */
class JobReportPdf(private val context: Context) {

    private val pageW = 595
    private val pageH = 842
    private val margin = 40
    private val contentW = pageW - 2 * margin

    private val navy = Color.parseColor("#0B1F3A")
    private val orange = Color.parseColor("#F97316")

    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F2937"); textSize = 13f
    }
    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy; textSize = 12f; typeface = Typeface.DEFAULT_BOLD
    }
    private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 20f; typeface = Typeface.DEFAULT_BOLD
    }
    private val subTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB"); textSize = 12f
    }

    fun create(data: JobReportData): File {
        val dir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val out = File(dir, "report_${data.job.id}_${Formatting.fileStamp(System.currentTimeMillis())}.pdf")
        val doc = PdfDocument()
        val writer = PageWriter(doc)
        try {
            drawHeader(writer, data)
            section(writer, "العميل / الموقع", listOfNotNull(data.job.customerName, data.job.location.takeIf { it.isNotBlank() }).joinToString(" — "))
            section(writer, "تاريخ الزيارة", Formatting.date(data.job.visitDate))
            val machine = listOfNotNull(data.categoryName, data.brandName, data.modelName).joinToString(" • ")
            if (machine.isNotBlank()) section(writer, "نوع الباب والشركة والموديل", machine)
            section(writer, "وصف المشكلة", data.job.problem)
            section(writer, "نتائج الفحص", data.job.findings)
            section(writer, "الحل المنفذ", data.job.solution)
            if (data.parts.isNotEmpty()) {
                section(writer, "قطع الغيار المستخدمة", data.parts.joinToString("\n") { "• ${it.name} × ${it.quantity}" })
            } else {
                section(writer, "قطع الغيار المستخدمة", "لا توجد")
            }
            section(writer, "ملاحظات الفني", data.job.notes)
            data.job.nextVisitDate?.let { section(writer, "موعد الصيانة القادمة", Formatting.date(it)) }
            drawImages(writer, data.images)
            writer.finish()
            out.outputStream().use { doc.writeTo(it) }
        } finally {
            doc.close()
        }
        return out
    }

    private fun drawHeader(w: PageWriter, data: JobReportData) {
        val bandH = 70f
        val paint = Paint().apply { color = navy }
        w.canvas.drawRect(0f, 0f, pageW.toFloat(), bandH, paint)
        val accent = Paint().apply { color = orange }
        w.canvas.drawRect(0f, bandH, pageW.toFloat(), bandH + 4f, accent)
        w.y = 14f
        w.drawLayout(layout("تقرير صيانة", titlePaint), margin.toFloat(), colorOverride = true)
        w.drawLayout(layout("HAMO DOOR TECH — رقم الزيارة ${data.job.id}", subTitlePaint), margin.toFloat(), colorOverride = true)
        w.y = bandH + 20f
    }

    private fun section(w: PageWriter, label: String, value: String) {
        val text = value.trim().ifEmpty { "—" }
        w.ensure(40f)
        w.drawLayout(layout(label, labelPaint), margin.toFloat())
        w.y += 2f
        w.drawLayout(layout(text, bodyPaint), margin.toFloat())
        w.y += 10f
    }

    private fun drawImages(w: PageWriter, images: List<ReportImage>) {
        if (images.isEmpty()) return
        w.ensure(30f)
        w.drawLayout(layout("الصور", labelPaint), margin.toFloat())
        w.y += 6f
        val maxH = 230
        for (img in images) {
            val bmp = decode(img.file, contentW) ?: continue
            val scale = minOf(contentW.toFloat() / bmp.width, maxH.toFloat() / bmp.height, 1f)
            val dw = (bmp.width * scale).toInt()
            val dh = (bmp.height * scale).toInt()
            val caption = MediaRole.label(img.role)
            w.ensure(dh + 36f)
            if (caption.isNotEmpty()) {
                w.drawLayout(layout("صورة $caption", labelPaint), margin.toFloat())
                w.y += 2f
            }
            val left = margin + (contentW - dw) / 2f
            val scaled = Bitmap.createScaledBitmap(bmp, dw, dh, true)
            w.canvas.drawBitmap(scaled, left, w.y, null)
            w.y += dh + 12f
            if (scaled !== bmp) scaled.recycle()
            bmp.recycle()
        }
    }

    private fun decode(file: File, targetW: Int): Bitmap? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetW) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun layout(text: String, paint: TextPaint): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, contentW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .setLineSpacing(0f, 1.25f)
            .build()

    /** يدير الصفحات ويقسّم الفقرات الطويلة بين الصفحات سطرًا بسطر. */
    private inner class PageWriter(private val doc: PdfDocument) {
        private var number = 1
        private var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, number).create())
        var canvas = page.canvas
            private set
        var y = margin.toFloat()

        fun ensure(height: Float) {
            if (y + height > pageH - margin) newPage()
        }

        private fun newPage() {
            drawFooter()
            doc.finishPage(page)
            number++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, number).create())
            canvas = page.canvas
            y = margin.toFloat()
        }

        fun drawLayout(layout: StaticLayout, x: Float, colorOverride: Boolean = false) {
            for (i in 0 until layout.lineCount) {
                val top = layout.getLineTop(i)
                val bottom = layout.getLineBottom(i)
                val h = (bottom - top).toFloat()
                if (!colorOverride) ensure(h)
                canvas.save()
                canvas.translate(x, y - top)
                canvas.clipRect(0f, top.toFloat(), contentW.toFloat(), bottom.toFloat())
                layout.draw(canvas)
                canvas.restore()
                y += h
            }
        }

        private fun drawFooter() {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY; textSize = 9f }
            canvas.drawText("HAMO DOOR TECH — $number", margin.toFloat(), pageH - 20f, p)
        }

        fun finish() {
            drawFooter()
            doc.finishPage(page)
        }
    }
}
