package com.hamo.doortech.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuidePartsTest {
    @Test fun splitStepTakesShortHeadingBeforeColon() {
        val (head, body) = splitStep("المرحلة 1 — ريست اللوحة: افصل الكهرباء، اضغط زر START.")
        assertEquals("المرحلة 1 — ريست اللوحة", head)
        assertEquals("افصل الكهرباء، اضغط زر START.", body)
    }

    @Test fun splitStepKeepsTextWithoutHeading() {
        val text = "تأكد أن ستوبات الفتح والغلق الميكانيكية مثبتة في مكانها الصحيح، وأن فك المحرك مقفل وبعدها: ابدأ."
        val (head, body) = splitStep(text)
        assertNull(head)
        assertEquals(text, body)
    }

    @Test fun sentencesSplitOnStopsButNotDecimals() {
        val parts = sentences("فيوز F1 = T1.25A. اترك جسرًا إن لم يُستخدم. هل تعمل؟ نعم")
        assertEquals(listOf("فيوز F1 = T1.25A.", "اترك جسرًا إن لم يُستخدم.", "هل تعمل؟", "نعم"), parts)
    }
}
