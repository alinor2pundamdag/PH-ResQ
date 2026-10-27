package ph.resq.app.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class DirectoryRecord(
    val id: String,
    val name: String,
    val category: String,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class DirectoryDataset(
    val sourceUrl: String,
    val license: String,
    val version: String,
    val verifiedAt: Long
)

/**
 * Transactional offline-data primitives. A dataset is accepted only with explicit HTTPS source,
 * license, version, and verification timestamp. Importing records is not proof of national coverage.
 */
class OfflineDataRepository(private val context: Context, private val database: ResQDatabase) {
    fun replaceDirectory(records: List<DirectoryRecord>, dataset: DirectoryDataset) {
        require(dataset.sourceUrl.startsWith("https://")) { "Dataset source must use HTTPS." }
        require(dataset.license.isNotBlank() && dataset.license.length <= 240) { "Dataset license/terms are required." }
        require(dataset.version.isNotBlank() && dataset.version.length <= 80) { "Dataset version is required." }
        require(dataset.verifiedAt > 0 && dataset.verifiedAt <= System.currentTimeMillis() + 300_000L) { "Invalid verification timestamp." }
        require(records.isNotEmpty()) { "Refusing to replace directory with an empty dataset." }
        require(records.size <= 250_000) { "Dataset exceeds the supported import bound." }
        require(records.map { it.id }.distinct().size == records.size) { "Directory IDs must be unique." }
        records.forEach { record ->
            require(record.id.isNotBlank() && record.id.length <= 120)
            require(record.name.isNotBlank() && record.name.length <= 200)
            require(record.category.isNotBlank() && record.category.length <= 80)
            require(record.phone == null || record.phone.length <= 80)
            require((record.latitude == null) == (record.longitude == null)) { "Coordinates must be supplied as a pair." }
            if (record.latitude != null && record.longitude != null) {
                require(record.latitude.isFinite() && record.latitude in -90.0..90.0)
                require(record.longitude.isFinite() && record.longitude in -180.0..180.0)
            }
        }
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("directory_entries", null, null)
            records.forEach { record ->
                val values = ContentValues().apply {
                    put("entry_id", record.id)
                    put("name", record.name.trim())
                    put("category", record.category.trim())
                    if (record.phone == null) putNull("phone") else put("phone", record.phone)
                    if (record.latitude == null) putNull("latitude") else put("latitude", record.latitude)
                    if (record.longitude == null) putNull("longitude") else put("longitude", record.longitude)
                    put("source_url", dataset.sourceUrl)
                    put("verified_at", dataset.verifiedAt)
                }
                db.insertOrThrow("directory_entries", null, values)
            }
            val meta = ContentValues().apply {
                put("dataset_name", "emergency_directory")
                put("source_url", dataset.sourceUrl)
                put("license", dataset.license)
                put("version", dataset.version)
                put("verified_at", dataset.verifiedAt)
                put("imported_at", System.currentTimeMillis())
                put("record_count", records.size)
            }
            db.insertWithOnConflict("dataset_meta", null, meta, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun searchDirectory(query: String, limit: Int = 100): List<DirectoryRecord> {
        val q = query.trim().take(120)
        val safeLimit = limit.coerceIn(1, 500)
        val result = mutableListOf<DirectoryRecord>()
        val sql = if (q.isEmpty()) {
            "SELECT entry_id,name,category,phone,latitude,longitude FROM directory_entries ORDER BY name COLLATE NOCASE LIMIT $safeLimit"
        } else {
            "SELECT entry_id,name,category,phone,latitude,longitude FROM directory_entries WHERE name LIKE ? OR category LIKE ? OR phone LIKE ? ORDER BY name COLLATE NOCASE LIMIT $safeLimit"
        }
        val args = if (q.isEmpty()) null else arrayOf("%$q%", "%$q%", "%$q%")
        database.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) result.add(DirectoryRecord(
                cursor.getString(0), cursor.getString(1), cursor.getString(2),
                if (cursor.isNull(3)) null else cursor.getString(3),
                if (cursor.isNull(4)) null else cursor.getDouble(4),
                if (cursor.isNull(5)) null else cursor.getDouble(5)
            ))
        }
        return result
    }

    fun datasetMetadata(): Map<String, String>? {
        database.readableDatabase.rawQuery(
            "SELECT source_url,license,version,verified_at,imported_at,record_count FROM dataset_meta WHERE dataset_name = ?",
            arrayOf("emergency_directory")
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return mapOf(
                "sourceUrl" to cursor.getString(0), "license" to cursor.getString(1),
                "version" to cursor.getString(2), "verifiedAt" to cursor.getLong(3).toString(),
                "importedAt" to cursor.getLong(4).toString(), "recordCount" to cursor.getInt(5).toString()
            )
        }
    }
}

/**
 * Integrity-checked cache for map tile bytes. This stores tiles only; a renderer and a licensed
 * package/download/rollback pipeline are separate requirements and are not implied by this class.
 */
class OfflineMapTileCache(context: Context, private val database: ResQDatabase) {
    private val root = File(context.applicationContext.filesDir, "offline-map-tiles").apply { mkdirs() }

    fun save(tileKey: String, bytes: ByteArray, expectedSha256: String, datasetVersion: String): File {
        require(tileKey.isNotBlank() && tileKey.length <= 200 && !tileKey.contains("..")) { "Invalid tile key." }
        require(bytes.isNotEmpty() && bytes.size <= 2_000_000) { "Tile must be 1 byte to 2 MB." }
        require(datasetVersion.isNotBlank() && datasetVersion.length <= 80)
        require(IntegrityVerifier.verify(bytes, expectedSha256)) { "Tile integrity check failed." }
        val safeName = IntegrityVerifier.sha256Hex(tileKey.toByteArray(Charsets.UTF_8)) + ".tile"
        val destination = File(root, safeName)
        val temporary = File(root, safeName + ".tmp")
        temporary.writeBytes(bytes)
        if (!temporary.renameTo(destination)) {
            temporary.delete()
            throw IllegalStateException("Unable to atomically store map tile.")
        }
        val values = ContentValues().apply {
            put("tile_key", tileKey); put("local_path", destination.absolutePath)
            put("sha256", expectedSha256.lowercase()); put("dataset_version", datasetVersion)
            put("updated_at", System.currentTimeMillis())
        }
        database.writableDatabase.insertWithOnConflict("map_tiles", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return destination
    }

    fun read(tileKey: String): ByteArray? {
        database.readableDatabase.rawQuery("SELECT local_path,sha256 FROM map_tiles WHERE tile_key = ?", arrayOf(tileKey)).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val file = File(cursor.getString(0))
            if (file.canonicalFile.parentFile != root.canonicalFile || !file.isFile) return null
            val bytes = file.readBytes()
            if (!IntegrityVerifier.verify(bytes, cursor.getString(1))) {
                file.delete()
                database.writableDatabase.delete("map_tiles", "tile_key = ?", arrayOf(tileKey))
                return null
            }
            return bytes
        }
    }
}
