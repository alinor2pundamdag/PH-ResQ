package ph.resq.app.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local transactional foundation for durable emergency state.
 * Directory/map tables are empty until sourced datasets and importers are added.
 */
class ResQDatabase(context: Context) : SQLiteOpenHelper(context, "ph_resq_local.db", null, 2) {
    private val fieldCipher = EncryptedFieldCipher(context)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE app_state (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
        db.execSQL("CREATE TABLE sos_events (id INTEGER PRIMARY KEY AUTOINCREMENT, state TEXT NOT NULL, created_at INTEGER NOT NULL, draft_ciphertext TEXT, recipients_ciphertext TEXT, location_ciphertext TEXT)")
        db.execSQL("CREATE TABLE last_location (id INTEGER PRIMARY KEY CHECK(id = 1), latitude REAL NOT NULL, longitude REAL NOT NULL, accuracy_m REAL, captured_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE search_history (id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, searched_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks (item_id TEXT PRIMARY KEY NOT NULL, label TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE directory_entries (entry_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, phone TEXT, latitude REAL, longitude REAL, source_url TEXT, verified_at INTEGER)")
        db.execSQL("CREATE TABLE map_tiles (tile_key TEXT PRIMARY KEY NOT NULL, local_path TEXT NOT NULL, sha256 TEXT, dataset_version TEXT, updated_at INTEGER)")
        db.execSQL("CREATE TABLE family_checkins (id INTEGER PRIMARY KEY AUTOINCREMENT, status TEXT NOT NULL, created_at INTEGER NOT NULL, sync_state TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE sos_events ADD COLUMN draft_ciphertext TEXT")
            db.execSQL("ALTER TABLE sos_events ADD COLUMN recipients_ciphertext TEXT")
            db.execSQL("ALTER TABLE sos_events ADD COLUMN location_ciphertext TEXT")
            val legacyRows = mutableListOf<Array<Any?>>()
            db.rawQuery("SELECT id, draft, recipients, location FROM sos_events", null).use { cursor ->
                while (cursor.moveToNext()) {
                    legacyRows.add(arrayOf(
                        cursor.getLong(0),
                        if (cursor.isNull(1)) null else cursor.getString(1),
                        if (cursor.isNull(2)) null else cursor.getString(2),
                        if (cursor.isNull(3)) null else cursor.getString(3)
                    ))
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
                    putNull("draft")
                    putNull("recipients")
                    putNull("location")
                }
                db.update("sos_events", values, "id = ?", arrayOf(row[0].toString()))
            }
        } else {
            throw IllegalStateException("Database migration required: " + oldVersion + " -> " + newVersion)
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

    fun latestSosState(): SosState? {
        readableDatabase.rawQuery("SELECT state FROM sos_events ORDER BY id DESC LIMIT 1", null).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return runCatching { SosState.valueOf(cursor.getString(0)) }.getOrNull()
        }
    }
}
