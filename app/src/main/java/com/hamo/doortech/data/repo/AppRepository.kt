package com.hamo.doortech.data.repo

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.room.withTransaction
import com.hamo.doortech.data.db.AppDatabase
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.ErrorCode
import com.hamo.doortech.data.db.Favorite
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.FaultFull
import com.hamo.doortech.data.db.FaultSymptom
import com.hamo.doortech.data.db.GuideFull
import com.hamo.doortech.data.db.GuideStep
import com.hamo.doortech.data.db.JobFull
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.Solution
import com.hamo.doortech.data.db.SolutionStep
import com.hamo.doortech.data.db.SparePart
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.domain.SearchDoc
import com.hamo.doortech.domain.SearchField
import com.hamo.doortech.domain.SearchType
import com.hamo.doortech.domain.Validators
import com.hamo.doortech.util.MediaStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * مصدر البيانات الوحيد للتطبيق (Room). كل عمليات الإضافة والتعديل والحذف تمر من هنا
 * مع التحقق من الحقول ومنع حذف ما له تبعيات.
 */
class AppRepository(val db: AppDatabase, val mediaStore: MediaStore) {
    private val catalog = db.catalogDao()
    private val faultDao = db.faultDao()
    private val guideDao = db.guideDao()
    private val jobDao = db.jobDao()
    private val mediaDao = db.mediaDao()
    private val favDao = db.favoriteDao()

    // ===== تدفقات القراءة =====
    val categories: Flow<List<DoorCategory>> = catalog.categories()
    val brands: Flow<List<Brand>> = catalog.brands()
    val models: Flow<List<MachineModel>> = catalog.models()
    val parts: Flow<List<SparePart>> = catalog.parts()
    val faults: Flow<List<Fault>> = faultDao.faults()
    val guides: Flow<List<TechnicalGuide>> = guideDao.guides()
    val jobs: Flow<List<MaintenanceJob>> = jobDao.jobs()
    val favorites: Flow<List<Favorite>> = favDao.all()
    val allMedia: Flow<List<MediaAttachment>> = mediaDao.all()
    val faultCount: Flow<Int> = faultDao.count()
    val guideCount: Flow<Int> = guideDao.count()
    val jobCount: Flow<Int> = jobDao.count()
    val modelCount: Flow<Int> = catalog.models().map { it.size }

    fun guidesByType(type: String) = guideDao.guidesByType(type)
    fun modelFlow(id: Long) = catalog.modelFlow(id)
    fun faultFull(id: Long) = faultDao.faultFull(id)
    fun guideFull(id: Long) = guideDao.guideFull(id)
    fun jobFull(id: Long) = jobDao.jobFull(id)
    fun faultsForModel(id: Long) = faultDao.faultsForModel(id)
    fun guidesForModel(id: Long) = guideDao.guidesForModel(id)
    fun partsForModel(id: Long) = catalog.partsForModel(id)
    fun mediaFor(type: String, id: Long) = mediaDao.forOwner(type, id)
    fun isFavorite(type: String, id: Long): Flow<Boolean> = favDao.isFavorite(type, id).map { it > 0 }

    suspend fun model(id: Long) = catalog.model(id)
    suspend fun faultFullOnce(id: Long) = faultDao.faultFullOnce(id)
    suspend fun guideFullOnce(id: Long) = guideDao.guideFullOnce(id)
    suspend fun jobFullOnce(id: Long) = jobDao.jobFullOnce(id)
    suspend fun categoriesOnce() = catalog.categoriesOnce()
    suspend fun brandsOnce() = catalog.brandsOnce()
    suspend fun modelsOnce() = catalog.modelsOnce()
    suspend fun partsOnce() = catalog.partsOnce()
    suspend fun mediaOnce(type: String, id: Long) = mediaDao.forOwnerOnce(type, id)

    private fun now() = System.currentTimeMillis()

    // ===== التصنيفات =====
    suspend fun saveCategory(c: DoorCategory): Outcome {
        Validators.required("اسم التصنيف", c.name)?.let { return Outcome.Fail(it) }
        Validators.optionalText("الوصف", c.description)?.let { return Outcome.Fail(it) }
        val clean = c.copy(name = c.name.trim(), description = c.description.trim())
        val dup = catalog.categoryByName(clean.name)
        if (dup != null && dup.id != clean.id) return Outcome.Fail("يوجد تصنيف بنفس الاسم")
        return if (clean.id == 0L) {
            Outcome.Ok(catalog.insertCategory(clean.copy(createdAt = now(), updatedAt = now())))
        } else {
            catalog.updateCategory(clean.copy(updatedAt = now()))
            Outcome.Ok(clean.id)
        }
    }

