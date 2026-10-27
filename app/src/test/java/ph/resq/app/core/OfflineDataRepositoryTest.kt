package ph.resq.app.core

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineDataRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: ResQDatabase
    private fun testCipher() = EncryptedFieldCipher(testKeyProvider = { SecretKeySpec(ByteArray(32) { 0x5a }, "AES") })

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        db = ResQDatabase(context, testCipher())
    }

    @After fun tearDown() { db.close(); context.deleteDatabase("ph_resq_local.db") }

    @Test fun directoryImportIsTransactionalAndSearchableWithMetadata() {
        val repository = OfflineDataRepository(context, db)
        repository.replaceDirectory(
            listOf(DirectoryRecord("a", "Barangay Clinic", "medical", "911", 14.6, 121.0),
                DirectoryRecord("b", "Fire Station", "fire", "117")),
            DirectoryDataset("https://example.gov.ph/dataset", "Public-sector terms reviewed", "v1", System.currentTimeMillis())
        )
        assertEquals("Barangay Clinic", repository.searchDirectory("clinic").single().name)
        assertEquals("2", repository.datasetMetadata()!!["recordCount"])
    }

    @Test fun invalidOrEmptyDirectoryCannotReplaceExistingData() {
        val repository = OfflineDataRepository(context, db)
        val dataset = DirectoryDataset("https://example.gov.ph/data", "terms", "v1", System.currentTimeMillis())
        repository.replaceDirectory(listOf(DirectoryRecord("a", "Clinic", "medical")), dataset)
        try {
            repository.replaceDirectory(emptyList(), dataset)
            fail("Expected empty import rejection")
        } catch (_: IllegalArgumentException) { }
        assertEquals(1, repository.searchDirectory("").size)
    }

    @Test fun versionSixDatabaseWithoutDirectoryTableUpgradesAndCreatesCoordinateIndex() {
        db.close()
        context.deleteDatabase("ph_resq_local.db")
        val legacy = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("ph_resq_local.db").absolutePath, null)
        legacy.version = 6
        legacy.close()

        db = ResQDatabase(context, testCipher())
        val upgraded = db.writableDatabase
        upgraded.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='directory_entries'", null).use {
            assertTrue("directory_entries table should be created during migration", it.moveToFirst())
        }
        upgraded.rawQuery("PRAGMA index_list('directory_entries')", null).use { cursor ->
            var found = false
            while (cursor.moveToNext()) if (cursor.getString(1) == "idx_directory_coordinates") found = true
            assertTrue("coordinate index should be created during migration", found)
        }
    }

    @Test fun nearbySearchUsesCoordinatesFiltersRadiusAndSortsByDistance() {
        val repository = OfflineDataRepository(context, db)
        repository.replaceDirectory(
            listOf(
                DirectoryRecord("near", "Nearby Clinic", "medical", latitude = 14.600, longitude = 121.000),
                DirectoryRecord("middle", "Second Clinic", "medical", latitude = 14.610, longitude = 121.000),
                DirectoryRecord("far", "Far Clinic", "medical", latitude = 15.100, longitude = 121.000),
                DirectoryRecord("no-coordinates", "Unknown Clinic", "medical")
            ),
            DirectoryDataset("https://example.gov.ph/data", "Public-sector terms reviewed", "v2", System.currentTimeMillis())
        )
        val result = repository.searchNearby(14.600, 121.000, radiusKm = 5.0)
        assertEquals(listOf("near", "middle"), result.map { it.record.id })
        assertTrue(result.first().distanceKm < 0.1)
        assertTrue(result.first().distanceKm < result.last().distanceKm)
    }

    @Test fun nearbySearchRejectsInvalidCoordinatesAndRadius() {
        val repository = OfflineDataRepository(context, db)
        try { repository.searchNearby(91.0, 0.0); fail("Expected latitude validation") }
        catch (_: IllegalArgumentException) { }
        try { repository.searchNearby(0.0, 0.0, 0.0); fail("Expected radius validation") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun tileCacheVerifiesHashBeforeSavingAndReading() {
        val cache = OfflineMapTileCache(context, db)
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        val sha = IntegrityVerifier.sha256Hex(bytes)
        cache.save("z1/x2/y3", bytes, sha, "map-v1")
        assertArrayEquals(bytes, cache.read("z1/x2/y3"))
        try {
            cache.save("bad", bytes, "0".repeat(64), "map-v1")
            fail("Expected integrity rejection")
        } catch (_: IllegalArgumentException) { }
    }
}
