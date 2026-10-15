package com.hamo.doortech.domain

/**
 * توحيد اختلافات الكتابة العربية الشائعة لأغراض البحث فقط (النص المخزن لا يتغيّر):
 * إزالة التشكيل والتطويل، توحيد الألف والياء والتاء المربوطة، تحويل الأرقام الهندية إلى لاتينية،
 * وتحويل الحروف اللاتينية إلى صغيرة وإزالة علامات الترقيم.
 */
object ArabicNormalizer {
    fun normalize(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val c = when (ch) {
                in 'ً'..'ٟ', 'ٰ', 'ـ' -> continue
                'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                'ى', 'ئ' -> 'ي'
                'ة' -> 'ه'
                'ؤ' -> 'و'
                in '٠'..'٩' -> '0' + (ch - '٠')
                in '۰'..'۹' -> '0' + (ch - '۰')
                else -> ch.lowercaseChar()
            }
            sb.append(if (Character.isLetterOrDigit(c)) c else ' ')
        }
        return sb.toString().trim().replace(Regex("\\s+"), " ")
    }

    fun tokens(input: String): List<String> =
        normalize(input).split(' ').filter { it.isNotEmpty() }
}

enum class SearchType(val label: String) {
    FAULT("عطل"),
    MODEL("موديل"),
    GUIDE("دليل"),
    BRAND("شركة"),
}

/** حقل قابل للبحث بوزن يعكس أهميته. [exact] للأكواد: تطابق كامل فقط يحصل على الدرجة العالية. */
data class SearchField(
    val label: String,
    val text: String,
    val weight: Double,
    val exact: Boolean = false,
)

data class SearchDoc(
    val type: SearchType,
    val id: Long,
    val title: String,
    val subtitle: String = "",
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val modelId: Long? = null,
    val source: String = "",
    val level: Int = 0,
    val fields: List<SearchField> = emptyList(),
)

data class SearchFilter(
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val modelId: Long? = null,
    val types: Set<SearchType> = emptySet(),
) {
    val isEmpty: Boolean
        get() = categoryId == null && brandId == null && modelId == null && types.isEmpty()
}

data class SearchResult(
    val doc: SearchDoc,
    val score: Double,
    val matched: List<String>,
    /** هل العنصر مرتبط بالموديل المختار تحديدًا (وليس عامًا)؟ */
    val modelSpecific: Boolean,
)

object SearchEngine {

    /**
     * يبحث في [docs] عن [query]. الاستعلام الفارغ مع مرشحات يعيد قائمة تصفح.
     * لا يعتمد على اتصال ولا على أي خدمة خارجية.
     */
    fun search(docs: List<SearchDoc>, query: String, filter: SearchFilter = SearchFilter()): List<SearchResult> {
        val tokens = ArabicNormalizer.tokens(query)
        val candidates = docs.filter { passes(it, filter) }

        if (tokens.isEmpty()) {
            return candidates
                .map { SearchResult(it, 0.0, emptyList(), isModelSpecific(it, filter)) }
                .sortedWith(compareByDescending<SearchResult> { it.modelSpecific }
                    .thenBy { it.doc.type.ordinal }
                    .thenBy { ArabicNormalizer.normalize(it.doc.title) })
        }

        val results = ArrayList<SearchResult>()
        for (doc in candidates) {
            var score = 0.0
            val matched = LinkedHashSet<String>()
            var matchedTokens = 0
            for (token in tokens) {
                var tokenHit = false
                for (field in doc.fields) {
                    val norm = ArabicNormalizer.normalize(field.text)
                    if (norm.isEmpty()) continue
                    if (field.exact) {
                        // الأكواد: تطابق كامل بين الكود والكلمة، وإلا تطابق جزئي ضعيف.
                        val codeTokens = norm.split(' ')
                        if (norm == token || codeTokens.contains(token)) {
                            score += field.weight * 4.0
                            matched += field.label
                            tokenHit = true
                        } else if (norm.contains(token) && token.length >= 2) {
                            score += field.weight * 0.5
                            matched += field.label
                            tokenHit = true
                        }
                    } else if (norm.contains(token)) {
                        val words = norm.split(' ')
                        val wordMatch = words.contains(token)
                        val prefixMatch = words.any { it.startsWith(token) }
                        val factor = when {
                            wordMatch -> 1.5
                            prefixMatch -> 1.2
                            else -> 1.0
                        }
                        score += field.weight * factor
                        matched += field.label
                        tokenHit = true
                    }
                }
                if (tokenHit) matchedTokens++
            }
            if (matchedTokens == 0) continue
            // مكافأة لتغطية كل كلمات البحث.
            score *= 1.0 + (matchedTokens.toDouble() / tokens.size)
            if (matchedTokens == tokens.size) score *= 1.25
            val specific = isModelSpecific(doc, filter)
            if (specific) score += 3.0
            results += SearchResult(doc, score, matched.toList(), specific)
        }
        return results.sortedWith(
            compareByDescending<SearchResult> { it.score }
                .thenBy { ArabicNormalizer.normalize(it.doc.title) }
        )
    }

    private fun isModelSpecific(doc: SearchDoc, filter: SearchFilter): Boolean =
        filter.modelId != null && doc.modelId == filter.modelId

    /**
     * العناصر العامة (غير المرتبطة بموديل/شركة/تصنيف) تبقى ظاهرة عند التصفية،
     * أما العناصر المرتبطة بقيمة مختلفة فتُستبعد.
     */
    private fun passes(doc: SearchDoc, filter: SearchFilter): Boolean {
        if (filter.types.isNotEmpty() && doc.type !in filter.types) return false
        if (doc.type == SearchType.BRAND) {
            if (filter.categoryId != null || filter.modelId != null) return false
            if (filter.brandId != null && doc.brandId != filter.brandId) return false
            return true
        }
        if (filter.categoryId != null && doc.categoryId != null && doc.categoryId != filter.categoryId) return false
        if (filter.brandId != null && doc.brandId != null && doc.brandId != filter.brandId) return false
        if (filter.modelId != null && doc.modelId != null && doc.modelId != filter.modelId) return false
        if (doc.type == SearchType.MODEL) {
            if (filter.brandId != null && doc.brandId != filter.brandId) return false
            if (filter.modelId != null && doc.modelId != filter.modelId) return false
        }
        return true
    }
}
