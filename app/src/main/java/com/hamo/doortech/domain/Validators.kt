package com.hamo.doortech.domain

/** قواعد التحقق من الحقول. ترجع رسالة خطأ بالعربية أو null إن كانت القيمة سليمة. */
object Validators {
    const val MAX_NAME = 120
    const val MAX_TEXT = 4000

    fun required(label: String, value: String, max: Int = MAX_NAME): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "$label مطلوب"
            t.length > max -> "$label أطول من المسموح ($max حرفًا)"
            else -> null
        }
    }

    fun optionalText(label: String, value: String, max: Int = MAX_TEXT): String? =
        if (value.trim().length > max) "$label أطول من المسموح ($max حرفًا)" else null

    fun isValidUrl(value: String): Boolean {
        val t = value.trim()
        if (t.contains(' ') || t.length < 11) return false
        return (t.startsWith("http://") && t.length > 7) || (t.startsWith("https://") && t.length > 8)
    }

    fun url(value: String): String? =
        if (isValidUrl(value)) null else "الرابط غير صالح (يجب أن يبدأ بـ http:// أو https://)"

    fun nextVisit(visit: Long, next: Long?): String? =
        if (next != null && next < visit) "موعد الصيانة القادمة يجب أن يكون بعد تاريخ الزيارة" else null

    fun quantity(value: String): String? {
        if (value.isBlank()) return null
        val n = value.trim().toIntOrNull() ?: return "الكمية يجب أن تكون رقمًا صحيحًا"
        return if (n < 0) "الكمية لا يمكن أن تكون سالبة" else null
    }

    fun stepsNotEmpty(steps: List<String>): String? =
        if (steps.none { it.isNotBlank() }) "أضف خطوة واحدة على الأقل" else null
}
