package com.hamo.doortech.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class SolutionWithSteps(
    @Embedded val solution: Solution,
    @Relation(parentColumn = "id", entityColumn = "solutionId")
    val steps: List<SolutionStep>,
) {
    val orderedSteps: List<SolutionStep> get() = steps.sortedBy { it.position }
}

data class FaultFull(
    @Embedded val fault: Fault,
    @Relation(parentColumn = "id", entityColumn = "faultId")
    val symptoms: List<FaultSymptom>,
    @Relation(parentColumn = "id", entityColumn = "faultId")
    val codes: List<ErrorCode>,
    @Relation(entity = Solution::class, parentColumn = "id", entityColumn = "faultId")
    val solutions: List<SolutionWithSteps>,
)

data class GuideFull(
    @Embedded val guide: TechnicalGuide,
    @Relation(parentColumn = "id", entityColumn = "guideId")
    val steps: List<GuideStep>,
) {
    val orderedSteps: List<GuideStep> get() = steps.sortedBy { it.position }
}

data class JobFull(
    @Embedded val job: MaintenanceJob,
    @Relation(parentColumn = "id", entityColumn = "jobId")
    val parts: List<JobPart>,
)

@Dao
interface CatalogDao {
    // ---- التصنيفات ----
    @Query("SELECT * FROM door_categories ORDER BY name COLLATE NOCASE")
    fun categories(): Flow<List<DoorCategory>>

    @Query("SELECT * FROM door_categories ORDER BY name COLLATE NOCASE")
    suspend fun categoriesOnce(): List<DoorCategory>

    @Query("SELECT * FROM door_categories WHERE id = :id")
    suspend fun category(id: Long): DoorCategory?

