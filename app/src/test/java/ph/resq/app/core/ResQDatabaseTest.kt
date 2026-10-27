package ph.resq.app.core

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResQDatabaseTest {
    private lateinit var context: Context
    private lateinit var database: ResQDatabase

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("ph_resq_local.db")
        database = ResQDatabase(context)
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
        database = ResQDatabase(context)

        assertEquals(listOf("evacuation"), database.recentSearches())
        assertEquals(listOf("hotline:911" to "National Emergency • 911"), database.bookmarks())
        assertEquals("Safe • local", database.latestFamilyCheckin())
    }

    @Test
    fun newDatabaseHasNoFamilyCheckIn() {
        assertNull(database.latestFamilyCheckin())
    }
}
