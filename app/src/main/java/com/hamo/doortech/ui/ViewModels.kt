package com.hamo.doortech.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hamo.doortech.AppContainer
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.FaultFull
import com.hamo.doortech.data.db.GuideFull
import com.hamo.doortech.data.db.JobFull
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.SparePart
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.repo.AppRepository
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.data.repo.toDraft
import com.hamo.doortech.domain.SearchDoc
import com.hamo.doortech.domain.SearchEngine
import com.hamo.doortech.domain.SearchFilter
import com.hamo.doortech.domain.SearchResult
import com.hamo.doortech.domain.SearchType
import com.hamo.doortech.util.JobReportData
import com.hamo.doortech.util.ReportImage
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ======================= أدوات مشتركة =======================

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun <T> Flow<T>.asUiState(scope: CoroutineScope): StateFlow<UiState<T>> =
    this.map<T, UiState<T>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "حدث خطأ غير متوقع")) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

class VmFactory(private val make: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = make() as T
}

/** خرائط الأسماء لعرض اسم الشركة والتصنيف بجانب العناصر. */
data class NameMaps(
    val categories: Map<Long, String>,
    val brands: Map<Long, String>,
    val models: Map<Long, MachineModel>,
) {
    fun category(id: Long?): String? = id?.let { categories[it] }
    fun model(id: Long?): MachineModel? = id?.let { models[it] }
    fun modelName(id: Long?): String? = model(id)?.name
    fun brandOfModel(id: Long?): String? = model(id)?.let { brands[it.brandId] }
    fun brand(id: Long?): String? = id?.let { brands[it] }
}

fun AppRepository.nameMaps(): Flow<NameMaps> =
    combine(categories, brands, models) { c, b, m ->
        NameMaps(c.associate { it.id to it.name }, b.associate { it.id to it.name }, m.associateBy { it.id })
    }

// ======================= الرئيسية =======================

data class HomeStats(
    val faults: Int, val guides: Int, val models: Int, val jobs: Int, val favorites: Int,
    val recentJobs: List<MaintenanceJob>,
)

class HomeViewModel(repo: AppRepository) : ViewModel() {
    val stats: StateFlow<UiState<HomeStats>> = combine(
        repo.faultCount, repo.guideCount, repo.modelCount, repo.jobCount, repo.favorites,
    ) { f, g, m, j, fav -> arrayOf<Any>(f, g, m, j, fav.size) }
        .combine(repo.jobs) { a, jobs ->
            HomeStats(a[0] as Int, a[1] as Int, a[2] as Int, a[3] as Int, a[4] as Int, jobs.take(3))
        }
        .asUiState(viewModelScope)
}

// ======================= البحث والتشخيص =======================

class SearchViewModel(private val repo: AppRepository, private val fixedTypes: Set<SearchType>) : ViewModel() {
    var query by mutableStateOf("")
        private set
    var categoryId by mutableStateOf<Long?>(null)
        private set
    var brandId by mutableStateOf<Long?>(null)
        private set
    var modelId by mutableStateOf<Long?>(null)
        private set
    var typeFilter by mutableStateOf<SearchType?>(null)
        private set
    var results by mutableStateOf<UiState<List<SearchResult>>>(UiState.Loading)
        private set

    private var docs: List<SearchDoc> = emptyList()
    private var job: Job? = null

    init {
        viewModelScope.launch {
            combine(repo.faults, repo.models, repo.brands, repo.guides) { _, _, _, _ -> 0 }
                .catch { results = UiState.Error(it.message ?: "تعذّر تحميل بيانات البحث") }
                .collect {
                    try {
                        docs = repo.searchDocs()
                        run(immediate = true)
                    } catch (e: Exception) {
                        results = UiState.Error(e.message ?: "تعذّر تحميل بيانات البحث")
                    }
                }
        }
    }

    fun updateQuery(v: String) { query = v; run() }
    fun setCategory(v: Long?) { categoryId = v; run(immediate = true) }
    fun setBrand(v: Long?) {
        brandId = v
        // مسح الموديل إن لم يعد يتبع الشركة المختارة يتم من الواجهة (قائمة الموديلات مفلترة).
        modelId = null
        run(immediate = true)
    }
    fun setModel(v: Long?) { modelId = v; run(immediate = true) }
    fun setType(v: SearchType?) { typeFilter = v; run(immediate = true) }
    fun clearFilters() {
        categoryId = null; brandId = null; modelId = null; typeFilter = null; run(immediate = true)
    }

