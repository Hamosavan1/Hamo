package com.hamo.doortech.data.repo

import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.FaultFull
import com.hamo.doortech.data.db.GuideFull
import com.hamo.doortech.data.db.JobFull
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.TechnicalGuide

/** نتيجة عملية على البيانات (نجاح أو رسالة خطأ مفهومة). */
sealed interface Outcome {
    data class Ok(val id: Long = 0, val message: String = "") : Outcome
    data class Fail(val message: String) : Outcome
}

data class CodeDraft(val code: String, val meaning: String = "")

data class SolutionDraft(
    val title: String,
    val source: String = "",
    val appliesTo: String = "",
    val level: Int = 0,
    val warnings: String = "",
    val tools: String = "",
    val steps: List<String> = emptyList(),
)

data class FaultDraft(
    val fault: Fault,
    val symptoms: List<String> = emptyList(),
    val codes: List<CodeDraft> = emptyList(),
    val solutions: List<SolutionDraft> = emptyList(),
)

data class GuideDraft(
    val guide: TechnicalGuide,
    val steps: List<String> = emptyList(),
)

data class JobDraft(
    val job: MaintenanceJob,
    val parts: List<JobPart> = emptyList(),
)

fun FaultFull.toDraft(): FaultDraft = FaultDraft(
    fault = fault,
    symptoms = symptoms.map { it.text },
    codes = codes.map { CodeDraft(it.code, it.meaning) },
    solutions = solutions.map { s ->
        SolutionDraft(
            title = s.solution.title,
            source = s.solution.source,
            appliesTo = s.solution.appliesTo,
            level = s.solution.level,
            warnings = s.solution.warnings,
            tools = s.solution.tools,
            steps = s.orderedSteps.map { it.text },
        )
    },
)

fun GuideFull.toDraft(): GuideDraft = GuideDraft(guide, orderedSteps.map { it.text })

fun JobFull.toDraft(): JobDraft = JobDraft(job, parts)
