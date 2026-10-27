package ph.resq.app.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local transactional foundation for durable emergency state.
 * Directory/map tables are empty until sourced datasets and importers are added.
 */
class ResQDatabase(context: Context, private val fieldCipher: EncryptedFieldCipher = EncryptedFieldCipher()) : SQLiteOpenHelper(context, "ph_resq_local.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE app_state (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
        db.execSQL("CREATE TABLE sos_events (id INTEGER PRIMARY KEY AUTOINCREMENT, state TEXT NOT NULL, created_at INTEGER NOT NULL, draft_ciphertext TEXT, recipients_ciphertext TEXT, location_ciphertext TEXT)")
        db.execSQL("CREATE TABLE last_location (id INTEGER PRIMARY KEY CHECK(id = 1), latitude REAL NOT NULL, longitude REAL NOT NULL, accuracy_m REAL, captured_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE search_history (id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, searched_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks (item_id TEXT PRIMARY KEY NOT NULL, label TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE directory_entries (entry_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, phone TEXT, latitude REAL, longitude REAL, source_url TEXT, verified_at INTEGER)")
        db.execSQL("CREATE TABLE map_tiles (tile_key TEXT PRIMARY KEY NOT NULL, local_path TEXT NOT NULL, sha256 TEXT, dataset_version TEXT, updated_at INTEGER)")
        db.execSQL("CREATE TABLE dataset_meta (dataset_name TEXT PRIMARY KEY NOT NULL, source_url TEXT NOT NULL, license TEXT NOT NULL, version TEXT NOT NULL, verified_at INTEGER NOT NULL, imported_at INTEGER NOT NULL, record_count INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE cached_advisories (advisory_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, severity TEXT NOT NULL, body TEXT NOT NULL, source_url TEXT NOT NULL, published_at INTEGER NOT NULL, fetched_at INTEGER NOT NULL, expires_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE evacuation_centers (center_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, address TEXT NOT NULL, latitude REAL, longitude REAL, status TEXT NOT NULL, source_url TEXT NOT NULL, verified_at INTEGER NOT NULL)")
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
        }
    }

    fun recordSosEvent(state: SosState, draft: String?, recipients: String?, location: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("state", state.name)
                put("created_at", System.currentTimeMillis())
                if (draft == null) putNull("draft_ciphertext") else put("draft_ciphertext", fieldCipher.encrypt(draft))
                if (recipients == null) putNull("recipients_ciphertext") else put("recipients_ciphertext", fieldCipher.encrypt(recipients))
                put("location_ciphertext", fieldCipher.encrypt(location))
            }
            db.insertOrThrow("sos_events", null, values)
            db.execSQL("DELETE FROM sos_events WHERE id NOT IN (SELECT id FROM sos_events ORDER BY id DESC LIMIT 200)")
            db.setTransactionSuccessful()
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

    fun latestSosState(): SosState? {
        readableDatabase.rawQuery("SELECT state FROM sos_events ORDER BY id DESC LIMIT 1", null).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return runCatching { SosState.valueOf(cursor.getString(0)) }.getOrNull()
        }
    }
}