    suspend fun deleteCategory(c: DoorCategory): Outcome {
        val used = catalog.categoryUsage(c.id)
        if (used > 0) return Outcome.Fail("لا يمكن حذف التصنيف: مستخدم في $used عنصر. انقل العناصر أو احذفها أولًا.")
        catalog.deleteCategory(c)
        return Outcome.Ok(c.id)
    }

    // ===== الشركات =====
    suspend fun saveBrand(b: Brand): Outcome {
        Validators.required("اسم الشركة", b.name)?.let { return Outcome.Fail(it) }
        Validators.optionalText("الملاحظات", b.notes)?.let { return Outcome.Fail(it) }
        val clean = b.copy(name = b.name.trim(), country = b.country.trim(), notes = b.notes.trim())
        val dup = catalog.brandByName(clean.name)
        if (dup != null && dup.id != clean.id) return Outcome.Fail("توجد شركة بنفس الاسم")
        return if (clean.id == 0L) {
            Outcome.Ok(catalog.insertBrand(clean.copy(createdAt = now(), updatedAt = now())))
        } else {
            catalog.updateBrand(clean.copy(updatedAt = now()))
            Outcome.Ok(clean.id)
        }
    }

    suspend fun deleteBrand(b: Brand): Outcome {
        val used = catalog.brandUsage(b.id)
        if (used > 0) return Outcome.Fail("لا يمكن حذف الشركة: مرتبطة بـ $used عنصر (موديلات/قطع/زيارات).")
        catalog.deleteBrand(b)
        return Outcome.Ok(b.id)
    }

    // ===== الموديلات =====
    suspend fun saveModel(m: MachineModel): Outcome {
        Validators.required("اسم الموديل", m.name)?.let { return Outcome.Fail(it) }
        if (m.brandId <= 0L) return Outcome.Fail("اختر الشركة المصنعة")
        listOf("المواصفات" to m.specs, "الملاحظات" to m.notes, "معلومات الريموت" to m.remoteInfo, "معلومات الحساسات" to m.sensorsInfo)
            .forEach { (l, v) -> Validators.optionalText(l, v)?.let { return Outcome.Fail(it) } }
        val clean = m.copy(
            name = m.name.trim(), controlUnit = m.controlUnit.trim(), specs = m.specs.trim(),
            remoteInfo = m.remoteInfo.trim(), sensorsInfo = m.sensorsInfo.trim(),
            notes = m.notes.trim(), source = m.source.trim(),
        )
        val dup = catalog.modelByBrandAndName(clean.brandId, clean.name)
        if (dup != null && dup.id != clean.id) return Outcome.Fail("هذا الموديل موجود مسبقًا لنفس الشركة")
        return try {
            if (clean.id == 0L) {
                Outcome.Ok(catalog.insertModel(clean.copy(createdAt = now(), updatedAt = now())))
            } else {
                catalog.updateModel(clean.copy(updatedAt = now()))
                Outcome.Ok(clean.id)
            }
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("تعذّر الحفظ: الشركة أو التصنيف غير موجود")
        }
    }

    suspend fun deleteModel(m: MachineModel): Outcome {
        val used = catalog.modelUsage(m.id)
        if (used > 0) return Outcome.Fail("لا يمكن حذف الموديل: مرتبط بـ $used عنصر (أعطال/أدلة/زيارات/قطع).")
        deleteAttachmentsOf(OwnerType.MODEL, m.id)
        favDao.delete(OwnerType.MODEL, m.id)
        catalog.deleteModel(m)
        return Outcome.Ok(m.id)
    }

    // ===== قطع الغيار =====
    suspend fun savePart(p: SparePart): Outcome {
        Validators.required("اسم القطعة", p.name)?.let { return Outcome.Fail(it) }
        if (p.quantity < 0) return Outcome.Fail("الكمية لا يمكن أن تكون سالبة")
        val clean = p.copy(name = p.name.trim(), partNumber = p.partNumber.trim(), notes = p.notes.trim())
        return try {
            if (clean.id == 0L) {
                Outcome.Ok(catalog.insertPart(clean.copy(createdAt = now(), updatedAt = now())))
            } else {
                catalog.updatePart(clean.copy(updatedAt = now()))
                Outcome.Ok(clean.id)
            }
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("تعذّر الحفظ: الشركة أو الموديل غير موجود")
        }
    }

