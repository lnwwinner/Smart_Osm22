package com.example.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Gender
import com.example.data.HouseholdRole
import com.example.data.PersonStatus
import com.example.data.PersonRepository
import com.example.data.sync.RoomFirestoreSyncHelper
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Regression coverage for the real backup/restore path represented by
 * population_backup_*.xlsx:
 * - 17 households
 * - 69 people
 * - UUID identity is preserved
 * - re-import is idempotent
 * - Firebase failure cannot remove restored Room data
 *
 * The production backup is intentionally NOT committed to the repository because
 * it contains personal data. This test uses synthetic data with the same cardinality.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExcelBackupRestoreIntegrityTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ExcelImportUseCase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = ExcelImportUseCase(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun restore17Households69People_persistsAndIsIdempotent() = runBlocking {
        val bytes = createSyntheticBackup()

        val firstPlan = useCase.createImportPlan(ByteArrayInputStream(bytes))
        assertEquals(69, firstPlan.totalRows)
        assertEquals(69, firstPlan.insertCount)
        assertEquals(0, firstPlan.updateCount)
        assertEquals(0, firstPlan.needsReviewCount)

        val firstResult = useCase.commitImportPlan(firstPlan)
        assertEquals(69, firstResult.successCount)
        assertEquals(0, firstResult.failedCount)
        assertEquals(17, db.householdDao().getAllHouseholds().size)
        assertEquals(69, db.personDao().getAllPersonsList().size)

        val households = db.householdDao().getAllHouseholds()
        val persons = db.personDao().getAllPersonsList()
        assertEquals(17, households.map { it.householdUuid }.distinct().size)
        assertTrue(persons.all { person ->
            db.householdDao().getHouseholdById(person.householdId) != null
        })

        val secondPlan = useCase.createImportPlan(ByteArrayInputStream(bytes))
        assertEquals(0, secondPlan.insertCount)
        assertEquals(69, secondPlan.updateCount)
        assertEquals(0, secondPlan.needsReviewCount)

        val secondResult = useCase.commitImportPlan(secondPlan)
        assertEquals(69, secondResult.successCount)
        assertEquals(0, secondResult.failedCount)
        assertEquals(17, db.householdDao().getAllHouseholds().size)
        assertEquals(69, db.personDao().getAllPersonsList().size)
    }

    @Test
    fun firebaseFailure_doesNotClearRestoredRoomData() = runBlocking {
        val bytes = createSyntheticBackup()
        val plan = useCase.createImportPlan(ByteArrayInputStream(bytes))
        useCase.commitImportPlan(plan)

        val repository = PersonRepository(
            db,
            db.personDao(),
            db.householdDao(),
            db.personHistoryDao()
        )
        val helper = RoomFirestoreSyncHelper(
            context,
            repository,
            firestoreProvider = { null }
        )

        val beforeHouseholds = db.householdDao().getAllHouseholds().size
        val beforePersons = db.personDao().getAllPersonsList().size

        val result = helper.syncRoomToFirestore()

        assertTrue(result.isFailure)
        assertEquals(beforeHouseholds, db.householdDao().getAllHouseholds().size)
        assertEquals(beforePersons, db.personDao().getAllPersonsList().size)
        assertNotNull(db.householdDao().getHouseholdByUuid("00000000-0000-0000-0000-000000000001"))
        assertNotNull(db.personDao().getPersonByUuid("00000000-0000-0000-0000-000000000001"))
    }

    private fun createSyntheticBackup(): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(SmartOsmExcelSchema.SHEET_NAME)

        SmartOsmExcelSchema.CANONICAL_COLUMNS.forEachIndexed { index, header ->
            sheet.createRow(0).createCell(index).setCellValue(header)
        }

        var rowIndex = 1
        var personNumber = 1

        for (householdNumber in 1..17) {
            val householdUuid = String.format(
                "00000000-0000-0000-0000-%012d",
                householdNumber
            )
            val houseNo = (householdNumber * 10).toString()
            val members = when (householdNumber) {
                1 -> 9
                2 -> 3
                17 -> 1
                else -> 4
            }

            repeat(members) { memberIndex ->
                val row = sheet.createRow(rowIndex++)
                val personUuid = String.format(
                    "00000000-0000-0000-0001-%012d",
                    personNumber++
                )

                row.createCell(0).setCellValue(SmartOsmExcelSchema.SCHEMA_VERSION)
                row.createCell(1).setCellValue(householdUuid)
                row.createCell(2).setCellValue(personUuid)
                row.createCell(3).setCellValue(houseNo)
                row.createCell(4).setCellValue("8")
                row.createCell(5).setCellValue("ป่าขะ")
                row.createCell(6).setCellValue("บ้านนา")
                row.createCell(7).setCellValue("นครนายก")
                row.createCell(8).setCellValue("")
                row.createCell(9).setCellValue("Synthetic Person $personNumber")
                row.createCell(10).setCellValue(
                    if (memberIndex % 2 == 0) Gender.MALE.name else Gender.FEMALE.name
                )
                row.createCell(11).setCellValue("1990-01-01")
                row.createCell(12).setCellValue("DAY")
                row.createCell(13).setCellValue(
                    if (memberIndex == 0) HouseholdRole.HEAD.name else HouseholdRole.RESIDENT.name
                )
                row.createCell(14).setCellValue(PersonStatus.ALIVE.name)
                row.createCell(15).setCellValue("VERIFIED")
            }
        }

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()
        return out.toByteArray()
    }
}
