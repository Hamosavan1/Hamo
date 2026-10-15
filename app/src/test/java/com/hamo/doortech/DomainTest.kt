package com.hamo.doortech

import com.hamo.doortech.domain.ArabicNormalizer
import com.hamo.doortech.domain.SearchDoc
import com.hamo.doortech.domain.SearchEngine
import com.hamo.doortech.domain.SearchField
import com.hamo.doortech.domain.SearchFilter
import com.hamo.doortech.domain.SearchType
import com.hamo.doortech.domain.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainTest {
    @Test fun normalizerUnifiesArabicForms() {
        assertEquals(ArabicNormalizer.normalize("أَحمد"), ArabicNormalizer.normalize("احمد"))
        assertEquals(ArabicNormalizer.normalize("مدرسة"), ArabicNormalizer.normalize("مدرسه"))
        assertEquals(ArabicNormalizer.normalize("على"), ArabicNormalizer.normalize("علي"))
    }

    @Test fun validators() {
        assertNotNull(Validators.required("الاسم", "  "))
        assertNull(Validators.required("الاسم", "باب"))
        assertNotNull(Validators.quantity("-1"))
        assertNotNull(Validators.quantity("abc"))
        assertNull(Validators.quantity("3"))
        assertNotNull(Validators.nextVisit(100, 50))
        assertNull(Validators.nextVisit(100, 200))
        assertTrue(Validators.isValidUrl("https://example.com/v"))
        assertTrue(!Validators.isValidUrl("example.com"))
        assertNotNull(Validators.stepsNotEmpty(listOf(" ", "")))
    }

    private fun docs() = listOf(
        SearchDoc(SearchType.FAULT, 1, "الباب لا يفتح", categoryId = 1, fields = listOf(SearchField("العنوان", "الباب لا يفتح", 5.0))),
        SearchDoc(SearchType.FAULT, 2, "صوت غريب من المحرك", categoryId = 2, modelId = 9, fields = listOf(SearchField("العنوان", "صوت غريب من المحرك", 5.0))),
        SearchDoc(SearchType.FAULT, 3, "كود خطأ", categoryId = 1, fields = listOf(SearchField("كود", "E12", 6.0, exact = true))),
        SearchDoc(SearchType.GUIDE, 4, "دليل تركيب الخلية", categoryId = 1, fields = listOf(SearchField("العنوان", "دليل تركيب الخلية", 5.0))),
    )

    @Test fun searchPartialAndArabicVariants() {
        assertEquals(listOf(1L), SearchEngine.search(docs(), "يفتح").map { it.doc.id })
        assertEquals(listOf(2L), SearchEngine.search(docs(), "المحرك").map { it.doc.id })
        assertTrue(SearchEngine.search(docs(), "كلمة غير موجودة").isEmpty())
    }

    @Test fun searchByErrorCodeIsCaseInsensitive() {
        assertEquals(listOf(3L), SearchEngine.search(docs(), "e12").map { it.doc.id })
    }

    @Test fun filters() {
        assertEquals(setOf(1L, 3L, 4L), SearchEngine.search(docs(), "", SearchFilter(categoryId = 1)).map { it.doc.id }.toSet())
        assertEquals(setOf(4L), SearchEngine.search(docs(), "", SearchFilter(types = setOf(SearchType.GUIDE))).map { it.doc.id }.toSet())
    }
}

class AiParseTest {
    private val sample = """
    {"content":[
      {"type":"server_tool_use","name":"web_search"},
      {"type":"web_search_tool_result","content":[{"type":"web_search_result","url":"https://example.com/manual.pdf","title":"m"}]},
      {"type":"text","text":"هذه النتيجة: {\"found\":true,\"brand\":\"X\",\"model\":\"M1\",\"controlUnit\":\"ZX\",\"specs\":\"230V\",\"sensors\":[\"فوتوسيل\"],\"wiring\":[\"طرف 1\"],\"programming\":[\"خطوة\"],\"remote\":\"أ\nب\",\"faults\":[{\"title\":\"t\",\"code\":\"E1\",\"meaning\":\"m\"}],\"warnings\":\"w\",\"notes\":\"\"} انتهى",
       "citations":[{"url":"https://example.com/cite"}]}
    ]}""".trimIndent()

    @org.junit.Test fun parsesSuccess() {
        val o = com.hamo.doortech.data.ai.AiSearchClient.parseResponse(sample)
        org.junit.Assert.assertTrue(o is com.hamo.doortech.data.ai.AiOutcome.Success)
        val s = o as com.hamo.doortech.data.ai.AiOutcome.Success
        org.junit.Assert.assertEquals("M1", s.result.model)
        org.junit.Assert.assertEquals(2, s.result.remote.size)
        org.junit.Assert.assertEquals("E1", s.result.faults.single().code)
        org.junit.Assert.assertTrue(s.sources.contains("https://example.com/manual.pdf"))
        org.junit.Assert.assertTrue(s.sources.contains("https://example.com/cite"))
    }

    @org.junit.Test fun rejectsGarbage() {
        org.junit.Assert.assertTrue(com.hamo.doortech.data.ai.AiSearchClient.parseResponse("nope") is com.hamo.doortech.data.ai.AiOutcome.Failure)
        org.junit.Assert.assertTrue(com.hamo.doortech.data.ai.AiSearchClient.parseResponse("""{"content":[{"type":"text","text":"بدون json"}]}""") is com.hamo.doortech.data.ai.AiOutcome.Failure)
    }

    @org.junit.Test fun requestIsWellFormed() {
        val j = com.google.gson.JsonParser.parseString(com.hamo.doortech.data.ai.AiSearchClient.buildRequest("m", "CAME BX-243")).asJsonObject
        org.junit.Assert.assertEquals("web_search", j.getAsJsonArray("tools")[0].asJsonObject.get("name").asString)
        org.junit.Assert.assertTrue(j.getAsJsonArray("messages")[0].asJsonObject.get("content").asString.contains("BX-243"))
    }
}