    suspend fun deletePart(p: SparePart): Outcome {
        catalog.deletePart(p)
        return Outcome.Ok(p.id)
    }

    // ===== الأعطال =====
    suspend fun saveFault(draft: FaultDraft): Outcome {
        val f = draft.fault
        Validators.required("عنوان العطل", f.title)?.let { return Outcome.Fail(it) }
        listOf("الوصف" to f.description, "التحذيرات" to f.warnings, "الأدوات" to f.tools, "كلمات البحث" to f.altKeywords)
            .forEach { (l, v) -> Validators.optionalText(l, v)?.let { return Outcome.Fail(it) } }
        draft.codes.forEach { c ->
            if (c.code.isBlank() && c.meaning.isNotBlank()) return Outcome.Fail("أدخل الكود لكل معنى أضفته")
        }
        for (s in draft.solutions) {
            if (s.title.isBlank()) return Outcome.Fail("كل حل يحتاج عنوانًا")
            Validators.stepsNotEmpty(s.steps)?.let { return Outcome.Fail("الحل «${s.title}»: $it") }
        }
        return try {
            val id = db.withTransaction {
                val clean = f.copy(
                    title = f.title.trim(), description = f.description.trim(), altKeywords = f.altKeywords.trim(),
                    warnings = f.warnings.trim(), tools = f.tools.trim(), source = f.source.trim(),
                )
                val faultId = if (clean.id == 0L) {
                    faultDao.insertFault(clean.copy(createdAt = now(), updatedAt = now()))
                } else {
                    faultDao.updateFault(clean.copy(updatedAt = now()))
                    faultDao.deleteSymptoms(clean.id)
                    faultDao.deleteCodes(clean.id)
                    faultDao.deleteSolutions(clean.id)
                    clean.id
                }
                draft.symptoms.map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                    faultDao.insertSymptom(FaultSymptom(faultId = faultId, text = it))
                }
                draft.codes.filter { it.code.isNotBlank() }.forEach {
                    faultDao.insertCode(ErrorCode(faultId = faultId, code = it.code.trim(), meaning = it.meaning.trim()))
                }
                draft.solutions.forEach { s ->
                    val sid = faultDao.insertSolution(
                        Solution(
                            faultId = faultId, title = s.title.trim(), source = s.source.trim(),
                            appliesTo = s.appliesTo.trim(), level = s.level,
                            warnings = s.warnings.trim(), tools = s.tools.trim(),
                        )
                    )
                    s.steps.map { it.trim() }.filter { it.isNotEmpty() }.forEachIndexed { i, text ->
                        faultDao.insertSolutionStep(SolutionStep(solutionId = sid, position = i, text = text))
                    }
                }
                faultId
            }
            Outcome.Ok(id)
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("تعذّر الحفظ: التصنيف أو الموديل المرتبط غير موجود")
        }
    }

    suspend fun deleteFault(id: Long): Outcome {
        deleteAttachmentsOf(OwnerType.FAULT, id)
        favDao.delete(OwnerType.FAULT, id)
        faultDao.deleteFault(id)
        return Outcome.Ok(id)
    }

    suspend fun duplicateFault(id: Long): Outcome {
        val full = faultDao.faultFullOnce(id) ?: return Outcome.Fail("العطل غير موجود")
        val d = full.toDraft()
        return saveFault(
            d.copy(fault = d.fault.copy(id = 0, title = d.fault.title + " (نسخة)", isSeed = false))
        )
    }

    // ===== الأدلة =====
    suspend fun saveGuide(draft: GuideDraft): Outcome {
        val g = draft.guide
        Validators.required("عنوان الدليل", g.title)?.let { return Outcome.Fail(it) }
        Validators.optionalText("الملخص", g.summary)?.let { return Outcome.Fail(it) }
        Validators.optionalText("التحذيرات", g.warnings)?.let { return Outcome.Fail(it) }
        Validators.stepsNotEmpty(draft.steps)?.let { return Outcome.Fail(it) }
        return try {
            val id = db.withTransaction {
                val clean = g.copy(
                    title = g.title.trim(), summary = g.summary.trim(),
                    warnings = g.warnings.trim(), source = g.source.trim(),
                )
                val gid = if (clean.id == 0L) {
                    guideDao.insertGuide(clean.copy(createdAt = now(), updatedAt = now()))
                } else {
                    guideDao.updateGuide(clean.copy(updatedAt = now()))
                    guideDao.deleteSteps(clean.id)
                    clean.id
                }
                draft.steps.map { it.trim() }.filter { it.isNotEmpty() }.forEachIndexed { i, t ->
                    guideDao.insertStep(GuideStep(guideId = gid, position = i, text = t))
                }
                gid
            }
            Outcome.Ok(id)
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("تعذّر الحفظ: التصنيف أو الموديل المرتبط غير موجود")
        }
    }

    suspend fun deleteGuide(id: Long): Outcome {
        deleteAttachmentsOf(OwnerType.GUIDE, id)
        favDao.delete(OwnerType.GUIDE, id)
        guideDao.deleteGuide(id)
        return Outcome.Ok(id)
    }

    suspend fun duplicateGuide(id: Long): Outcome {
        val full = guideDao.guideFullOnce(id) ?: return Outcome.Fail("الدليل غير موجود")
        val d = full.toDraft()
        return saveGuide(d.copy(guide = d.guide.copy(id = 0, title = d.guide.title + " (نسخة)", isSeed = false)))
    }

    // ===== سجل الصيانة =====
    suspend fun saveJob(draft: JobDraft): Outcome {
        val j = draft.job
        Validators.required("اسم العميل أو الموقع", j.customerName)?.let { return Outcome.Fail(it) }
        Validators.nextVisit(j.visitDate, j.nextVisitDate)?.let { return Outcome.Fail(it) }
        listOf("وصف المشكلة" to j.problem, "نتائج الفحص" to j.findings, "الحل المنفذ" to j.solution, "الملاحظات" to j.notes)
            .forEach { (l, v) -> Validators.optionalText(l, v)?.let { return Outcome.Fail(it) } }
        draft.parts.forEach { p ->
            if (p.name.isBlank()) return Outcome.Fail("اسم القطعة المستخدمة مطلوب")
            if (p.quantity <= 0) return Outcome.Fail("كمية القطعة «${p.name}» يجب أن تكون أكبر من صفر")
        }
        return try {
            val id = db.withTransaction {
                val clean = j.copy(
                    customerName = j.customerName.trim(), location = j.location.trim(),
                    problem = j.problem.trim(), findings = j.findings.trim(),
                    solution = j.solution.trim(), notes = j.notes.trim(),
                )
                val jid = if (clean.id == 0L) {
                    jobDao.insertJob(clean.copy(createdAt = now(), updatedAt = now()))
                } else {
                    jobDao.updateJob(clean.copy(updatedAt = now()))
                    jobDao.deleteJobParts(clean.id)
                    clean.id
                }
                draft.parts.forEach { p ->
                    jobDao.insertJobPart(JobPart(jobId = jid, partId = p.partId, name = p.name.trim(), quantity = p.quantity))
                }
                jid
            }
            Outcome.Ok(id)
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("تعذّر الحفظ: التصنيف أو الشركة أو الموديل المرتبط غير موجود")
        }
    }

    suspend fun deleteJob(id: Long): Outcome {
        deleteAttachmentsOf(OwnerType.JOB, id)
        jobDao.deleteJob(id)
        return Outcome.Ok(id)
    }

    suspend fun duplicateJob(id: Long): Outcome {
        val full = jobDao.jobFullOnce(id) ?: return Outcome.Fail("الزيارة غير موجودة")
        val d = full.toDraft()
        return saveJob(
            JobDraft(
                job = d.job.copy(id = 0, visitDate = now(), nextVisitDate = null, isSeed = false),
                parts = d.parts.map { it.copy(id = 0, jobId = 0) },
            )
        )
    }

    // ===== المرفقات =====
    suspend fun addImage(ownerType: String, ownerId: Long, source: Uri, role: String = MediaRole.NONE): Outcome =
        withContext(Dispatchers.IO) {
            val imported = mediaStore.importUri(source, "jpg") ?: return@withContext Outcome.Fail("تعذّر قراءة الصورة")
            Outcome.Ok(mediaDao.insert(MediaAttachment(ownerType = ownerType, ownerId = ownerId, kind = MediaKind.IMAGE, path = imported.first, title = "", role = role)))
        }

    suspend fun addCameraImage(ownerType: String, ownerId: Long, file: File, role: String = MediaRole.NONE): Outcome =
        withContext(Dispatchers.IO) {
            if (!file.exists() || file.length() == 0L) {
                file.delete()
                return@withContext Outcome.Fail("لم يتم التقاط صورة")
            }
            Outcome.Ok(mediaDao.insert(MediaAttachment(ownerType = ownerType, ownerId = ownerId, kind = MediaKind.IMAGE, path = file.name, role = role)))
        }

    suspend fun addDocument(ownerType: String, ownerId: Long, source: Uri): Outcome =
        withContext(Dispatchers.IO) {
            val imported = mediaStore.importUri(source, "bin") ?: return@withContext Outcome.Fail("تعذّر قراءة الملف")
            Outcome.Ok(mediaDao.insert(MediaAttachment(ownerType = ownerType, ownerId = ownerId, kind = MediaKind.DOC, path = imported.first, title = imported.second)))
        }

    suspend fun addVideoLink(ownerType: String, ownerId: Long, url: String, title: String): Outcome {
        Validators.url(url)?.let { return Outcome.Fail(it) }
        return Outcome.Ok(mediaDao.insert(MediaAttachment(ownerType = ownerType, ownerId = ownerId, kind = MediaKind.LINK, path = url.trim(), title = title.trim())))
    }

    suspend fun deleteMedia(id: Long): Outcome = withContext(Dispatchers.IO) {
        val m = mediaDao.byId(id) ?: return@withContext Outcome.Fail("المرفق غير موجود")
        mediaDao.delete(id)
        if (m.kind != MediaKind.LINK) {
            val stillUsed = mediaDao.allOnce().any { it.path == m.path }
            if (!stillUsed) mediaStore.delete(m.path)
        }
        Outcome.Ok(id)
    }

    private suspend fun deleteAttachmentsOf(type: String, id: Long) = withContext(Dispatchers.IO) {
        val list = mediaDao.forOwnerOnce(type, id)
        mediaDao.deleteForOwner(type, id)
        val remaining = mediaDao.allOnce().map { it.path }.toSet()
        list.filter { it.kind != MediaKind.LINK && it.path !in remaining }.forEach { mediaStore.delete(it.path) }
    }

    // ===== المفضلة =====
    suspend fun toggleFavorite(type: String, id: Long) {
        val isFav = favDao.isFavorite(type, id).first() > 0
        if (isFav) favDao.delete(type, id) else favDao.insert(Favorite(targetType = type, targetId = id))
    }

    // ===== البحث =====
    suspend fun searchDocs(): List<SearchDoc> = withContext(Dispatchers.IO) {
        val brands = catalog.brandsOnce()
        val models = catalog.modelsOnce()
        val brandById = brands.associateBy { it.id }
        val modelById = models.associateBy { it.id }
        val docs = ArrayList<SearchDoc>()

        for (b in brands) {
            docs += SearchDoc(
                type = SearchType.BRAND, id = b.id, title = b.name, subtitle = b.country,
                brandId = b.id,
                fields = listOf(SearchField("اسم الشركة", b.name, 4.0), SearchField("ملاحظات", b.notes, 0.8)),
            )
        }
        for (m in models) {
            val brand = brandById[m.brandId]
            docs += SearchDoc(
                type = SearchType.MODEL, id = m.id, title = m.name, subtitle = brand?.name.orEmpty(),
                categoryId = m.categoryId, brandId = m.brandId, modelId = m.id, source = m.source,
                fields = listOf(
                    SearchField("اسم الموديل", m.name, 4.0),
                    SearchField("وحدة التحكم", m.controlUnit, 2.0),
                    SearchField("الشركة", brand?.name.orEmpty(), 2.0),
                    SearchField("المواصفات", m.specs, 1.0),
                    SearchField("الريموت", m.remoteInfo, 1.0),
                    SearchField("الحساسات", m.sensorsInfo, 1.0),
                    SearchField("ملاحظات", m.notes, 0.8),
                ),
            )
        }
        for (ff in faultDao.faultsFull()) {
            val f = ff.fault
            val model = f.modelId?.let { modelById[it] }
            val fields = ArrayList<SearchField>()
            fields += SearchField("العنوان", f.title, 4.0)
            ff.symptoms.forEach { fields += SearchField("عرض", it.text, 3.0) }
            ff.codes.forEach {
                fields += SearchField("كود", it.code, 5.0, exact = true)
                fields += SearchField("معنى الكود", it.meaning, 1.5)
            }
            fields += SearchField("كلمات بديلة", f.altKeywords, 3.0)
            fields += SearchField("الوصف", f.description, 1.0)
            fields += SearchField("الموديل", model?.name.orEmpty(), 1.5)
            fields += SearchField("الشركة", model?.let { brandById[it.brandId]?.name }.orEmpty(), 1.2)
            ff.solutions.forEach { s ->
                fields += SearchField("حل", s.solution.title, 1.5)
                s.steps.forEach { fields += SearchField("خطوة", it.text, 0.5) }
            }
            fields += SearchField("أدوات", f.tools, 0.5)
            docs += SearchDoc(
                type = SearchType.FAULT, id = f.id, title = f.title,
                subtitle = model?.name ?: "عام",
                categoryId = f.categoryId ?: model?.categoryId,
                brandId = model?.brandId, modelId = f.modelId,
                source = f.source, level = f.level, fields = fields,
            )
        }
        for (gf in guideDao.guidesFull()) {
            val g = gf.guide
            val model = g.modelId?.let { modelById[it] }
            val fields = ArrayList<SearchField>()
            fields += SearchField("العنوان", g.title, 4.0)
            fields += SearchField("الملخص", g.summary, 1.5)
            fields += SearchField("الموديل", model?.name.orEmpty(), 1.5)
            fields += SearchField("الشركة", model?.let { brandById[it.brandId]?.name }.orEmpty(), 1.2)
            gf.steps.forEach { fields += SearchField("خطوة", it.text, 0.6) }
            docs += SearchDoc(
                type = SearchType.GUIDE, id = g.id, title = g.title,
                subtitle = com.hamo.doortech.data.db.GuideType.singular(g.type),
                categoryId = g.categoryId ?: model?.categoryId,
                brandId = model?.brandId, modelId = g.modelId,
                source = g.source, level = g.level, fields = fields,
            )
        }
        docs
    }

    // ===== البيانات التجريبية =====
    /** يحذف كل البيانات المعلّمة كتجريبية فقط. يفشل بأمان إن كانت بيانات المستخدم تعتمد عليها. */
    suspend fun deleteSeedData(): Outcome {
        return try {
            val seedFaults = faultDao.faultsFull().filter { it.fault.isSeed }.map { it.fault.id }
            val seedGuides = guideDao.guidesFull().filter { it.guide.isSeed }.map { it.guide.id }
            val seedJobs = jobDao.jobsFull().filter { it.job.isSeed }.map { it.job.id }
            val seedModels = catalog.modelsOnce().filter { it.isSeed }.map { it.id }
            db.withTransaction {
                seedFaults.forEach { favDao.delete(OwnerType.FAULT, it) }
                seedGuides.forEach { favDao.delete(OwnerType.GUIDE, it) }
                seedModels.forEach { favDao.delete(OwnerType.MODEL, it) }
                faultDao.deleteSeedFaults()
                guideDao.deleteSeedGuides()
                jobDao.deleteSeedJobs()
                catalog.deleteSeedModels()
                catalog.deleteSeedBrands()
                catalog.deleteSeedCategories()
            }
            seedFaults.forEach { deleteAttachmentsOf(OwnerType.FAULT, it) }
            seedGuides.forEach { deleteAttachmentsOf(OwnerType.GUIDE, it) }
            seedJobs.forEach { deleteAttachmentsOf(OwnerType.JOB, it) }
            seedModels.forEach { deleteAttachmentsOf(OwnerType.MODEL, it) }
            Outcome.Ok()
        } catch (e: SQLiteConstraintException) {
            Outcome.Fail("لا يمكن حذف بعض البيانات التجريبية لأن بياناتك الخاصة مرتبطة بها. لم يُحذف شيء.")
        }
    }

    suspend fun hasAnyData(): Boolean = catalog.categoriesOnce().isNotEmpty() || catalog.brandsOnce().isNotEmpty()
}
