package ph.resq.app.core

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResQDatabaseTest {
    private lateinit var context: Context
    private lateinit var database: ResQDatabase
    private fun testCipher() = EncryptedFieldCipher(testKeyProvider = { SecretKeySpec(ByteArray(32) { 0x5a }, "AES") })

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        database = ResQDatabase(context, testCipher())
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase("ph_resq_local.db")
    }

    @Test
    fun searchHistoryDeduplicatesCaseInsensitivelyAndCanBeCleared() {
        database.recordSearch("  911 ")
        database.recordSearch("911")
        database.recordSearch("RED CROSS")

        assertEquals(listOf("RED CROSS", "911"), database.recentSearches())
        database.clearSearchHistory()
        assertEquals(emptyList<String>(), database.recentSearches())
    }

    @Test
    fun searchHistoryRetainsOnlyThirtyMostRecentDistinctQueries() {
        (1..35).forEach { database.recordSearch("query-$it") }

        val history = database.recentSearches(30)
        assertEquals(30, history.size)
        assertEquals("query-35", history.first())
        assertEquals("query-6", history.last())
    }

    @Test
    fun bookmarksCanBeSavedUpdatedAndRemoved() {
        database.saveBookmark("hotline:911", "Emergency • 911")
        database.saveBookmark("hotline:911", "National Emergency • 911")
        database.saveBookmark("hotline:143", "Red Cross • 143")

        assertEquals(
            listOf(
                "hotline:911" to "National Emergency • 911",
                "hotline:143" to "Red Cross • 143"
            ),
            database.bookmarks()
        )

        database.removeBookmark("hotline:911")
        assertEquals(listOf("hotline:143" to "Red Cross • 143"), database.bookmarks())
    }

    @Test
    fun familyCheckInIsPersistedAndLatestValueWins() {
        database.recordFamilyCheckin("Safe • first")
        database.recordFamilyCheckin("Need help • second")
        assertEquals("Need help • second", database.latestFamilyCheckin())
    }

    @Test
    fun localRecordsSurviveDatabaseHelperReopen() {
        database.recordSearch("evacuation")
        database.saveBookmark("hotline:911", "National Emergency • 911")
        database.recordFamilyCheckin("Safe • local")

        database.close()
        database = ResQDatabase(context, testCipher())

        assertEquals(listOf("evacuation"), database.recentSearches())
        assertEquals(listOf("hotline:911" to "National Emergency • 911"), database.bookmarks())
        assertEquals("Safe • local", database.latestFamilyCheckin())
    }

    @Test
    fun familyCheckInCreatesEncryptedPendingOutboxItem() {
        database.recordFamilyCheckin("Need help at shelter")
        assertEquals(1, database.pendingActionCount())
        val item = database.pendingActions().single()
        assertEquals("FAMILY_CHECKIN", item.type)
        assertEquals("Need help at shelter", item.payload)
        assertEquals("PENDING", item.status)
        database.updatePendingAction(item.id, "RETRY", "No transport")
        assertEquals(1, database.pendingActions().single().attempts)
    }

    @Test
    fun appStatePersistsBatterySnapshot() {
        database.putAppState("low_battery_snapshot", "Battery <=5% (4%)")
        assertEquals("Battery <=5% (4%)", database.getAppState("low_battery_snapshot"))
    }

    @Test
    fun sosIntentAndRecipientSnapshotAreDurableBeforeExternalHandoff() {
        val draft = "Kailangan ng tulong. Location: 7.1,125.6"
        val recipients = "+639171234567;+639181234567"
        val location = "Fresh fix: 7.1,125.6 • accuracy 12m"
        database.recordSosEvent(SosState.QUEUED, draft, recipients, location)
        val event = database.latestSosEventSnapshot()
        assertEquals(SosState.QUEUED, event?.state)
        assertTrue(event != null && event.eventId.isNotBlank())
        assertEquals(draft, event?.draft)
        assertEquals(recipients, event?.recipients)
        assertEquals(location, event?.location)
        assertTrue((event?.createdAt ?: 0L) > 0L)
    }

    @Test
    fun sosStateTransitionsKeepTheSameStableEventId() {
        val queuedId = database.recordSosEvent(SosState.QUEUED, "SOS draft", "+639171234567", "last known")
        database.recordSosEvent(SosState.SENDING, "SOS draft", "+639171234567", "last known", queuedId)
        val sending = database.latestSosEventSnapshot()
        assertEquals(SosState.SENDING, sending?.state)
        assertEquals(queuedId, sending?.eventId)
    }

    @Test
    fun startupReconciliationHandlesEveryInterruptedEventWithoutResending() {
        val queued = database.recordSosEvent(SosState.QUEUED, "queued draft", "+639171111111", "queued location")
        val sending = database.recordSosEvent(SosState.QUEUED, "sending draft", "+639172222222", "sending location")
        database.recordSosEvent(SosState.SENDING, "sending draft", "+639172222222", "sending location", sending)

        assertEquals(2, database.reconcileInterruptedSosHandoffs())
        assertEquals(SosState.INTERRUPTED_NOT_SENT, database.latestSosEventSnapshot(queued)?.state)
        assertEquals(SosState.HANDOFF_UNCONFIRMED, database.latestSosEventSnapshot(sending)?.state)
        assertEquals("queued draft", database.latestSosEventSnapshot(queued)?.draft)
        assertEquals("sending draft", database.latestSosEventSnapshot(sending)?.draft)
        assertEquals(0, database.reconcileInterruptedSosHandoffs())
    }

    @Test
    fun handedOffEventIsNotChangedByStartupReconciliation() {
        val eventId = database.recordSosEvent(SosState.QUEUED, "draft", "+639171234567", "location")
        database.recordSosEvent(SosState.SENDING, "draft", "+639171234567", "location", eventId)
        database.recordSosEvent(SosState.HANDED_OFF, "draft", "+639171234567", "location", eventId)
        assertEquals(0, database.reconcileInterruptedSosHandoffs())
        assertEquals(SosState.HANDED_OFF, database.latestSosEventSnapshot(eventId)?.state)
    }

    @Test
    fun databaseRejectsUnverifiedDeliveryAndAcknowledgementTransitions() {
        val eventId = database.recordSosEvent(SosState.QUEUED, "SOS draft", "+639171234567", "last known")
        database.recordSosEvent(SosState.SENDING, "SOS draft", "+639171234567", "last known", eventId)
        database.recordSosEvent(SosState.HANDED_OFF, "SOS draft", "+639171234567", "last known", eventId)
        assertTrue(runCatching { database.recordSosEvent(SosState.DELIVERED, "SOS draft", "+639171234567", "last known", eventId) }.isFailure)
        assertTrue(runCatching { database.recordSosEvent(SosState.ACKNOWLEDGED, "SOS draft", "+639171234567", "last known", eventId) }.isFailure)
        assertEquals(eventId, database.latestSosEventSnapshot()?.eventId)
        assertEquals(SosState.HANDED_OFF, database.latestSosEventSnapshot()?.state)
    }

    @Test
    fun schemaSevenMigrationAssignsStableEventIdsWithoutDroppingLegacySosRows() {
        database.close()
        context.deleteDatabase("ph_resq_local.db")
        val legacy = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("ph_resq_local.db"), null)
        legacy.execSQL("CREATE TABLE sos_events (id INTEGER PRIMARY KEY AUTOINCREMENT, state TEXT NOT NULL, created_at INTEGER NOT NULL, draft_ciphertext TEXT, recipients_ciphertext TEXT, location_ciphertext TEXT)")
        legacy.execSQL("INSERT INTO sos_events(state,created_at) VALUES('QUEUED',1234)")
        legacy.version = 7
        legacy.close()

        database = ResQDatabase(context, testCipher())
        val migrated = database.latestSosEventSnapshot()
        assertEquals(SosState.QUEUED, migrated?.state)
        assertTrue(migrated != null && migrated.eventId.isNotBlank())
        assertEquals(1234L, migrated?.createdAt)
    }


    @Test
    fun interleavedSosAttemptsKeepTransitionsBoundToTheirOwnEventIds() {
        val first = database.recordSosEvent(SosState.QUEUED, "first draft", "+639171111111", "first location")
        val second = database.recordSosEvent(SosState.QUEUED, "second draft", "+639172222222", "second location")
        database.recordSosEvent(SosState.SENDING, "first draft", "+639171111111", "first location", first)
        database.recordSosEvent(SosState.HANDED_OFF, "first draft", "+639171111111", "first location", first)
        database.recordSosEvent(SosState.SENDING, "second draft", "+639172222222", "second location", second)
        database.recordSosEvent(SosState.FAILED, "second draft", "+639172222222", "second location", second)
        assertEquals(SosState.HANDED_OFF, database.latestSosEventSnapshot(first)?.state)
        assertEquals("first draft", database.latestSosEventSnapshot(first)?.draft)
        assertEquals(SosState.FAILED, database.latestSosEventSnapshot(second)?.state)
        assertEquals("second draft", database.latestSosEventSnapshot(second)?.draft)
    }

    @Test
    fun sosTransitionWithoutExplicitEventIdIsRejected() {
        database.recordSosEvent(SosState.QUEUED, "draft", "+639171234567", "location")
        assertTrue(runCatching {
            database.recordSosEvent(SosState.SENDING, "draft", "+639171234567", "location")
        }.isFailure)
    }

    @Test
    fun newDatabaseHasNoFamilyCheckIn() {
        assertNull(database.latestFamilyCheckin())
    }
}