    private fun run(immediate: Boolean = false) {
        job?.cancel()
        job = viewModelScope.launch {
            if (!immediate) delay(120)
            val types = if (fixedTypes.isNotEmpty()) fixedTypes else typeFilter?.let { setOf(it) } ?: emptySet()
            val filter = SearchFilter(categoryId, brandId, modelId, types)
            val q = query
            val list = withContext(Dispatchers.Default) { SearchEngine.search(docs, q, filter) }
            results = UiState.Success(list)
        }
    }
}

// ======================= الكتالوج: التصنيفات / الشركات / الموديلات =======================

class CatalogViewModel(private val repo: AppRepository) : ViewModel() {
    val categories = repo.categories.asUiState(viewModelScope)
    val brands = repo.brands.asUiState(viewModelScope)
    val models = repo.models.asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)

    private fun run(onDone: (Outcome) -> Unit, block: suspend () -> Outcome) {
        viewModelScope.launch {
            val r = try { block() } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر تنفيذ العملية") }
            onDone(r)
        }
    }

    fun saveCategory(c: DoorCategory, onDone: (Outcome) -> Unit) = run(onDone) { repo.saveCategory(c) }
    fun deleteCategory(c: DoorCategory, onDone: (Outcome) -> Unit) = run(onDone) { repo.deleteCategory(c) }
    fun saveBrand(b: Brand, onDone: (Outcome) -> Unit) = run(onDone) { repo.saveBrand(b) }
    fun deleteBrand(b: Brand, onDone: (Outcome) -> Unit) = run(onDone) { repo.deleteBrand(b) }
    fun deleteModel(m: MachineModel, onDone: (Outcome) -> Unit) = run(onDone) { repo.deleteModel(m) }
}

class ModelEditViewModel(private val repo: AppRepository, private val id: Long, private val presetBrand: Long?) : ViewModel() {
    var form by mutableStateOf<ModelForm?>(null)
        private set
    var missing by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    val categories = repo.categories.asUiState(viewModelScope)
    val brands = repo.brands.asUiState(viewModelScope)

    init {
        viewModelScope.launch {
            if (id == 0L) form = ModelForm(null, presetBrand)
            else {
                val m = repo.model(id)
                if (m == null) missing = true else form = ModelForm(m)
            }
        }
    }

    fun save(onDone: (Outcome) -> Unit) {
        val f = form ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            val r = repo.saveModel(f.toModel())
            saving = false
            onDone(r)
        }
    }
}

class ModelDetail(
    val model: MachineModel, val brandName: String?, val categoryName: String?,
    val faults: List<Fault>, val guides: List<TechnicalGuide>, val parts: List<SparePart>,
)

class ModelDetailViewModel(private val repo: AppRepository, val id: Long) : ViewModel() {
    val detail: StateFlow<UiState<ModelDetail?>> = combine(
        repo.modelFlow(id), repo.nameMaps(), repo.faultsForModel(id), repo.guidesForModel(id), repo.partsForModel(id),
    ) { m, n, f, g, p ->
        m?.let { ModelDetail(it, n.brands[it.brandId], n.category(it.categoryId), f, g, p) }
    }.asUiState(viewModelScope)
    val isFavorite = repo.isFavorite(OwnerType.MODEL, id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavorite() { viewModelScope.launch { repo.toggleFavorite(OwnerType.MODEL, id) } }
    fun delete(onDone: (Outcome) -> Unit) {
        viewModelScope.launch {
            val m = repo.model(id) ?: return@launch onDone(Outcome.Fail("الموديل غير موجود"))
            onDone(repo.deleteModel(m))
        }
    }
}

// ======================= الأعطال =======================

class FaultDetail(val full: FaultFull, val categoryName: String?, val modelName: String?, val brandName: String?)

class FaultDetailViewModel(private val repo: AppRepository, val id: Long) : ViewModel() {
    val detail: StateFlow<UiState<FaultDetail?>> = combine(repo.faultFull(id), repo.nameMaps()) { f, n ->
        f?.let {
            FaultDetail(
                it, n.category(it.fault.categoryId ?: n.model(it.fault.modelId)?.categoryId),
                n.modelName(it.fault.modelId), n.brandOfModel(it.fault.modelId),
            )
        }
    }.asUiState(viewModelScope)
    val isFavorite = repo.isFavorite(OwnerType.FAULT, id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavorite() { viewModelScope.launch { repo.toggleFavorite(OwnerType.FAULT, id) } }
    fun delete(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.deleteFault(id)) } }
    fun duplicate(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.duplicateFault(id)) } }
}

