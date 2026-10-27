package ph.resq.app.core

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineDataRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: ResQDatabase

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        db = ResQDatabase(context)
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