    @Query("SELECT * FROM door_categories WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun categoryByName(name: String): DoorCategory?

    @Insert
    suspend fun insertCategory(c: DoorCategory): Long

    @Update
    suspend fun updateCategory(c: DoorCategory)

    @Delete
    suspend fun deleteCategory(c: DoorCategory)

    @Query(
        "SELECT (SELECT COUNT(*) FROM machine_models WHERE categoryId = :id) + " +
            "(SELECT COUNT(*) FROM faults WHERE categoryId = :id) + " +
            "(SELECT COUNT(*) FROM technical_guides WHERE categoryId = :id) + " +
            "(SELECT COUNT(*) FROM maintenance_jobs WHERE categoryId = :id)"
    )
    suspend fun categoryUsage(id: Long): Int

    // ---- الشركات ----
    @Query("SELECT * FROM brands ORDER BY name COLLATE NOCASE")
    fun brands(): Flow<List<Brand>>

    @Query("SELECT * FROM brands ORDER BY name COLLATE NOCASE")
    suspend fun brandsOnce(): List<Brand>

    @Query("SELECT * FROM brands WHERE id = :id")
    suspend fun brand(id: Long): Brand?

    @Query("SELECT * FROM brands WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun brandByName(name: String): Brand?

    @Insert
    suspend fun insertBrand(b: Brand): Long

    @Update
    suspend fun updateBrand(b: Brand)

    @Delete
    suspend fun deleteBrand(b: Brand)

    @Query(
        "SELECT (SELECT COUNT(*) FROM machine_models WHERE brandId = :id) + " +
            "(SELECT COUNT(*) FROM spare_parts WHERE brandId = :id) + " +
            "(SELECT COUNT(*) FROM maintenance_jobs WHERE brandId = :id)"
    )
    suspend fun brandUsage(id: Long): Int

    // ---- الموديلات ----
    @Query("SELECT * FROM machine_models ORDER BY name COLLATE NOCASE")
    fun models(): Flow<List<MachineModel>>

    @Query("SELECT * FROM machine_models ORDER BY name COLLATE NOCASE")
    suspend fun modelsOnce(): List<MachineModel>

    @Query("SELECT * FROM machine_models WHERE id = :id")
    fun modelFlow(id: Long): Flow<MachineModel?>

    @Query("SELECT * FROM machine_models WHERE id = :id")
    suspend fun model(id: Long): MachineModel?

    @Query("SELECT * FROM machine_models WHERE brandId = :brandId AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun modelByBrandAndName(brandId: Long, name: String): MachineModel?

    @Insert
    suspend fun insertModel(m: MachineModel): Long

    @Update
    suspend fun updateModel(m: MachineModel)

    @Delete
    suspend fun deleteModel(m: MachineModel)

    @Query(
        "SELECT (SELECT COUNT(*) FROM faults WHERE modelId = :id) + " +
            "(SELECT COUNT(*) FROM technical_guides WHERE modelId = :id) + " +
            "(SELECT COUNT(*) FROM maintenance_jobs WHERE modelId = :id) + " +
            "(SELECT COUNT(*) FROM spare_parts WHERE modelId = :id)"
    )
    suspend fun modelUsage(id: Long): Int

    // ---- قطع الغيار ----
    @Query("SELECT * FROM spare_parts ORDER BY name COLLATE NOCASE")
    fun parts(): Flow<List<SparePart>>

    @Query("SELECT * FROM spare_parts ORDER BY name COLLATE NOCASE")
    suspend fun partsOnce(): List<SparePart>

    @Query("SELECT * FROM spare_parts WHERE modelId = :modelId ORDER BY name COLLATE NOCASE")
    fun partsForModel(modelId: Long): Flow<List<SparePart>>

    @Insert
    suspend fun insertPart(p: SparePart): Long

    @Update
    suspend fun updatePart(p: SparePart)

    @Delete
    suspend fun deletePart(p: SparePart)

    @Query("DELETE FROM door_categories WHERE isSeed = 1")
    suspend fun deleteSeedCategories()

    @Query("DELETE FROM brands WHERE isSeed = 1")
    suspend fun deleteSeedBrands()

    @Query("DELETE FROM machine_models WHERE isSeed = 1")
    suspend fun deleteSeedModels()
}

@Dao
interface FaultDao {
    @Query("SELECT * FROM faults ORDER BY title COLLATE NOCASE")
    fun faults(): Flow<List<Fault>>

    @Transaction
    @Query("SELECT * FROM faults ORDER BY title COLLATE NOCASE")
    suspend fun faultsFull(): List<FaultFull>

    @Transaction
    @Query("SELECT * FROM faults WHERE id = :id")
    fun faultFull(id: Long): Flow<FaultFull?>

    @Transaction
    @Query("SELECT * FROM faults WHERE id = :id")
    suspend fun faultFullOnce(id: Long): FaultFull?

    @Query("SELECT * FROM faults WHERE modelId = :modelId ORDER BY title COLLATE NOCASE")
    fun faultsForModel(modelId: Long): Flow<List<Fault>>

    @Query("SELECT * FROM faults WHERE id = :id")
    suspend fun fault(id: Long): Fault?

    @Insert
    suspend fun insertFault(f: Fault): Long

    @Update
    suspend fun updateFault(f: Fault)

    @Query("DELETE FROM faults WHERE id = :id")
    suspend fun deleteFault(id: Long)

    @Insert
    suspend fun insertSymptom(s: FaultSymptom): Long

    @Insert
    suspend fun insertCode(c: ErrorCode): Long

    @Insert
    suspend fun insertSolution(s: Solution): Long

    @Insert
    suspend fun insertSolutionStep(s: SolutionStep): Long

    @Query("DELETE FROM fault_symptoms WHERE faultId = :faultId")
    suspend fun deleteSymptoms(faultId: Long)

    @Query("DELETE FROM error_codes WHERE faultId = :faultId")
    suspend fun deleteCodes(faultId: Long)

    @Query("DELETE FROM solutions WHERE faultId = :faultId")
    suspend fun deleteSolutions(faultId: Long)

    @Query("SELECT COUNT(*) FROM faults")
    fun count(): Flow<Int>

    @Query("DELETE FROM faults WHERE isSeed = 1")
    suspend fun deleteSeedFaults()
}

@Dao
interface GuideDao {
    @Query("SELECT * FROM technical_guides WHERE type = :type ORDER BY title COLLATE NOCASE")
    fun guidesByType(type: String): Flow<List<TechnicalGuide>>

    @Query("SELECT * FROM technical_guides ORDER BY title COLLATE NOCASE")
    fun guides(): Flow<List<TechnicalGuide>>

    @Transaction
    @Query("SELECT * FROM technical_guides ORDER BY title COLLATE NOCASE")
    suspend fun guidesFull(): List<GuideFull>

    @Transaction
    @Query("SELECT * FROM technical_guides WHERE id = :id")
    fun guideFull(id: Long): Flow<GuideFull?>

    @Transaction
    @Query("SELECT * FROM technical_guides WHERE id = :id")
    suspend fun guideFullOnce(id: Long): GuideFull?

    @Query("SELECT * FROM technical_guides WHERE modelId = :modelId ORDER BY title COLLATE NOCASE")
    fun guidesForModel(modelId: Long): Flow<List<TechnicalGuide>>

    @Insert
    suspend fun insertGuide(g: TechnicalGuide): Long

    @Update
    suspend fun updateGuide(g: TechnicalGuide)

    @Query("DELETE FROM technical_guides WHERE id = :id")
    suspend fun deleteGuide(id: Long)

    @Insert
    suspend fun insertStep(s: GuideStep): Long

    @Query("DELETE FROM guide_steps WHERE guideId = :guideId")
    suspend fun deleteSteps(guideId: Long)

    @Query("SELECT COUNT(*) FROM technical_guides")
    fun count(): Flow<Int>

    @Query("DELETE FROM technical_guides WHERE isSeed = 1")
    suspend fun deleteSeedGuides()
}

@Dao
interface JobDao {
    @Query("SELECT * FROM maintenance_jobs ORDER BY visitDate DESC, id DESC")
    fun jobs(): Flow<List<MaintenanceJob>>

    @Transaction
    @Query("SELECT * FROM maintenance_jobs ORDER BY visitDate DESC, id DESC")
    suspend fun jobsFull(): List<JobFull>

    @Transaction
    @Query("SELECT * FROM maintenance_jobs WHERE id = :id")
    fun jobFull(id: Long): Flow<JobFull?>

    @Transaction
    @Query("SELECT * FROM maintenance_jobs WHERE id = :id")
    suspend fun jobFullOnce(id: Long): JobFull?

    @Insert
    suspend fun insertJob(j: MaintenanceJob): Long

    @Update
    suspend fun updateJob(j: MaintenanceJob)

    @Query("DELETE FROM maintenance_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Insert
    suspend fun insertJobPart(p: JobPart): Long

    @Query("DELETE FROM job_parts WHERE jobId = :jobId")
    suspend fun deleteJobParts(jobId: Long)

    @Query("SELECT COUNT(*) FROM maintenance_jobs")
    fun count(): Flow<Int>

    @Query("DELETE FROM maintenance_jobs WHERE isSeed = 1")
    suspend fun deleteSeedJobs()
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_attachments WHERE ownerType = :type AND ownerId = :id ORDER BY createdAt")
    fun forOwner(type: String, id: Long): Flow<List<MediaAttachment>>

    @Query("SELECT * FROM media_attachments WHERE ownerType = :type AND ownerId = :id ORDER BY createdAt")
    suspend fun forOwnerOnce(type: String, id: Long): List<MediaAttachment>

    @Query("SELECT * FROM media_attachments ORDER BY createdAt DESC")
    fun all(): Flow<List<MediaAttachment>>

    @Query("SELECT * FROM media_attachments ORDER BY createdAt DESC")
    suspend fun allOnce(): List<MediaAttachment>

    @Query("SELECT * FROM media_attachments WHERE id = :id")
    suspend fun byId(id: Long): MediaAttachment?

    @Insert
    suspend fun insert(m: MediaAttachment): Long

    @Query("DELETE FROM media_attachments WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM media_attachments WHERE ownerType = :type AND ownerId = :id")
    suspend fun deleteForOwner(type: String, id: Long)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun all(): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    suspend fun allOnce(): List<Favorite>

    @Query("SELECT COUNT(*) FROM favorites WHERE targetType = :type AND targetId = :id")
    fun isFavorite(type: String, id: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(f: Favorite): Long

    @Query("DELETE FROM favorites WHERE targetType = :type AND targetId = :id")
    suspend fun delete(type: String, id: Long)
}