class FaultEditViewModel(private val repo: AppRepository, private val id: Long, private val presetModel: Long?) : ViewModel() {
    var form by mutableStateOf<FaultForm?>(null)
        private set
    var missing by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    val names = repo.nameMaps().asUiState(viewModelScope)

    init {
        viewModelScope.launch {
            if (id == 0L) {
                val cat = presetModel?.let { repo.model(it)?.categoryId }
                form = FaultForm(null, presetModel, cat)
            } else {
                val full = repo.faultFullOnce(id)
                if (full == null) missing = true else form = FaultForm(full.toDraft())
            }
        }
    }

    fun save(onDone: (Outcome) -> Unit) {
        val f = form ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            val r = repo.saveFault(f.toDraft())
            saving = false
            onDone(r)
        }
    }
}

// ======================= الأدلة =======================

class GuideListViewModel(repo: AppRepository, type: String) : ViewModel() {
    val guides = repo.guidesByType(type).asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)
}

class GuideDetail(val full: GuideFull, val categoryName: String?, val modelName: String?, val brandName: String?)

class GuideDetailViewModel(private val repo: AppRepository, val id: Long) : ViewModel() {
    val detail: StateFlow<UiState<GuideDetail?>> = combine(repo.guideFull(id), repo.nameMaps()) { g, n ->
        g?.let {
            GuideDetail(
                it, n.category(it.guide.categoryId ?: n.model(it.guide.modelId)?.categoryId),
                n.modelName(it.guide.modelId), n.brandOfModel(it.guide.modelId),
            )
        }
    }.asUiState(viewModelScope)
    val isFavorite = repo.isFavorite(OwnerType.GUIDE, id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavorite() { viewModelScope.launch { repo.toggleFavorite(OwnerType.GUIDE, id) } }
    fun delete(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.deleteGuide(id)) } }
    fun duplicate(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.duplicateGuide(id)) } }
}

class GuideEditViewModel(private val repo: AppRepository, private val id: Long, private val type: String) : ViewModel() {
    var form by mutableStateOf<GuideForm?>(null)
        private set
    var missing by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    val names = repo.nameMaps().asUiState(viewModelScope)

    init {
        viewModelScope.launch {
            if (id == 0L) form = GuideForm(null, type)
            else {
                val full = repo.guideFullOnce(id)
                if (full == null) missing = true else form = GuideForm(full.toDraft(), full.guide.type)
            }
        }
    }

    fun save(onDone: (Outcome) -> Unit) {
        val f = form ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            val r = repo.saveGuide(f.toDraft())
            saving = false
            onDone(r)
        }
    }
}

// ======================= سجل الصيانة =======================

class JobsViewModel(repo: AppRepository) : ViewModel() {
    val jobs = repo.jobs.asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)
}

class JobDetail(val full: JobFull, val categoryName: String?, val brandName: String?, val modelName: String?)

class JobDetailViewModel(private val container: AppContainer, val id: Long) : ViewModel() {
    private val repo = container.repo
    val detail: StateFlow<UiState<JobDetail?>> = combine(repo.jobFull(id), repo.nameMaps()) { j, n ->
        j?.let { JobDetail(it, n.category(it.job.categoryId), n.brand(it.job.brandId) ?: n.brandOfModel(it.job.modelId), n.modelName(it.job.modelId)) }
    }.asUiState(viewModelScope)

    var exporting by mutableStateOf(false)
        private set

    fun delete(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.deleteJob(id)) } }
    fun duplicate(onDone: (Outcome) -> Unit) { viewModelScope.launch { onDone(repo.duplicateJob(id)) } }

    /** يُنشئ تقرير PDF ويعيد الملف (أو رسالة خطأ). */
    fun createReport(onDone: (File?, String?) -> Unit) {
        if (exporting) return
        exporting = true
        viewModelScope.launch {
            try {
                val full = repo.jobFullOnce(id)
                if (full == null) { onDone(null, "الزيارة غير موجودة"); return@launch }
                val maps = repo.nameMaps().first()
                val media = repo.mediaOnce(OwnerType.JOB, id)
                val images = media.filter { it.kind == com.hamo.doortech.data.db.MediaKind.IMAGE }
                    .sortedBy { if (it.role == MediaRole.BEFORE) 0 else if (it.role == MediaRole.AFTER) 1 else 2 }
                    .map { ReportImage(it.role, container.mediaStore.fileFor(it.path)) }
                val data = JobReportData(
                    job = full.job, parts = full.parts,
                    categoryName = maps.category(full.job.categoryId),
                    brandName = maps.brand(full.job.brandId) ?: maps.brandOfModel(full.job.modelId),
                    modelName = maps.modelName(full.job.modelId),
                    images = images,
                )
                val file = withContext(Dispatchers.IO) { container.pdf.create(data) }
                onDone(file, null)
            } catch (e: Exception) {
                onDone(null, "تعذّر إنشاء التقرير: ${e.message ?: "خطأ غير معروف"}")
            } finally {
                exporting = false
            }
        }
    }
}

