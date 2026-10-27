package ph.resq.app.core

import android.content.ContentValues

enum class AdvisorySeverity { INFO, WATCH, WARNING, EMERGENCY }
enum class EvacuationStatus { OPEN, LIMITED, FULL, CLOSED, UNKNOWN }

data class CachedAdvisory(val id: String, val title: String, val severity: AdvisorySeverity, val body: String, val sourceUrl: String, val publishedAt: Long, val fetchedAt: Long, val expiresAt: Long) {
    fun isStale(now: Long = System.currentTimeMillis()): Boolean = expiresAt < now || fetchedAt > now + 300_000L
}

data class EvacuationCenter(val id: String, val name: String, val address: String, val latitude: Double?, val longitude: Double?, val status: EvacuationStatus, val sourceUrl: String, val verifiedAt: Long) {
    fun isStale(now: Long = System.currentTimeMillis(), maxAgeMs: Long = 86_400_000L): Boolean = verifiedAt > now + 300_000L || now - verifiedAt > maxAgeMs
}

/** Local cache/import API only. Callers must supply attributed records; this class does not fetch live feeds. */
class HazardDataRepository(private val database: ResQDatabase) {
    fun replaceAdvisories(records: List<CachedAdvisory>) {
        require(records.isNotEmpty()) { "Refusing to replace advisory cache with an empty batch." }
        require(records.size <= 2_000 && records.map { it.id }.distinct().size == records.size)
        val now = System.currentTimeMillis()
        records.forEach { require(it.id.isNotBlank() && it.id.length <= 160 && it.title.isNotBlank() && it.title.length <= 200 && it.body.isNotBlank() && it.body.length <= 4_000 && it.sourceUrl.startsWith("https://") && it.publishedAt > 0 && it.fetchedAt > 0 && it.expiresAt >= it.fetchedAt && it.fetchedAt <= now + 300_000L) }
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("cached_advisories", null, null)
            records.forEach { item ->
                val v = ContentValues().apply {
                    put("advisory_id", item.id); put("title", item.title); put("severity", item.severity.name)
                    put("body", item.body); put("source_url", item.sourceUrl); put("published_at", item.publishedAt)
                    put("fetched_at", item.fetchedAt); put("expires_at", item.expiresAt)
                }
                db.insertOrThrow("cached_advisories", null, v)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun advisories(now: Long = System.currentTimeMillis()): List<Pair<CachedAdvisory, Boolean>> {
        val out = mutableListOf<Pair<CachedAdvisory, Boolean>>()
        database.readableDatabase.rawQuery("SELECT advisory_id,title,severity,body,source_url,published_at,fetched_at,expires_at FROM cached_advisories ORDER BY published_at DESC", null).use { c ->
            while (c.moveToNext()) {
                val item = CachedAdvisory(c.getString(0), c.getString(1), runCatching { AdvisorySeverity.valueOf(c.getString(2)) }.getOrDefault(AdvisorySeverity.INFO), c.getString(3), c.getString(4), c.getLong(5), c.getLong(6), c.getLong(7))
                out.add(item to item.isStale(now))
            }
        }
        return out
    }

    fun replaceEvacuationCenters(records: List<EvacuationCenter>) {
        require(records.isNotEmpty()) { "Refusing to replace evacuation-centre cache with an empty batch." }
        require(records.size <= 50_000 && records.map { it.id }.distinct().size == records.size)
        val now = System.currentTimeMillis()
        records.forEach { item ->
            require(item.id.isNotBlank() && item.id.length <= 160 && item.name.isNotBlank() && item.name.length <= 200 && item.address.isNotBlank() && item.address.length <= 400 && item.sourceUrl.startsWith("https://") && item.verifiedAt > 0 && item.verifiedAt <= now + 300_000L)
            require((item.latitude == null) == (item.longitude == null))
            if (item.latitude != null) require(item.latitude.isFinite() && item.latitude in -90.0..90.0 && item.longitude!!.isFinite() && item.longitude in -180.0..180.0)
        }
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("evacuation_centers", null, null)
            records.forEach { item ->
                val v = ContentValues().apply {
                    put("center_id", item.id); put("name", item.name); put("address", item.address)
                    if (item.latitude == null) putNull("latitude") else put("latitude", item.latitude)
                    if (item.longitude == null) putNull("longitude") else put("longitude", item.longitude)
                    put("status", item.status.name); put("source_url", item.sourceUrl); put("verified_at", item.verifiedAt)
                }
                db.insertOrThrow("evacuation_centers", null, v)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun searchEvacuationCenters(query: String, now: Long = System.currentTimeMillis(), maxAgeMs: Long = 86_400_000L): List<Pair<EvacuationCenter, Boolean>> {
        val q = query.trim().take(120)
        val out = mutableListOf<Pair<EvacuationCenter, Boolean>>()
        val sql = "SELECT center_id,name,address,latitude,longitude,status,source_url,verified_at FROM evacuation_centers " + (if (q.isEmpty()) "" else "WHERE name LIKE ? OR address LIKE ? ") + "ORDER BY name COLLATE NOCASE LIMIT 500"
        val args = if (q.isEmpty()) null else arrayOf("%$q%", "%$q%")
        database.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                val item = EvacuationCenter(c.getString(0), c.getString(1), c.getString(2), if (c.isNull(3)) null else c.getDouble(3), if (c.isNull(4)) null else c.getDouble(4), runCatching { EvacuationStatus.valueOf(c.getString(5)) }.getOrDefault(EvacuationStatus.UNKNOWN), c.getString(6), c.getLong(7))
                out.add(item to item.isStale(now, maxAgeMs))
            }
        }
        return out
    }
}
