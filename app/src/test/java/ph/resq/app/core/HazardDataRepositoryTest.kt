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
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HazardDataRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: ResQDatabase
    private lateinit var repo: HazardDataRepository
    private fun testCipher() = EncryptedFieldCipher(testKeyProvider = { SecretKeySpec(ByteArray(32) { 0x5a }, "AES") })

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        db = ResQDatabase(context, testCipher())
        repo = HazardDataRepository(db)
    }
    @After fun tearDown() { db.close(); context.deleteDatabase("ph_resq_local.db") }

    @Test fun advisoryExpiryIsExplicitAndEmptyRefreshCannotEraseCache() {
        val now = System.currentTimeMillis()
        repo.replaceAdvisories(listOf(CachedAdvisory("p1", "Heavy rain", AdvisorySeverity.WARNING, "Monitor local alerts", "https://www.pagasa.dost.gov.ph/", now - 1000, now, now + 60_000)))
        assertFalse(repo.advisories(now).single().second)
        assertTrue(repo.advisories(now + 61_000).single().second)
        try { repo.replaceAdvisories(emptyList()); fail("empty refresh must be rejected") } catch (_: IllegalArgumentException) { }
        assertEquals(1, repo.advisories(now).size)
    }

    @Test fun evacuationSearchLabelsStaleAndRejectsInvalidCoordinates() {
        val now = System.currentTimeMillis()
        repo.replaceEvacuationCenters(listOf(EvacuationCenter("e1", "Municipal Gym", "Main Road", 7.1, 125.6, EvacuationStatus.OPEN, "https://example.gov.ph/evacuation", now - 2 * 86_400_000L)))
        assertTrue(repo.searchEvacuationCenters("gym", now).single().second)
        try {
            repo.replaceEvacuationCenters(listOf(EvacuationCenter("bad", "Bad", "Unknown", 200.0, 1.0, EvacuationStatus.OPEN, "https://example.gov.ph", now)))
            fail("invalid latitude must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertEquals("Municipal Gym", repo.searchEvacuationCenters("").single().first.name)
    }
}
