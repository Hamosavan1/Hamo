package com.hamo.doortech.data.repo

import androidx.room.withTransaction
import com.google.gson.Gson
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.ErrorCode
import com.hamo.doortech.data.db.Favorite
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.FaultSymptom
import com.hamo.doortech.data.db.GuideStep
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.Solution
import com.hamo.doortech.data.db.SolutionStep
import com.hamo.doortech.data.db.SparePart
import com.hamo.doortech.data.db.TechnicalGuide
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BackupManifest(
    val format: String = BackupManager.FORMAT,
    val version: Int = BackupManager.VERSION,
    val createdAt: Long = 0,
    val dataSha256: String = "",
    val mediaSha256: Map<String, String> = emptyMap(),
    val counts: Map<String, Int> = emptyMap(),
)

data class BackupData(
    val categories: List<DoorCategory> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val models: List<MachineModel> = emptyList(),
    val faults: List<Fault> = emptyList(),
    val symptoms: List<FaultSymptom> = emptyList(),
    val codes: List<ErrorCode> = emptyList(),
    val solutions: List<Solution> = emptyList(),
    val solutionSteps: List<SolutionStep> = emptyList(),
    val guides: List<TechnicalGuide> = emptyList(),
    val guideSteps: List<GuideStep> = emptyList(),
    val parts: List<SparePart> = emptyList(),
    val jobs: List<MaintenanceJob> = emptyList(),
    val jobParts: List<JobPart> = emptyList(),
    val media: List<MediaAttachment> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
)

data class BackupPreview(val manifest: BackupManifest, val data: BackupData)

sealed interface InspectResult {
    data class Valid(val preview: BackupPreview) : InspectResult
    data class Invalid(val reason: String) : InspectResult
}

data class ImportReport(val added: Map<String, Int>, val skippedDuplicates: Int, val message: String)

/**
 * النسخ الاحتياطي والاستعادة: ملف ZIP يحوي manifest.json (مع بصمات SHA-256)، data.json، ومجلد media/.
 * الاستعادة تفحص السلامة أولًا ثم تدمج الجديد فقط؛ لا تستبدل ولا تحذف أي بيانات موجودة.
 */
class BackupManager(private val repo: AppRepository) {
    private val gson = Gson()
    private val db = repo.db
    private val store = repo.mediaStore

    companion object {
        const val FORMAT = "hamo-door-tech-backup"
        const val VERSION = 1
        private const val MAX_JSON_BYTES = 64L * 1024 * 1024
    }

    suspend fun collect(): BackupData = withContext(Dispatchers.IO) {
        val faults = db.faultDao().faultsFull()
        val guides = db.guideDao().guidesFull()
        val jobs = db.jobDao().jobsFull()
        BackupData(
            categories = db.catalogDao().categoriesOnce(),
            brands = db.catalogDao().brandsOnce(),
            models = db.catalogDao().modelsOnce(),
            faults = faults.map { it.fault },
            symptoms = faults.flatMap { it.symptoms },
            codes = faults.flatMap { it.codes },
            solutions = faults.flatMap { f -> f.solutions.map { it.solution } },
            solutionSteps = faults.flatMap { f -> f.solutions.flatMap { it.steps } },
            guides = guides.map { it.guide },
            guideSteps = guides.flatMap { it.steps },
            parts = db.catalogDao().partsOnce(),
            jobs = jobs.map { it.job },
            jobParts = jobs.flatMap { it.parts },
            media = db.mediaDao().allOnce(),
            favorites = db.favoriteDao().allOnce(),
        )
    }

    fun countsOf(d: BackupData): Map<String, Int> = linkedMapOf(
        "التصنيفات" to d.categories.size,
        "الشركات" to d.brands.size,
        "الموديلات" to d.models.size,
        "الأعطال" to d.faults.size,
        "الأدلة" to d.guides.size,
        "قطع الغيار" to d.parts.size,
        "زيارات الصيانة" to d.jobs.size,
        "المرفقات" to d.media.size,
        "المفضلة" to d.favorites.size,
    )

