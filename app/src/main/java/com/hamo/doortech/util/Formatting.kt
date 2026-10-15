package com.hamo.doortech.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** تنسيقات مشتركة. الأرقام لاتينية دائمًا لتبقى واضحة في الواجهة والتقارير. */
object Formatting {
    fun date(millis: Long): String =
        SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(millis))

    fun dateTime(millis: Long): String =
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).format(Date(millis))

    fun fileStamp(millis: Long): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date(millis))
}

private const val LRM = "‎"

/** يحيط النص اللاتيني (موديل/كود/رقم) بعلامة اتجاه ليظهر سليمًا داخل جملة عربية RTL. */
fun String.ltr(): String = if (isEmpty()) this else "$LRM$this$LRM"
