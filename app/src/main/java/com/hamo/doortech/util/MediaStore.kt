package com.hamo.doortech.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.util.UUID

/** مخزن الملفات المحلي: كل الصور والمستندات تُنسخ داخل مجلد خاص بالتطبيق. */
class MediaStore(private val context: Context) {

    val dir: File = File(context.filesDir, "media").also { it.mkdirs() }

    fun fileFor(name: String): File = File(dir, File(name).name)

    fun exists(name: String): Boolean = fileFor(name).exists()

    fun delete(name: String) {
        runCatching { fileFor(name).delete() }
    }

    /** ملف جديد لالتقاط صورة بالكاميرا. */
    fun newCameraFile(): File = File(dir, "img_${UUID.randomUUID()}.jpg")

    fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun uriForName(name: String): Uri = uriFor(fileFor(name))

    /** ينسخ محتوى [source] إلى المخزن ويعيد اسم الملف الجديد (أو null عند الفشل). */
    fun importUri(source: Uri, defaultExt: String): Pair<String, String>? {
        val displayName = queryDisplayName(source)
        val ext = displayName?.substringAfterLast('.', "")?.takeIf { it.length in 1..5 } ?: defaultExt
        val name = "${UUID.randomUUID()}.$ext"
        return try {
            context.contentResolver.openInputStream(source)?.use { input ->
                copyTo(input, fileFor(name))
            } ?: return null
            name to (displayName ?: name)
        } catch (e: Exception) {
            delete(name)
            null
        }
    }

    fun copyFromStream(input: InputStream, preferredName: String): String? {
        val ext = preferredName.substringAfterLast('.', "bin").take(5)
        var name = preferredName
        if (fileFor(name).exists() || name.contains('/') || name.contains("..")) {
            name = "${UUID.randomUUID()}.$ext"
        }
        return try {
            copyTo(input, fileFor(name))
            name
        } catch (e: Exception) {
            delete(name)
            null
        }
    }

    fun mimeFor(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "heic" -> "image/heic"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "*/*"
    }

    private fun copyTo(input: InputStream, target: File) {
        target.outputStream().use { out -> input.copyTo(out) }
    }

    private fun queryDisplayName(uri: Uri): String? = try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }
}