class JobEditViewModel(private val repo: AppRepository, private val id: Long) : ViewModel() {
    var form by mutableStateOf<JobForm?>(null)
        private set
    var missing by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    val names = repo.nameMaps().asUiState(viewModelScope)
    val parts = repo.parts.asUiState(viewModelScope)

    init {
        viewModelScope.launch {
            if (id == 0L) form = JobForm(null)
            else {
                val full = repo.jobFullOnce(id)
                if (full == null) missing = true else form = JobForm(full.toDraft())
            }
        }
    }

    fun save(onDone: (Outcome) -> Unit) {
        val f = form ?: return
        if (saving) return
        val (draft, err) = f.toDraft()
        if (draft == null) { onDone(Outcome.Fail(err ?: "بيانات غير صالحة")); return }
        saving = true
        viewModelScope.launch {
            val r = repo.saveJob(draft)
            saving = false
            onDone(r)
        }
    }
}

// ======================= قطع الغيار / المفضلة =======================

class PartsViewModel(private val repo: AppRepository) : ViewModel() {
    val parts = repo.parts.asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)

    fun save(p: SparePart, onDone: (Outcome) -> Unit) {
        viewModelScope.launch { onDone(try { repo.savePart(p) } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر الحفظ") }) }
    }

    fun delete(p: SparePart, onDone: (Outcome) -> Unit) {
        viewModelScope.launch { onDone(try { repo.deletePart(p) } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر الحذف") }) }
    }
}

data class FavItem(val type: String, val id: Long, val title: String, val subtitle: String)

class FavoritesViewModel(private val repo: AppRepository) : ViewModel() {
    val items: StateFlow<UiState<List<FavItem>>> = combine(repo.favorites, repo.faults, repo.models, repo.guides, repo.brands) { favs, faults, models, guides, brands ->
        val bm = brands.associate { it.id to it.name }
        val fm = faults.associateBy { it.id }
        val mm = models.associateBy { it.id }
        val gm = guides.associateBy { it.id }
        favs.mapNotNull { f ->
            when (f.targetType) {
                OwnerType.FAULT -> fm[f.targetId]?.let { FavItem(f.targetType, it.id, it.title, "عطل") }
                OwnerType.MODEL -> mm[f.targetId]?.let { FavItem(f.targetType, it.id, it.name, bm[it.brandId] ?: "موديل") }
                OwnerType.GUIDE -> gm[f.targetId]?.let { FavItem(f.targetType, it.id, it.title, "دليل") }
                else -> null
            }
        }
    }.asUiState(viewModelScope)

    fun remove(item: FavItem) { viewModelScope.launch { repo.toggleFavorite(item.type, item.id) } }
}

// ======================= المرفقات =======================

class MediaViewModel(private val repo: AppRepository, val ownerType: String, val ownerId: Long) : ViewModel() {
    val media = repo.mediaFor(ownerType, ownerId).asUiState(viewModelScope)

    private fun run(onDone: (Outcome) -> Unit, block: suspend () -> Outcome) {
        viewModelScope.launch {
            onDone(try { block() } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر تنفيذ العملية") })
        }
    }

    fun addImage(uri: Uri, role: String, onDone: (Outcome) -> Unit) = run(onDone) { repo.addImage(ownerType, ownerId, uri, role) }
    fun addCamera(file: File, role: String, onDone: (Outcome) -> Unit) = run(onDone) { repo.addCameraImage(ownerType, ownerId, file, role) }
    fun addDocument(uri: Uri, onDone: (Outcome) -> Unit) = run(onDone) { repo.addDocument(ownerType, ownerId, uri) }
    fun addLink(url: String, title: String, onDone: (Outcome) -> Unit) = run(onDone) { repo.addVideoLink(ownerType, ownerId, url, title) }
    fun delete(m: MediaAttachment, onDone: (Outcome) -> Unit) = run(onDone) { repo.deleteMedia(m.id) }
}

class AllMediaViewModel(private val repo: AppRepository) : ViewModel() {
    val media = repo.allMedia.asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)
    val faults = repo.faults.asUiState(viewModelScope)

    fun delete(m: MediaAttachment, onDone: (Outcome) -> Unit) {
        viewModelScope.launch { onDone(repo.deleteMedia(m.id)) }
    }
}

// ======================= النسخ الاحتياطي والإعدادات =======================

class BackupViewModel(private val container: AppContainer) : ViewModel() {
    var busy by mutableStateOf(false)
        private set
    var pendingPreview by mutableStateOf<com.hamo.doortech.data.repo.BackupPreview?>(null)
        private set
    private var pendingFile: File? = null

    fun export(context: Context, target: Uri, onDone: (Outcome) -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            val r = try {
                val out = context.contentResolver.openOutputStream(target)
                    ?: return@launch onDone(Outcome.Fail("تعذّر فتح مكان الحفظ")).also { busy = false }
                container.backup.export(out)
            } catch (e: Exception) {
                Outcome.Fail("فشل التصدير: ${e.message ?: "خطأ غير معروف"}")
            }
            busy = false
            onDone(r)
        }
    }

    /** يقرأ الملف المختار إلى مجلد مؤقت ثم يفحص سلامته ويعرض الملخص قبل الدمج. */
    fun inspect(context: Context, source: Uri, onDone: (Outcome) -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val dir = File(context.cacheDir, "backups").also { it.mkdirs() }
                    val tmp = File(dir, "import_${System.currentTimeMillis()}.zip")
                    val ins = context.contentResolver.openInputStream(source)
                        ?: return@withContext Pair<File?, String?>(null, "تعذّر فتح الملف")
                    ins.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
                    Pair<File?, String?>(tmp, null)
                } catch (e: Exception) {
                    Pair<File?, String?>(null, "تعذّر قراءة الملف: ${e.message ?: ""}")
                }
            }
            val file = result.first
            if (file == null) { busy = false; onDone(Outcome.Fail(result.second ?: "تعذّر قراءة الملف")); return@launch }
            when (val r = container.backup.inspect(file)) {
                is com.hamo.doortech.data.repo.InspectResult.Invalid -> {
                    file.delete(); busy = false; onDone(Outcome.Fail(r.reason))
                }
                is com.hamo.doortech.data.repo.InspectResult.Valid -> {
                    pendingFile?.delete()
                    pendingFile = file
                    pendingPreview = r.preview
                    busy = false
                    onDone(Outcome.Ok())
                }
            }
        }
    }

    fun confirmImport(onDone: (Outcome) -> Unit) {
        val file = pendingFile ?: return
        if (busy) return
        busy = true
        viewModelScope.launch {
            val r = container.backup.import(file)
            busy = false
            if (r is Outcome.Ok) cancelImport()
            onDone(r)
        }
    }

    fun cancelImport() {
        pendingFile?.delete()
        pendingFile = null
        pendingPreview = null
    }

    override fun onCleared() {
        pendingFile?.delete()
        super.onCleared()
    }
}

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.repo
    val stats = HomeViewModelStats(repo).stats.asUiState(viewModelScope)
    var busy by mutableStateOf(false)
        private set

    fun deleteSeed(onDone: (Outcome) -> Unit) {
        viewModelScope.launch {
            busy = true
            val r = try { repo.deleteSeedData() } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر الحذف") }
            busy = false
            onDone(r)
        }
    }

    fun reloadSeed(onDone: (Outcome) -> Unit) {
        viewModelScope.launch {
            busy = true
            val r = try { container.reloadSeed(); Outcome.Ok(message = "تمت إضافة البيانات التجريبية") } catch (e: Exception) { Outcome.Fail(e.message ?: "تعذّر التحميل") }
            busy = false
            onDone(r)
        }
    }
}

class HomeViewModelStats(repo: AppRepository) {
    val stats: Flow<Map<String, Int>> = combine(
        repo.categories, repo.brands, repo.models, repo.faults, repo.guides,
    ) { c, b, m, f, g -> mapOf("التصنيفات" to c.size, "الشركات" to b.size, "الموديلات" to m.size, "الأعطال" to f.size, "الأدلة" to g.size) }
        .combine(repo.jobs) { base, jobs -> base + ("زيارات الصيانة" to jobs.size) }
        .combine(repo.parts) { base, parts -> base + ("قطع الغيار" to parts.size) }
}

// ======================= قائمة الأعطال =======================

class FaultListViewModel(repo: AppRepository) : ViewModel() {
    val faults = repo.faults.asUiState(viewModelScope)
    val names = repo.nameMaps().asUiState(viewModelScope)
}
