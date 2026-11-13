package ph.resq.app.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

/**
 * Local transactional foundation for durable emergency state.
 * Directory/map tables are empty until sourced datasets and importers are added.
 */
class ResQDatabase(context: Context, private val fieldCipher: EncryptedFieldCipher = EncryptedFieldCipher()) : SQLiteOpenHelper(context, "ph_resq_local.db", null, 8) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE app_state (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
        db.execSQL("CREATE TABLE sos_events (id INTEGER PRIMARY KEY AUTOINCREMENT, event_id TEXT NOT NULL, state TEXT NOT NULL, created_at INTEGER NOT NULL, draft_ciphertext TEXT, recipients_ciphertext TEXT, location_ciphertext TEXT)")
        db.execSQL("CREATE TABLE last_location (id INTEGER PRIMARY KEY CHECK(id = 1), latitude REAL NOT NULL, longitude REAL NOT NULL, accuracy_m REAL, captured_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE search_history (id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, searched_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks (item_id TEXT PRIMARY KEY NOT NULL, label TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE directory_entries (entry_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, phone TEXT, latitude REAL, longitude REAL, source_url TEXT, verified_at INTEGER)")
        db.execSQL("CREATE INDEX idx_directory_coordinates ON directory_entries(latitude, longitude)")
        db.execSQL("CREATE TABLE map_tiles (tile_key TEXT PRIMARY KEY NOT NULL, local_path TEXT NOT NULL, sha256 TEXT, dataset_version TEXT, updated_at INTEGER)")
        db.execSQL("CREATE TABLE dataset_meta (dataset_name TEXT PRIMARY KEY NOT NULL, source_url TEXT NOT NULL, license TEXT NOT NULL, version TEXT NOT NULL, verified_at INTEGER NOT NULL, imported_at INTEGER NOT NULL, record_count INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE cached_advisories (advisory_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, severity TEXT NOT NULL, body TEXT NOT NULL, source_url TEXT NOT NULL, published_at INTEGER NOT NULL, fetched_at INTEGER NOT NULL, expires_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE evacuation_centers (center_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, address TEXT NOT NULL, latitude REAL, longitude REAL, status TEXT NOT NULL, source_url TEXT NOT NULL, verified_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE mesh_queue (id INTEGER PRIMARY KEY AUTOINCREMENT, message_id TEXT NOT NULL UNIQUE, packet_ciphertext TEXT NOT NULL, created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL, attempts INTEGER NOT NULL DEFAULT 0, max_attempts INTEGER NOT NULL DEFAULT 5, status TEXT NOT NULL, last_error TEXT, acked_by TEXT, acked_at INTEGER)")
        db.execSQL("CREATE TABLE mesh_seen (message_id TEXT PRIMARY KEY NOT NULL, seen_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE family_checkins (id INTEGER PRIMARY KEY AUTOINCREMENT, status TEXT NOT NULL, created_at INTEGER NOT NULL, sync_state TEXT NOT NULL)")
        db.execSQL("CREATE TABLE pending_actions (id INTEGER PRIMARY KEY AUTOINCREMENT, action_type TEXT NOT NULL, payload_ciphertext TEXT NOT NULL, created_at INTEGER NOT NULL, attempts INTEGER NOT NULL DEFAULT 0, status TEXT NOT NULL, last_error TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var version = oldVersion
        if (version < 2) {
            db.execSQL("ALTER TABLE sos_events ADD COLUMN draft_ciphertext TEXT")
            db.execSQL("ALTER TABLE sos_events ADD COLUMN recipients_ciphertext TEXT")
            db.execSQL("ALTER TABLE sos_events ADD COLUMN location_ciphertext TEXT")
            val legacyRows = mutableListOf<Array<Any?>>()
            db.rawQuery("SELECT id, draft, recipients, location FROM sos_events", null).use { cursor ->
                while (cursor.moveToNext()) {
                    legacyRows.add(arrayOf(cursor.getLong(0),
                        if (cursor.isNull(1)) null else cursor.getString(1),
                        if (cursor.isNull(2)) null else cursor.getString(2),
                        if (cursor.isNull(3)) null else cursor.getString(3)))
                }
            }
            legacyRows.forEach { row ->
                val values = ContentValues().apply {
                    val draft = row[1] as String?
                    val recipients = row[2] as String?
                    val location = row[3] as String?
                    if (draft != null) put("draft_ciphertext", fieldCipher.encrypt(draft))
                    if (recipients != null) put("recipients_ciphertext", fieldCipher.encrypt(recipients))
                    if (location != null) put("location_ciphertext", fieldCipher.encrypt(location))
                    putNull("draft"); putNull("recipients"); putNull("location")
                }
                db.update("sos_events", values, "id = ?", arrayOf(row[0].toString()))
            }
            version = 2
        }
        if (version < 3 && newVersion >= 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS pending_actions (id INTEGER PRIMARY KEY AUTOINCREMENT, action_type TEXT NOT NULL, payload_ciphertext TEXT NOT NULL, created_at INTEGER NOT NULL, attempts INTEGER NOT NULL DEFAULT 0, status TEXT NOT NULL, last_error TEXT)")
            version = 3
        }
        if (version < 4 && newVersion >= 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS dataset_meta (dataset_name TEXT PRIMARY KEY NOT NULL, source_url TEXT NOT NULL, license TEXT NOT NULL, version TEXT NOT NULL, verified_at INTEGER NOT NULL, imported_at INTEGER NOT NULL, record_count INTEGER NOT NULL)")
            version = 4
        }
        if (version < 5 && newVersion >= 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS cached_advisories (advisory_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, severity TEXT NOT NULL, body TEXT NOT NULL, source_url TEXT NOT NULL, published_at INTEGER NOT NULL, fetched_at INTEGER NOT NULL, expires_at INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS evacuation_centers (center_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, address TEXT NOT NULL, latitude REAL, longitude REAL, status TEXT NOT NULL, source_url TEXT NOT NULL, verified_at INTEGER NOT NULL)")
            version = 5
        }
        if (version < 6 && newVersion >= 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS mesh_queue (id INTEGER PRIMARY KEY AUTOINCREMENT, message_id TEXT NOT NULL UNIQUE, packet_ciphertext TEXT NOT NULL, created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL, attempts INTEGER NOT NULL DEFAULT 0, max_attempts INTEGER NOT NULL DEFAULT 5, status TEXT NOT NULL, last_error TEXT, acked_by TEXT, acked_at INTEGER)")
            db.execSQL("CREATE TABLE IF NOT EXISTS mesh_seen (message_id TEXT PRIMARY KEY NOT NULL, seen_at INTEGER NOT NULL)")
            version = 6
        }
        if (version < 7 && newVersion >= 7) {
            // Older installs may predate the directory table; create it before adding the index.
            db.execSQL("CREATE TABLE IF NOT EXISTS directory_entries (entry_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, phone TEXT, latitude REAL, longitude REAL, source_url TEXT, verified_at INTEGER)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_directory_coordinates ON directory_entries(latitude, longitude)")
            version = 7
        }
        if (version < 8 && newVersion >= 8) {
            // Some early v6 fixtures/installations may have a user_version but no SOS table.
            // Repair the missing table without dropping any existing data.
            val hasSosTable = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name='sos_events'",
                null
            ).use { it.moveToFirst() }
            if (!hasSosTable) {
                db.execSQL("CREATE TABLE sos_events (id INTEGER PRIMARY KEY AUTOINCREMENT, event_id TEXT NOT NULL, state TEXT NOT NULL, created_at INTEGER NOT NULL, draft_ciphertext TEXT, recipients_ciphertext TEXT, location_ciphertext TEXT)")
            } else {
                val hasEventId = db.rawQuery("PRAGMA table_info('sos_events')", null).use { cursor ->
                    var found = false
                    while (cursor.moveToNext()) if (cursor.getString(1) == "event_id") found = true
                    found
                }
                if (!hasEventId) db.execSQL("ALTER TABLE sos_events ADD COLUMN event_id TEXT")
            }
            // Legacy transition rows cannot safely be grouped into one event; assign independent IDs.
            val legacyIds = mutableListOf<Long>()
            db.rawQuery("SELECT id FROM sos_events WHERE event_id IS NULL", null).use { cursor ->
                while (cursor.moveToNext()) legacyIds.add(cursor.getLong(0))
            }
            legacyIds.forEach { rowId ->
                val values = ContentValues().apply { put("event_id", UUID.randomUUID().toString()) }
                db.update("sos_events", values, "id = ?", arrayOf(rowId.toString()))
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_sos_event_id ON sos_events(event_id, id)")
            version = 8
        }
    }

    /**
     * Append an immutable transition row for one SOS event.
     * Non-QUEUED transitions must carry the originating eventId; a global "latest row" lookup
     * would cross-wire concurrent SOS attempts after a second event is queued.
     */
    fun recordSosEvent(
        state: SosState,
        draft: String?,
        recipients: String?,
        location: String,
        eventId: String? = null
    ): String {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val resolvedEventId = if (state == SosState.QUEUED) {
                UUID.randomUUID().toString()
            } else {
                val transitionEventId = eventId?.takeIf { it.isNotBlank() }
                    ?: error("An existing SOS event ID is required for a transition.")
                db.rawQuery(
                    "SELECT state FROM sos_events WHERE event_id = ? ORDER BY id DESC LIMIT 1",
                    arrayOf(transitionEventId)
                ).use { cursor ->
                    check(cursor.moveToFirst()) { "SOS event not found: $transitionEventId" }
                    val previousState = SosState.valueOf(cursor.getString(0))
                    check(SosStateMachine.canTransition(previousState, state)) {
                        "Illegal SOS transition: ${previousState.name} -> ${state.name}"
                    }
                }
                transitionEventId
            }
            val values = ContentValues().apply {
                put("event_id", resolvedEventId)
                put("state", state.name)
                put("created_at", System.currentTimeMillis())
                if (draft == null) putNull("draft_ciphertext") else put("draft_ciphertext", fieldCipher.encrypt(draft))
                if (recipients == null) putNull("recipients_ciphertext") else put("recipients_ciphertext", fieldCipher.encrypt(recipients))
                put("location_ciphertext", fieldCipher.encrypt(location))
            }
            db.insertOrThrow("sos_events", null, values)
            db.execSQL("DELETE FROM sos_events WHERE id NOT IN (SELECT id FROM sos_events ORDER BY id DESC LIMIT 200)")
            db.setTransactionSuccessful()
            resolvedEventId
        } finally {
            db.endTransaction()
        }
    }

    /** Store one distinct recent query; re-searching updates its recency instead of duplicating it. */
    fun recordSearch(query: String) {
        val clean = query.trim().take(80)
        if (clean.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("search_history", "lower(query) = lower(?)", arrayOf(clean))
            val values = ContentValues().apply {
                put("query", clean)
                put("searched_at", System.currentTimeMillis())
            }
            db.insertOrThrow("search_history", null, values)
            db.execSQL("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searched_at DESC, id DESC LIMIT 30)")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun recentSearches(limit: Int = 8): List<String> {
        val safeLimit = limit.coerceIn(1, 30)
        val result = mutableListOf<String>()
        readableDatabase.rawQuery(
            "SELECT query FROM search_history ORDER BY searched_at DESC, id DESC LIMIT $safeLimit",
            null
        ).use { cursor -> while (cursor.moveToNext()) result.add(cursor.getString(0)) }
        return result
    }

    fun clearSearchHistory() {
        writableDatabase.delete("search_history", null, null)
    }

    fun saveBookmark(itemId: String, label: String) {
        val safeId = itemId.trim().take(120)
        val safeLabel = label.trim().take(240)
        require(safeId.isNotEmpty() && safeLabel.isNotEmpty()) { "Bookmark ID and label are required" }
        val values = ContentValues().apply {
            put("item_id", safeId)
            put("label", safeLabel)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("bookmarks", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun removeBookmark(itemId: String) {
        writableDatabase.delete("bookmarks", "item_id = ?", arrayOf(itemId))
    }

    fun bookmarks(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        readableDatabase.rawQuery("SELECT item_id, label FROM bookmarks ORDER BY label COLLATE NOCASE", null)
            .use { cursor -> while (cursor.moveToNext()) result.add(cursor.getString(0) to cursor.getString(1)) }
        return result
    }

    fun recordFamilyCheckin(status: String) {
        val clean = status.trim().take(200)
        require(clean.isNotEmpty()) { "Check-in status is required" }
        val createdAt = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("status", clean)
            put("created_at", createdAt)
            put("sync_state", "QUEUED_LOCAL_ONLY")
        }
        val payload = clean.toByteArray(Charsets.UTF_8)
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.insertOrThrow("family_checkins", null, values)
            val queue = ContentValues().apply {
                put("action_type", "FAMILY_CHECKIN")
                put("payload_ciphertext", fieldCipher.encrypt(String(payload, Charsets.UTF_8)))
                put("created_at", createdAt)
                put("attempts", 0)
                put("status", "PENDING")
            }
            db.insertOrThrow("pending_actions", null, queue)
            db.execSQL("DELETE FROM family_checkins WHERE id NOT IN (SELECT id FROM family_checkins ORDER BY id DESC LIMIT 50)")
            db.setTransactionSuccessful()
        } finally {
            payload.fill(0)
            db.endTransaction()
        }
    }

    fun latestFamilyCheckin(): String? {
        readableDatabase.rawQuery("SELECT status FROM family_checkins ORDER BY id DESC LIMIT 1", null).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return cursor.getString(0)
        }
    }

    data class PendingAction(val id: Long, val type: String, val payload: String, val createdAt: Long, val attempts: Int, val status: String)

    /** Durable encrypted outbox; items remain local until a real transport acknowledges them. */
    fun pendingActions(limit: Int = 50): List<PendingAction> {
        val safeLimit = limit.coerceIn(1, 100)
        val result = mutableListOf<PendingAction>()
        readableDatabase.rawQuery(
            "SELECT id, action_type, payload_ciphertext, created_at, attempts, status FROM pending_actions WHERE status != 'SYNCED' ORDER BY id ASC LIMIT $safeLimit",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val payload = fieldCipher.decrypt(cursor.getString(2)) ?: continue
                result.add(PendingAction(cursor.getLong(0), cursor.getString(1), payload, cursor.getLong(3), cursor.getInt(4), cursor.getString(5)))
            }
        }
        return result
    }

    fun pendingActionCount(): Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM pending_actions WHERE status != 'SYNCED'", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun updatePendingAction(id: Long, status: String, error: String? = null) {
        require(status in setOf("PENDING", "RETRY", "SYNCED", "FAILED"))
        val db = writableDatabase
        db.execSQL("UPDATE pending_actions SET status = ?, last_error = ?, attempts = attempts + ? WHERE id = ?",
            arrayOf(status, error?.take(200), if (status == "RETRY" || status == "FAILED") 1 else 0, id))
    }

    fun putAppState(key: String, value: String) {
        require(key.isNotBlank() && key.length <= 80)
        val values = ContentValues().apply { put("key", key); put("value", value.take(1000)) }
        writableDatabase.insertWithOnConflict("app_state", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAppState(key: String): String? {
        readableDatabase.rawQuery("SELECT value FROM app_state WHERE key = ?", arrayOf(key)).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    data class SosEventSnapshot(
        val eventId: String,
        val state: SosState,
        val createdAt: Long,
        val draft: String?,
        val recipients: String?,
        val location: String?
    )

    /** Read the latest durable event without treating an undecryptable value as empty data. */
    fun latestSosEventSnapshot(eventId: String? = null): SosEventSnapshot? {
        val sql = if (eventId == null) {
            "SELECT event_id,state,created_at,draft_ciphertext,recipients_ciphertext,location_ciphertext FROM sos_events ORDER BY id DESC LIMIT 1"
        } else {
            "SELECT event_id,state,created_at,draft_ciphertext,recipients_ciphertext,location_ciphertext FROM sos_events WHERE event_id = ? ORDER BY id DESC LIMIT 1"
        }
        readableDatabase.rawQuery(sql, if (eventId == null) null else arrayOf(eventId)).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val eventState = runCatching { SosState.valueOf(cursor.getString(1)) }.getOrNull() ?: return null
            return SosEventSnapshot(
                eventId = cursor.getString(0),
                state = eventState,
                createdAt = cursor.getLong(2),
                draft = fieldCipher.decrypt(cursor.getString(3)),
                recipients = fieldCipher.decrypt(cursor.getString(4)),
                location = fieldCipher.decrypt(cursor.getString(5))
            )
        }
    }


    /**
     * Reconcile every event whose latest durable state was interrupted by process death.
     * QUEUED is known not to have launched the external app; SENDING is ambiguous because
     * the composer may already have opened. Neither state is automatically resent.
     */
    fun reconcileInterruptedSosHandoffs(): Int {
        val db = writableDatabase
        var recoveredCount = 0
        db.beginTransaction()
        try {
            val interrupted = mutableListOf<Array<Any?>>()
            db.rawQuery(
                """
                SELECT e.event_id, e.state, e.draft_ciphertext, e.recipients_ciphertext, e.location_ciphertext
                FROM sos_events e
                WHERE e.id = (
                    SELECT MAX(latest.id) FROM sos_events latest WHERE latest.event_id = e.event_id
                )
                AND e.state IN (?, ?)
                ORDER BY e.id ASC
                """.trimIndent(),
                arrayOf(SosState.QUEUED.name, SosState.SENDING.name)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    interrupted.add(arrayOf(
                        cursor.getString(0), cursor.getString(1),
                        if (cursor.isNull(2)) null else cursor.getString(2),
                        if (cursor.isNull(3)) null else cursor.getString(3),
                        if (cursor.isNull(4)) null else cursor.getString(4)
                    ))
                }
            }
            interrupted.forEach { row ->
                val previous = SosState.valueOf(row[1] as String)
                val recovered = if (previous == SosState.QUEUED) SosState.INTERRUPTED_NOT_SENT else SosState.HANDOFF_UNCONFIRMED
                check(SosStateMachine.canTransition(previous, recovered))
                val values = ContentValues().apply {
                    put("event_id", row[0] as String)
                    put("state", recovered.name)
                    put("created_at", System.currentTimeMillis())
                    if (row[2] == null) putNull("draft_ciphertext") else put("draft_ciphertext", row[2] as String)
                    if (row[3] == null) putNull("recipients_ciphertext") else put("recipients_ciphertext", row[3] as String)
                    if (row[4] == null) putNull("location_ciphertext") else put("location_ciphertext", row[4] as String)
                }
                db.insertOrThrow("sos_events", null, values)
                recoveredCount++
            }
            if (recoveredCount > 0) {
                db.execSQL("DELETE FROM sos_events WHERE id NOT IN (SELECT id FROM sos_events ORDER BY id DESC LIMIT 200)")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return recoveredCount
    }

    fun latestSosState(): SosState? {
        readableDatabase.rawQuery("SELECT state FROM sos_events ORDER BY id DESC LIMIT 1", null).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return runCatching { SosState.valueOf(cursor.getString(0)) }.getOrNull()
        }
    }
}