    /** يكتب النسخة الاحتياطية إلى [out] ويعيد ملخصًا. */
    suspend fun export(out: OutputStream): Outcome = withContext(Dispatchers.IO) {
        try {
            val data = collect()
            val json = gson.toJson(data).toByteArray(Charsets.UTF_8)
            val mediaNames = data.media.filter { it.kind != MediaKind.LINK }.map { it.path }.distinct()
            val present = mediaNames.filter { store.exists(it) }
            val missing = mediaNames.size - present.size
            val hashes = present.associateWith { sha256(store.fileFor(it).inputStream()) }
            val manifest = BackupManifest(
                createdAt = System.currentTimeMillis(),
                dataSha256 = sha256(json),
                mediaSha256 = hashes,
                counts = countsOf(data),
            )
            ZipOutputStream(out.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(gson.toJson(manifest).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(json)
                zip.closeEntry()
                present.forEach { name ->
                    zip.putNextEntry(ZipEntry("media/$name"))
                    store.fileFor(name).inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            val note = if (missing > 0) " (تنبيه: $missing مرفق مفقود من التخزين لم يُضمَّن)" else ""
            Outcome.Ok(message = "تم التصدير: ${present.size} مرفق و${data.faults.size} عطل و${data.jobs.size} زيارة$note")
        } catch (e: Exception) {
            Outcome.Fail("فشل التصدير: ${e.message ?: "خطأ غير معروف"}")
        }
    }

    /** يفحص ملف النسخة الاحتياطية دون تغيير أي بيانات. */
    suspend fun inspect(file: File): InspectResult = withContext(Dispatchers.IO) {
        try {
            ZipFile(file).use { zip ->
                val manifestEntry = zip.getEntry("manifest.json")
                    ?: return@withContext InspectResult.Invalid("الملف ليس نسخة احتياطية من التطبيق (manifest مفقود)")
                val dataEntry = zip.getEntry("data.json")
                    ?: return@withContext InspectResult.Invalid("ملف البيانات data.json مفقود")
                if (manifestEntry.size > MAX_JSON_BYTES || dataEntry.size > MAX_JSON_BYTES) {
                    return@withContext InspectResult.Invalid("حجم الملف غير معقول")
                }
                val manifest = try {
                    gson.fromJson(zip.getInputStream(manifestEntry).reader(Charsets.UTF_8), BackupManifest::class.java)
                } catch (e: Exception) {
                    null
                } ?: return@withContext InspectResult.Invalid("ملف manifest تالف")
                if (manifest.format != FORMAT) return@withContext InspectResult.Invalid("صيغة الملف غير معروفة")
                if (manifest.version > VERSION) {
                    return@withContext InspectResult.Invalid("النسخة أحدث من هذا التطبيق (الإصدار ${manifest.version})، حدّث التطبيق أولًا")
                }
                val bytes = zip.getInputStream(dataEntry).use { it.readBytes() }
                if (sha256(bytes) != manifest.dataSha256) {
                    return@withContext InspectResult.Invalid("البيانات تالفة أو تم تعديلها (بصمة SHA-256 غير مطابقة)")
                }
                val data = try {
                    gson.fromJson(String(bytes, Charsets.UTF_8), BackupData::class.java)
                } catch (e: Exception) {
                    null
                } ?: return@withContext InspectResult.Invalid("تعذّرت قراءة البيانات")
                for ((name, hash) in manifest.mediaSha256) {
                    if (name.contains('/') || name.contains("..") || name.isBlank()) {
                        return@withContext InspectResult.Invalid("اسم مرفق غير آمن في الملف")
                    }
                    val e = zip.getEntry("media/$name")
                        ?: return@withContext InspectResult.Invalid("المرفق $name مفقود من النسخة")
                    if (sha256(zip.getInputStream(e)) != hash) {
                        return@withContext InspectResult.Invalid("المرفق $name تالف (بصمة غير مطابقة)")
                    }
                }
                val referenced = data.media.filter { it.kind != MediaKind.LINK }.map { it.path }.toSet()
                val unsafe = referenced.firstOrNull { it.contains('/') || it.contains("..") }
                if (unsafe != null) return@withContext InspectResult.Invalid("مسار مرفق غير آمن")
                InspectResult.Valid(BackupPreview(manifest, data))
            }
        } catch (e: Exception) {
            InspectResult.Invalid("الملف غير صالح كنسخة احتياطية (${e.message ?: "ZIP تالف"})")
        }
    }

    /**
     * يدمج محتوى النسخة مع البيانات الحالية. التصنيفات والشركات والموديلات المتطابقة تُدمج،
     * والأعطال والأدلة والزيارات المتطابقة تُتخطى، والباقي يُضاف كجديد. لا حذف ولا استبدال.
     */
    suspend fun import(file: File): Outcome = withContext(Dispatchers.IO) {
        val preview = when (val r = inspect(file)) {
            is InspectResult.Invalid -> return@withContext Outcome.Fail(r.reason)
            is InspectResult.Valid -> r.preview
        }
        val d = preview.data
        val copiedFiles = ArrayList<String>()
        try {
            ZipFile(file).use { zip ->
                var skipped = 0
                val added = linkedMapOf<String, Int>()
                fun bump(k: String) { added[k] = (added[k] ?: 0) + 1 }

                db.withTransaction {
                    val cat = db.catalogDao()
                    val fdao = db.faultDao()
                    val gdao = db.guideDao()
                    val jdao = db.jobDao()

                    val catMap = HashMap<Long, Long>()
                    for (c in d.categories) {
                        val existing = cat.categoryByName(c.name)
                        if (existing != null) catMap[c.id] = existing.id
                        else { catMap[c.id] = cat.insertCategory(c.copy(id = 0)); bump("التصنيفات") }
                    }
                    val brandMap = HashMap<Long, Long>()
                    for (b in d.brands) {
                        val existing = cat.brandByName(b.name)
                        if (existing != null) brandMap[b.id] = existing.id
                        else { brandMap[b.id] = cat.insertBrand(b.copy(id = 0)); bump("الشركات") }
                    }
                    val modelMap = HashMap<Long, Long>()
                    for (m in d.models) {
                        val newBrand = brandMap[m.brandId] ?: continue
                        val existing = cat.modelByBrandAndName(newBrand, m.name)
                        if (existing != null) modelMap[m.id] = existing.id
                        else {
                            modelMap[m.id] = cat.insertModel(
                                m.copy(id = 0, brandId = newBrand, categoryId = m.categoryId?.let { catMap[it] })
                            )
                            bump("الموديلات")
                        }
                    }

                    // الأعطال
                    val existingFaultKeys = fdao.faultsFull().map { faultKey(it.fault.title, it.fault.modelId) }.toMutableSet()
                    val faultMap = HashMap<Long, Long>()
                    val newFaultIds = HashSet<Long>()
                    for (f in d.faults) {
                        val newModel = f.modelId?.let { modelMap[it] }
                        val key = faultKey(f.title, newModel)
                        if (key in existingFaultKeys) { skipped++; continue }
                        existingFaultKeys += key
                        val nid = fdao.insertFault(f.copy(id = 0, categoryId = f.categoryId?.let { catMap[it] }, modelId = newModel))
                        faultMap[f.id] = nid; newFaultIds += f.id; bump("الأعطال")
                    }
                    d.symptoms.filter { it.faultId in newFaultIds }.forEach {
                        fdao.insertSymptom(it.copy(id = 0, faultId = faultMap.getValue(it.faultId)))
                    }
                    d.codes.filter { it.faultId in newFaultIds }.forEach {
                        fdao.insertCode(it.copy(id = 0, faultId = faultMap.getValue(it.faultId)))
                    }
                    val solMap = HashMap<Long, Long>()
                    d.solutions.filter { it.faultId in newFaultIds }.forEach {
                        solMap[it.id] = fdao.insertSolution(it.copy(id = 0, faultId = faultMap.getValue(it.faultId)))
                    }
                    d.solutionSteps.filter { it.solutionId in solMap }.forEach {
                        fdao.insertSolutionStep(it.copy(id = 0, solutionId = solMap.getValue(it.solutionId)))
                    }

                    // الأدلة
                    val existingGuideKeys = gdao.guidesFull().map { guideKey(it.guide.type, it.guide.title, it.guide.modelId) }.toMutableSet()
                    val guideMap = HashMap<Long, Long>()
                    for (g in d.guides) {
                        val newModel = g.modelId?.let { modelMap[it] }
                        val key = guideKey(g.type, g.title, newModel)
                        if (key in existingGuideKeys) { skipped++; continue }
                        existingGuideKeys += key
                        guideMap[g.id] = gdao.insertGuide(g.copy(id = 0, categoryId = g.categoryId?.let { catMap[it] }, modelId = newModel))
                        bump("الأدلة")
                    }
                    d.guideSteps.filter { it.guideId in guideMap }.forEach {
                        gdao.insertStep(it.copy(id = 0, guideId = guideMap.getValue(it.guideId)))
                    }

                    // قطع الغيار
                    val existingPartKeys = cat.partsOnce().map { partKey(it.name, it.partNumber, it.modelId) }.toMutableSet()
                    for (p in d.parts) {
                        val newModel = p.modelId?.let { modelMap[it] }
                        val key = partKey(p.name, p.partNumber, newModel)
                        if (key in existingPartKeys) { skipped++; continue }
                        existingPartKeys += key
                        cat.insertPart(p.copy(id = 0, brandId = p.brandId?.let { brandMap[it] }, modelId = newModel))
                        bump("قطع الغيار")
                    }

                    // الزيارات
                    val existingJobKeys = jdao.jobsFull().map { jobKey(it.job.customerName, it.job.visitDate, it.job.problem) }.toMutableSet()
                    val jobMap = HashMap<Long, Long>()
                    for (j in d.jobs) {
                        val key = jobKey(j.customerName, j.visitDate, j.problem)
                        if (key in existingJobKeys) { skipped++; continue }
                        existingJobKeys += key
                        jobMap[j.id] = jdao.insertJob(
                            j.copy(
                                id = 0, categoryId = j.categoryId?.let { catMap[it] },
                                brandId = j.brandId?.let { brandMap[it] }, modelId = j.modelId?.let { modelMap[it] },
                            )
                        )
                        bump("زيارات الصيانة")
                    }
                    d.jobParts.filter { it.jobId in jobMap }.forEach {
                        jdao.insertJobPart(it.copy(id = 0, jobId = jobMap.getValue(it.jobId), partId = null))
                    }

                    // المرفقات (فقط لما أُضيف جديدًا)
                    fun newOwner(type: String, id: Long): Long? = when (type) {
                        OwnerType.FAULT -> if (id in newFaultIds) faultMap[id] else null
                        OwnerType.GUIDE -> guideMap[id]
                        OwnerType.JOB -> jobMap[id]
                        OwnerType.MODEL -> modelMap[id]
                        else -> null
                    }
                    for (m in d.media) {
                        val owner = newOwner(m.ownerType, m.ownerId) ?: continue
                        // الموديلات المدموجة مع موجودة سابقًا لا نكرر مرفقاتها
                        if (m.ownerType == OwnerType.MODEL && m.ownerId !in modelMap) continue
                        val path = if (m.kind == MediaKind.LINK) {
                            m.path
                        } else {
                            val entry = zip.getEntry("media/${m.path}") ?: continue
                            val saved = zip.getInputStream(entry).use { store.copyFromStream(it, m.path) } ?: continue
                            copiedFiles += saved
                            saved
                        }
                        db.mediaDao().insert(m.copy(id = 0, ownerId = owner, path = path))
                        bump("المرفقات")
                    }

                    // المفضلة
                    for (f in d.favorites) {
                        val target = when (f.targetType) {
                            OwnerType.FAULT -> faultMap[f.targetId] ?: findExistingFault(d, f.targetId, modelMap, fdao)
                            OwnerType.GUIDE -> guideMap[f.targetId]
                            OwnerType.MODEL -> modelMap[f.targetId]
                            else -> null
                        } ?: continue
                        if (db.favoriteDao().insert(f.copy(id = 0, targetId = target)) > 0) bump("المفضلة")
                    }
                }

                val msg = if (added.isEmpty()) "لا توجد بيانات جديدة؛ كل المحتوى موجود مسبقًا"
                else "تمت الإضافة: " + added.entries.joinToString("، ") { "${it.key} ${it.value}" } +
                    if (skipped > 0) " — وتم تخطي $skipped عنصر مكرر" else ""
                Outcome.Ok(message = msg)
            }
        } catch (e: Exception) {
            copiedFiles.forEach { store.delete(it) }
            Outcome.Fail("فشلت الاستعادة ولم يتغير شيء: ${e.message ?: "خطأ غير معروف"}")
        }
    }

    private suspend fun findExistingFault(
        d: BackupData, oldId: Long, modelMap: Map<Long, Long>, fdao: com.hamo.doortech.data.db.FaultDao,
    ): Long? {
        val f = d.faults.firstOrNull { it.id == oldId } ?: return null
        val key = faultKey(f.title, f.modelId?.let { modelMap[it] })
        return fdao.faultsFull().firstOrNull { faultKey(it.fault.title, it.fault.modelId) == key }?.fault?.id
    }

    private fun faultKey(title: String, modelId: Long?) = "${title.trim().lowercase()}|${modelId ?: 0}"
    private fun guideKey(type: String, title: String, modelId: Long?) = "$type|${title.trim().lowercase()}|${modelId ?: 0}"
    private fun partKey(name: String, number: String, modelId: Long?) = "${name.trim().lowercase()}|${number.trim().lowercase()}|${modelId ?: 0}"
    private fun jobKey(customer: String, visit: Long, problem: String) = "${customer.trim().lowercase()}|$visit|${problem.trim().lowercase()}"

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun sha256(input: InputStream): String = input.use {
        val md = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(8192)
        while (true) {
            val n = it.read(buf)
            if (n < 0) break
            md.update(buf, 0, n)
        }
        md.digest().joinToString("") { b -> "%02x".format(b) }
    }
}
