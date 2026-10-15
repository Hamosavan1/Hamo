package com.hamo.doortech.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.repo.CodeDraft
import com.hamo.doortech.data.repo.FaultDraft
import com.hamo.doortech.data.repo.GuideDraft
import com.hamo.doortech.data.repo.JobDraft
import com.hamo.doortech.data.repo.SolutionDraft

/** حالات النماذج القابلة للتعديل (Compose state) — تُحوَّل إلى مسودات عند الحفظ. */

class CodeForm(code: String = "", meaning: String = "") {
    var code by mutableStateOf(code)
    var meaning by mutableStateOf(meaning)
}

class SolutionForm(initial: SolutionDraft? = null) {
    var title by mutableStateOf(initial?.title ?: "")
    var source by mutableStateOf(initial?.source ?: "")
    var appliesTo by mutableStateOf(initial?.appliesTo ?: "")
    var level by mutableIntStateOf(initial?.level ?: 0)
    var warnings by mutableStateOf(initial?.warnings ?: "")
    var tools by mutableStateOf(initial?.tools ?: "")
    val steps = mutableStateListOf<String>().apply {
        addAll(initial?.steps?.takeIf { it.isNotEmpty() } ?: listOf(""))
    }

    fun toDraft() = SolutionDraft(
        title = title, source = source, appliesTo = appliesTo, level = level,
        warnings = warnings, tools = tools, steps = steps.toList(),
    )
}

class FaultForm(initial: FaultDraft?, presetModelId: Long? = null, presetCategoryId: Long? = null) {
    private val base: Fault = initial?.fault ?: Fault(title = "")
    var title by mutableStateOf(base.title)
    var categoryId by mutableStateOf(base.categoryId ?: presetCategoryId)
    var modelId by mutableStateOf(base.modelId ?: presetModelId)
    var description by mutableStateOf(base.description)
    var altKeywords by mutableStateOf(base.altKeywords)
    var warnings by mutableStateOf(base.warnings)
    var tools by mutableStateOf(base.tools)
    var source by mutableStateOf(base.source)
    var level by mutableIntStateOf(base.level)
    val symptoms = mutableStateListOf<String>().apply { addAll(initial?.symptoms ?: emptyList()) }
    val codes = mutableStateListOf<CodeForm>().apply {
        addAll((initial?.codes ?: emptyList()).map { CodeForm(it.code, it.meaning) })
    }
    val solutions = mutableStateListOf<SolutionForm>().apply {
        addAll((initial?.solutions ?: emptyList()).map { SolutionForm(it) })
    }

    fun toDraft() = FaultDraft(
        fault = base.copy(
            title = title, categoryId = categoryId, modelId = modelId, description = description,
            altKeywords = altKeywords, warnings = warnings, tools = tools, source = source, level = level,
        ),
        symptoms = symptoms.toList(),
        codes = codes.map { CodeDraft(it.code, it.meaning) },
        solutions = solutions.map { it.toDraft() },
    )
}

class GuideForm(initial: GuideDraft?, type: String) {
    private val base: TechnicalGuide = initial?.guide ?: TechnicalGuide(type = type, title = "")
    var title by mutableStateOf(base.title)
    var categoryId by mutableStateOf(base.categoryId)
    var modelId by mutableStateOf(base.modelId)
    var summary by mutableStateOf(base.summary)
    var warnings by mutableStateOf(base.warnings)
    var source by mutableStateOf(base.source)
    var level by mutableIntStateOf(base.level)
    val steps = mutableStateListOf<String>().apply {
        addAll(initial?.steps?.takeIf { it.isNotEmpty() } ?: listOf(""))
    }

    fun toDraft() = GuideDraft(
        guide = base.copy(
            title = title, categoryId = categoryId, modelId = modelId, summary = summary,
            warnings = warnings, source = source, level = level,
        ),
        steps = steps.toList(),
    )
}

class PartForm(name: String = "", quantity: String = "1", val partId: Long? = null) {
    var name by mutableStateOf(name)
    var quantity by mutableStateOf(quantity)
}

class JobForm(initial: JobDraft?) {
    private val base: MaintenanceJob = initial?.job ?: MaintenanceJob(customerName = "")
    var customerName by mutableStateOf(base.customerName)
    var location by mutableStateOf(base.location)
    var categoryId by mutableStateOf(base.categoryId)
    var brandId by mutableStateOf(base.brandId)
    var modelId by mutableStateOf(base.modelId)
    var problem by mutableStateOf(base.problem)
    var findings by mutableStateOf(base.findings)
    var solution by mutableStateOf(base.solution)
    var notes by mutableStateOf(base.notes)
    var visitDate by mutableStateOf(base.visitDate)
    var nextVisitDate by mutableStateOf(base.nextVisitDate)
    val parts = mutableStateListOf<PartForm>().apply {
        addAll((initial?.parts ?: emptyList()).map { PartForm(it.name, it.quantity.toString(), it.partId) })
    }

    /** يعيد null مع خطأ في [error] إن كانت الكميات غير صالحة. */
    fun toDraft(): Pair<JobDraft?, String?> {
        val list = ArrayList<JobPart>()
        for (p in parts) {
            if (p.name.isBlank() && p.quantity.isBlank()) continue
            val q = p.quantity.trim().toIntOrNull()
                ?: return null to "كمية القطعة «${p.name}» يجب أن تكون رقمًا صحيحًا"
            list += JobPart(jobId = 0, partId = p.partId, name = p.name, quantity = q)
        }
        return JobDraft(
            job = base.copy(
                customerName = customerName, location = location, categoryId = categoryId,
                brandId = brandId, modelId = modelId, problem = problem, findings = findings,
                solution = solution, notes = notes, visitDate = visitDate, nextVisitDate = nextVisitDate,
            ),
            parts = list,
        ) to null
    }
}

class ModelForm(initial: MachineModel?, presetBrandId: Long? = null) {
    private val base: MachineModel = initial ?: MachineModel(brandId = presetBrandId ?: 0L, name = "")
    var name by mutableStateOf(base.name)
    var brandId by mutableStateOf<Long?>(base.brandId.takeIf { it > 0 })
    var categoryId by mutableStateOf(base.categoryId)
    var controlUnit by mutableStateOf(base.controlUnit)
    var specs by mutableStateOf(base.specs)
    var remoteInfo by mutableStateOf(base.remoteInfo)
    var sensorsInfo by mutableStateOf(base.sensorsInfo)
    var notes by mutableStateOf(base.notes)
    var source by mutableStateOf(base.source)

    fun toModel() = base.copy(
        name = name, brandId = brandId ?: 0L, categoryId = categoryId, controlUnit = controlUnit,
        specs = specs, remoteInfo = remoteInfo, sensorsInfo = sensorsInfo, notes = notes, source = source,
    )
}
