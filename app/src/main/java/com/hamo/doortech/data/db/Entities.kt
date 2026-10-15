package com.hamo.doortech.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** ثوابت أنواع المالك للمرفقات والمفضلة. */
object OwnerType {
    const val FAULT = "FAULT"
    const val MODEL = "MODEL"
    const val GUIDE = "GUIDE"
    const val JOB = "JOB"
}

object GuideType {
    const val INSTALL = "INSTALL"
    const val PROGRAMMING = "PROGRAMMING"
    const val WIRING = "WIRING"
    const val REMOTE = "REMOTE"

    val all = listOf(INSTALL, PROGRAMMING, WIRING, REMOTE)

    fun label(type: String): String = when (type) {
        INSTALL -> "أدلة التركيب"
        PROGRAMMING -> "أدلة البرمجة"
        WIRING -> "التوصيلات الكهربائية"
        REMOTE -> "إعدادات الريموت والحساسات"
        else -> type
    }

    fun singular(type: String): String = when (type) {
        INSTALL -> "دليل تركيب"
        PROGRAMMING -> "دليل برمجة"
        WIRING -> "توصيلات كهربائية"
        REMOTE -> "ريموت وحساسات"
        else -> type
    }
}

object MediaKind {
    const val IMAGE = "IMAGE"
    const val DOC = "DOC"
    const val LINK = "LINK"
}

object MediaRole {
    const val NONE = "NONE"
    const val BEFORE = "BEFORE"
    const val AFTER = "AFTER"

    fun label(role: String): String = when (role) {
        BEFORE -> "قبل"
        AFTER -> "بعد"
        else -> ""
    }
}

/** درجة توثيق المعلومة. */
object Verification {
    const val UNVERIFIED = 0
    const val GENERAL = 1
    const val DOCUMENTED = 2

    val all = listOf(UNVERIFIED, GENERAL, DOCUMENTED)

    fun label(level: Int): String = when (level) {
        DOCUMENTED -> "موثّق من مصدر"
        GENERAL -> "إرشاد عام"
        else -> "غير موثّق"
    }
}

@Entity(tableName = "door_categories", indices = [Index(value = ["name"], unique = true)])
data class DoorCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "brands", indices = [Index(value = ["name"], unique = true)])
data class Brand(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val country: String = "",
    val notes: String = "",
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "machine_models",
    foreignKeys = [
        ForeignKey(
            entity = Brand::class, parentColumns = ["id"], childColumns = ["brandId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = DoorCategory::class, parentColumns = ["id"], childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("brandId"), Index("categoryId"), Index("name")],
)
data class MachineModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brandId: Long,
    val categoryId: Long? = null,
    val name: String,
    val controlUnit: String = "",
    val specs: String = "",
    val remoteInfo: String = "",
    val sensorsInfo: String = "",
    val notes: String = "",
    val source: String = "",
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "faults",
    foreignKeys = [
        ForeignKey(
            entity = DoorCategory::class, parentColumns = ["id"], childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = MachineModel::class, parentColumns = ["id"], childColumns = ["modelId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("modelId"), Index("title")],
)
data class Fault(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val categoryId: Long? = null,
    val modelId: Long? = null,
    val description: String = "",
    /** كلمات بحث بديلة مفصولة بفواصل أو أسطر. */
    val altKeywords: String = "",
    val warnings: String = "",
    val tools: String = "",
    val source: String = "",
    val level: Int = Verification.UNVERIFIED,
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "fault_symptoms",
    foreignKeys = [ForeignKey(
        entity = Fault::class, parentColumns = ["id"], childColumns = ["faultId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("faultId")],
)
data class FaultSymptom(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faultId: Long,
    val text: String,
)

@Entity(
    tableName = "error_codes",
    foreignKeys = [ForeignKey(
        entity = Fault::class, parentColumns = ["id"], childColumns = ["faultId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("faultId"), Index("code")],
)
data class ErrorCode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faultId: Long,
    val code: String,
    val meaning: String = "",
)

@Entity(
    tableName = "solutions",
    foreignKeys = [ForeignKey(
        entity = Fault::class, parentColumns = ["id"], childColumns = ["faultId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("faultId")],
)
data class Solution(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faultId: Long,
    val title: String,
    val source: String = "",
    /** الموديل أو الحالة التي ينطبق عليها هذا الحل (نص حر). */
    val appliesTo: String = "",
    val level: Int = Verification.UNVERIFIED,
    val warnings: String = "",
    val tools: String = "",
)

@Entity(
    tableName = "solution_steps",
    foreignKeys = [ForeignKey(
        entity = Solution::class, parentColumns = ["id"], childColumns = ["solutionId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("solutionId")],
)
data class SolutionStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solutionId: Long,
    val position: Int,
    val text: String,
)

@Entity(
    tableName = "technical_guides",
    foreignKeys = [
        ForeignKey(
            entity = DoorCategory::class, parentColumns = ["id"], childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = MachineModel::class, parentColumns = ["id"], childColumns = ["modelId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("modelId"), Index("type")],
)
data class TechnicalGuide(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val categoryId: Long? = null,
    val modelId: Long? = null,
    val summary: String = "",
    val warnings: String = "",
    val source: String = "",
    val level: Int = Verification.UNVERIFIED,
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "guide_steps",
    foreignKeys = [ForeignKey(
        entity = TechnicalGuide::class, parentColumns = ["id"], childColumns = ["guideId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("guideId")],
)
data class GuideStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guideId: Long,
    val position: Int,
    val text: String,
)

/** مرفق: صورة أو مستند (path = اسم الملف داخل مخزن التطبيق) أو رابط فيديو (path = الرابط). */
@Entity(tableName = "media_attachments", indices = [Index("ownerType", "ownerId")])
data class MediaAttachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerType: String,
    val ownerId: Long,
    val kind: String,
    val path: String,
    val title: String = "",
    val role: String = MediaRole.NONE,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "spare_parts",
    foreignKeys = [
        ForeignKey(
            entity = Brand::class, parentColumns = ["id"], childColumns = ["brandId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = MachineModel::class, parentColumns = ["id"], childColumns = ["modelId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("brandId"), Index("modelId"), Index("name")],
)
data class SparePart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val partNumber: String = "",
    val brandId: Long? = null,
    val modelId: Long? = null,
    val quantity: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "maintenance_jobs",
    foreignKeys = [
        ForeignKey(
            entity = DoorCategory::class, parentColumns = ["id"], childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = Brand::class, parentColumns = ["id"], childColumns = ["brandId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = MachineModel::class, parentColumns = ["id"], childColumns = ["modelId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("brandId"), Index("modelId"), Index("visitDate")],
)
data class MaintenanceJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val location: String = "",
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val modelId: Long? = null,
    val problem: String = "",
    val findings: String = "",
    val solution: String = "",
    val notes: String = "",
    val visitDate: Long = System.currentTimeMillis(),
    val nextVisitDate: Long? = null,
    val isSeed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** قطعة مستخدمة في زيارة صيانة (الاسم محفوظ نصيًا حتى لا يضيع إن حُذفت القطعة من المخزون). */
@Entity(
    tableName = "job_parts",
    foreignKeys = [ForeignKey(
        entity = MaintenanceJob::class, parentColumns = ["id"], childColumns = ["jobId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("jobId")],
)
data class JobPart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val partId: Long? = null,
    val name: String,
    val quantity: Int = 1,
)

@Entity(tableName = "favorites", indices = [Index(value = ["targetType", "targetId"], unique = true)])
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String,
    val targetId: Long,
    val createdAt: Long = System.currentTimeMillis(),
)
