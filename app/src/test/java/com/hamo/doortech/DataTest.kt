package com.hamo.doortech

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hamo.doortech.data.db.AppDatabase
import com.hamo.doortech.data.db.Brand
import com.hamo.doortech.data.db.DoorCategory
import com.hamo.doortech.data.db.Fault
import com.hamo.doortech.data.db.GuideType
import com.hamo.doortech.data.db.JobPart
import com.hamo.doortech.data.db.MachineModel
import com.hamo.doortech.data.db.MaintenanceJob
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.db.SparePart
import com.hamo.doortech.data.db.TechnicalGuide
import com.hamo.doortech.data.repo.AppRepository
import com.hamo.doortech.data.repo.BackupManager
import com.hamo.doortech.data.repo.CodeDraft
import com.hamo.doortech.data.repo.FaultDraft
import com.hamo.doortech.data.repo.GuideDraft
import com.hamo.doortech.data.repo.InspectResult
import com.hamo.doortech.data.repo.JobDraft
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.data.repo.ReferenceData
import com.hamo.doortech.data.repo.SeedData
import com.hamo.doortech.data.repo.SolutionDraft
import com.hamo.doortech.domain.SearchEngine
import com.hamo.doortech.util.JobReportData
import com.hamo.doortech.util.JobReportPdf
import com.hamo.doortech.util.MediaStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataTest {
    private lateinit var ctx: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: AppRepository

    private fun newDb() = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).allowMainThreadQueries().build()

    @Before fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        db = newDb()
        repo = AppRepository(db, MediaStore(ctx))
    }

    @After fun tearDown() { db.close() }

    private fun ok(o: Outcome): Long { assertTrue("expected Ok but was $o", o is Outcome.Ok); return (o as Outcome.Ok).id }

    @Test fun categoryBrandModelCrudAndRelations() = runBlocking<Unit> {
        val cat = ok(repo.saveCategory(DoorCategory(name = "أبواب زجاجية")))
        val brand = ok(repo.saveBrand(Brand(name = "شركة تجربة")))
        val model = ok(repo.saveModel(MachineModel(brandId = brand, categoryId = cat, name = "M-1")))
        assertEquals(1, repo.modelsOnce().size)
        // اسم مكرر مرفوض
        assertTrue(repo.saveCategory(DoorCategory(name = "أبواب زجاجية")) is Outcome.Fail)
        // حذف آمن: الشركة لها موديل
        assertTrue(repo.deleteBrand(repo.brandsOnce().first { it.id == brand }) is Outcome.Fail)
        assertTrue(repo.deleteCategory(repo.categoriesOnce().first { it.id == cat }) is Outcome.Fail)
        ok(repo.deleteModel(repo.modelsOnce().first { it.id == model }))
        ok(repo.deleteBrand(repo.brandsOnce().first { it.id == brand }))
        ok(repo.deleteCategory(repo.categoriesOnce().first { it.id == cat }))
        assertTrue(repo.brandsOnce().isEmpty())
    }

    @Test fun faultCrudWithSymptomsCodesSolutionsAndSteps() = runBlocking<Unit> {
        val id = ok(repo.saveFault(FaultDraft(
            fault = Fault(title = "الباب لا يغلق"),
            symptoms = listOf("يتوقف في المنتصف"),
            codes = listOf(CodeDraft("E1", "مثال")),
            solutions = listOf(SolutionDraft("فحص", steps = listOf("خطوة 1", "خطوة 2"))),
        )))
        val full = repo.faultFullOnce(id)!!
        assertEquals(1, full.symptoms.size)
        assertEquals(1, full.codes.size)
        assertEquals(2, full.solutions.single().steps.size)
        // تعديل ثم حذف
        ok(repo.saveFault(FaultDraft(full.fault.copy(title = "عنوان جديد"), symptoms = emptyList(), solutions = emptyList())))
        assertEquals("عنوان جديد", repo.faultFullOnce(id)!!.fault.title)
        assertTrue(repo.faultFullOnce(id)!!.symptoms.isEmpty())
        ok(repo.deleteFault(id))
        assertEquals(null, repo.faultFullOnce(id))
    }

    @Test fun faultWithoutTitleIsRejected() = runBlocking<Unit> {
        assertTrue(repo.saveFault(FaultDraft(Fault(title = " "))) is Outcome.Fail)
    }

    @Test fun guideJobPartsFavoritesAndMedia() = runBlocking<Unit> {
        val g = ok(repo.saveGuide(GuideDraft(TechnicalGuide(type = GuideType.INSTALL, title = "تركيب"), listOf("أ", "ب"))))
        assertEquals(2, repo.guideFullOnce(g)!!.steps.size)
        val part = ok(repo.savePart(SparePart(name = "ريموت", quantity = 3)))
        assertTrue(repo.savePart(SparePart(name = "x", quantity = -1)) is Outcome.Fail)
        val j = ok(repo.saveJob(JobDraft(
            MaintenanceJob(customerName = "عميل"),
            listOf(JobPart(jobId = 0, partId = part, name = "ريموت", quantity = 1)),
        )))
        assertEquals(1, repo.jobFullOnce(j)!!.parts.size)
        // مفضلة
        repo.toggleFavorite(OwnerType.GUIDE, g)
        assertEquals(1, repo.favorites.first().size)
        repo.toggleFavorite(OwnerType.GUIDE, g)
        assertTrue(repo.favorites.first().isEmpty())
        // رابط فيديو
        ok(repo.addVideoLink(OwnerType.JOB, j, "https://example.com/video", "شرح"))
        assertTrue(repo.addVideoLink(OwnerType.JOB, j, "not a url", "x") is Outcome.Fail)
        val m = repo.mediaOnce(OwnerType.JOB, j).single()
        assertEquals(MediaKind.LINK, m.kind)
        ok(repo.deleteMedia(m.id))
        assertTrue(repo.mediaOnce(OwnerType.JOB, j).isEmpty())
        ok(repo.deleteJob(j))
        ok(repo.deleteGuide(g))
    }

    @Test fun seedMeetsRequirementsAndSearchWorksOffline() = runBlocking<Unit> {
        SeedData.populate(repo)
        assertTrue(repo.categoriesOnce().size >= 7)
        assertTrue(repo.faults.first().size >= 15)
        assertTrue(repo.guides.first().size >= 10)
        assertTrue(repo.jobs.first().size >= 5)
        assertTrue(repo.faults.first().all { it.isSeed })
        val hits = SearchEngine.search(repo.searchDocs(), "الباب")
        assertTrue(hits.isNotEmpty())
        // حذف البيانات التجريبية
        ok(repo.deleteSeedData())
        assertTrue(repo.faults.first().isEmpty())
        assertTrue(repo.jobs.first().isEmpty())
    }

    @Test fun backupExportInspectImportAndCorruptFile() = runBlocking<Unit> {
        SeedData.populate(repo)
        ok(repo.saveFault(FaultDraft(Fault(title = "عطل خاص بي"))))
        val out = ByteArrayOutputStream()
        ok(BackupManager(repo).export(out))
        val file = File(ctx.cacheDir, "b.zip").also { it.writeBytes(out.toByteArray()) }

        val db2 = newDb()
        try {
            val repo2 = AppRepository(db2, MediaStore(ctx))
            val bm2 = BackupManager(repo2)
            val r = bm2.inspect(file)
            assertTrue(r is InspectResult.Valid)
            ok(bm2.import(file))
            assertEquals(repo.faults.first().size, repo2.faults.first().size)
            // استيراد ثانٍ لا يكرر
            ok(bm2.import(file))
            assertEquals(repo.faults.first().size, repo2.faults.first().size)
            assertEquals(repo.jobs.first().size, repo2.jobs.first().size)
        } finally { db2.close() }

        // ملف تالف
        val bytes = file.readBytes()
        val bad = File(ctx.cacheDir, "bad.zip").also { it.writeBytes(bytes.copyOf(bytes.size / 2)) }
        assertTrue(BackupManager(repo).inspect(bad) is InspectResult.Invalid)
        val junk = File(ctx.cacheDir, "junk.zip").also { it.writeText("not a zip") }
        assertTrue(BackupManager(repo).inspect(junk) is InspectResult.Invalid)
    }

    @Test fun referenceDataLoadsOnceAndSurvivesSeedDeletion() = runBlocking<Unit> {
        SeedData.populate(repo)
        ReferenceData.populate(repo)
        ReferenceData.populate(repo) // لا يكرر
        val brands = repo.brandsOnce().filter { !it.isSeed }.map { it.name }
        assertTrue(brands.toSet().containsAll(setOf("CAME", "FAAC", "Nice", "Motorline", "Roger Technology", "BFT", "DEA System")))
        assertTrue(repo.modelsOnce().count { !it.isSeed } >= 8)
        val before = repo.guides.first().count { !it.isSeed }
        assertTrue(before >= 15)
        ok(repo.deleteSeedData())
        assertEquals(before, repo.guides.first().size)
        val hits = SearchEngine.search(repo.searchDocs(), "844")
        assertTrue(hits.isNotEmpty())
    }
}
